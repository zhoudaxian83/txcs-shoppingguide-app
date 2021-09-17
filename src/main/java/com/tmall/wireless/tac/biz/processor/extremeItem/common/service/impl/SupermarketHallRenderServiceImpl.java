package com.tmall.wireless.tac.biz.processor.extremeItem.common.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.taobao.eagleeye.EagleEye;
import com.tmall.aselfcaptain.item.constant.Channel;
import com.tmall.aselfcaptain.item.model.ItemDTO;
import com.tmall.aselfcaptain.item.model.ItemId;
import com.tmall.aselfcaptain.item.model.ItemQueryDO;
import com.tmall.aselfcaptain.item.model.QueryOptionDO;
import com.tmall.tmallwireless.tac.spi.context.SPIResult;
import com.tmall.wireless.store.spi.render.RenderSpi;
import com.tmall.wireless.store.spi.render.model.RenderRequest;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.service.SupermarketHallRenderService;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SupermarketHallRenderServiceImpl implements SupermarketHallRenderService {
    private static Logger logger = LoggerFactory.getLogger(SupermarketHallRenderServiceImpl.class);

    @Autowired
    TacLogger tacLogger;
    @Autowired
    RenderSpi renderSpi;

    @Override
    public Map<Long, ItemDTO> batchQueryItem(List<Long> itemIdList) {
        tacLogger.info("batchQueryItem start");

        Map<Long, ItemDTO> captainItemMap = Maps.newHashMap();

        final Object rpcContext = EagleEye.currentRpcContext();
        final long callerId = Thread.currentThread().getId();

        if (CollectionUtils.isEmpty(itemIdList)) {
            tacLogger.info("batchQueryItem start, itemIdList empty");
            return captainItemMap;
        }

        Lists.partition(itemIdList, 20)
                .parallelStream()
                .map(list -> {
                    try {
                        EagleEye.setRpcContext(rpcContext);
                        Map<Long, ItemDTO> longItemDTOMap = queryItem(list);
                        if (MapUtils.isEmpty(longItemDTOMap)) {
                            tacLogger.info("batch query capatin empty;" + JSON.toJSONString(list));
                        }
                        return longItemDTOMap;
                    } catch (Exception e) {
                        tacLogger.error("batchQueryItem_catchException", e);
                        return new HashMap<Long, ItemDTO>();
                    } finally {
                        if (Thread.currentThread().getId() != callerId) {
                            EagleEye.clearRpcContext();
                        }
                    }
                })
                .forEach(e -> captainItemMap.putAll(e));

        return captainItemMap;
    }

    private Map<Long, ItemDTO> queryItem(List<Long> itemIds) {
        RenderRequest renderRequest = buildRenderRequest(itemIds, "330110",null, 1034513083L);
        SPIResult<List<ItemDTO>> itemDTOs = renderSpi.query(renderRequest);
        return itemDTOs.getData().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    }

    RenderRequest buildRenderRequest(List<Long> itemIds, String smAreaId, String queryTime, Long userId) {
        RenderRequest renderRequest = new RenderRequest();
        ItemQueryDO query = new ItemQueryDO();

        if (StringUtils.isNotBlank(smAreaId) && !"0".equals(smAreaId)) {
            query.setAreaId(Long.valueOf(smAreaId));
        } else {
            tacLogger.debug("smAreaId is null" + smAreaId);
            return null;
        }
        List<ItemId> itemIdList = itemIds.stream()
                .map(itemId -> ItemId.valueOf(itemId, ItemId.ItemType.B2C))
                .collect(Collectors.toList());
        query.setItemIds(itemIdList);

        query.setChannel(Channel.WAP);
        if(StringUtils.isNotEmpty(queryTime)){
            query.setQueryTime(queryTime);
        }

        if (userId != null && userId != 0) {
            query.setBuyerId(userId);
        }

        query.setSource("txcs-shoppingguide", "hall");

        QueryOptionDO option = new QueryOptionDO();
        option.setOpenMkt(true);
        option.setSceneCode("conference.promotion");
        option.setIncludeQuantity(true);
        option.setIncludeSales(true);
        option.setIncludeMaiFanCard(true);
        option.setUserPromotion(false);
        renderRequest.setQuery(query);
        renderRequest.setOption(option);
        return renderRequest;
    }
}
