package com.tmall.wireless.tac.biz.processor.processtemplate.common.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.taobao.eagleeye.EagleEye;
import com.tmall.aselfcaptain.item.constant.Channel;
import com.tmall.aselfcaptain.item.model.ItemDTO;
import com.tmall.aselfcaptain.item.model.ItemId;
import com.tmall.aselfcaptain.item.model.ItemQueryDO;
import com.tmall.aselfcaptain.item.model.QueryOptionDO;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tmallwireless.tac.spi.context.SPIResult;
import com.tmall.wireless.store.spi.render.RenderSpi;
import com.tmall.wireless.store.spi.render.model.RenderRequest;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.util.Logger;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.util.LoggerProxy;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.ProcessTemplateContext;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.ProcessTemplateRenderService;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.util.CommonParamUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProcessTemplateRenderServiceImpl implements ProcessTemplateRenderService {
    private static Logger logger = LoggerProxy.getLogger(ProcessTemplateRenderServiceImpl.class);

    @Autowired
    RenderSpi renderSpi;

    @Override
    public Map<Long, ItemDTO> batchQueryItem(List<Long> itemIdList, ProcessTemplateContext processTemplateContext) {
        String sourceClassName = processTemplateContext.getSourceClassSimpleName();
        Long captainStart = System.currentTimeMillis();
        Map<Long, ItemDTO> captainItemMap = Maps.newHashMap();
        try {
            final Object rpcContext = EagleEye.currentRpcContext();
            final long callerId = Thread.currentThread().getId();

            if (CollectionUtils.isEmpty(itemIdList)) {
                logger.warn("batchQueryItem, itemIdList empty, traceId:" + EagleEye.getTraceId());
                return captainItemMap;
            }

            Lists.partition(itemIdList, 20)
                    .parallelStream()
                    .map(list -> {
                        try {
                            EagleEye.setRpcContext(rpcContext);
                            Map<Long, ItemDTO> longItemDTOMap = queryItem(list, processTemplateContext);
                            if (MapUtils.isEmpty(longItemDTOMap)) {
                                logger.info("batch query capatin empty;" + JSON.toJSONString(list));
                            }
                            return longItemDTOMap;
                        } catch (Exception e) {
                            logger.error("batchQueryItem_catchException, traceId:" + EagleEye.getTraceId(), e);
                            return new HashMap<Long, ItemDTO>();
                        } finally {
                            if (Thread.currentThread().getId() != callerId) {
                                EagleEye.clearRpcContext();
                            }
                        }
                    })
                    .forEach(e -> captainItemMap.putAll(e));
            if(MapUtils.isNotEmpty(captainItemMap)) {
                Long captainEnd = System.currentTimeMillis();
                HadesLogUtil.stream(sourceClassName + "|captain|" + Logger.isEagleEyeTest() + "|success|" + (captainEnd - captainStart))
                        .error();
                return captainItemMap;
            }
        } catch (Exception e) {
            HadesLogUtil.stream(sourceClassName + "|captain|" + Logger.isEagleEyeTest() + "|exception")
                    .error();
            logger.error("SupermarketHallRenderServiceImpl.baItchQueryItem error, traceId:" + EagleEye.getTraceId(), e);
            return captainItemMap;
        }
        if(MapUtils.isEmpty(captainItemMap)) {
            HadesLogUtil.stream(sourceClassName + "|captain|" + Logger.isEagleEyeTest() + "|empty")
                    .error();
        }
        return captainItemMap;
    }

    private Map<Long, ItemDTO> queryItem(List<Long> itemIds, ProcessTemplateContext processTemplateContext) {
        RenderRequest renderRequest = buildRenderRequest(itemIds, processTemplateContext);
        SPIResult<List<ItemDTO>> itemDTOsResult = renderSpi.query(renderRequest);
        if(itemDTOsResult.isSuccess()) {
            return itemDTOsResult.getData().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
        } else {
            return new HashMap<>();
        }
    }

    RenderRequest buildRenderRequest(List<Long> itemIds, ProcessTemplateContext context) {
        RenderRequest renderRequest = new RenderRequest();
        ItemQueryDO query = new ItemQueryDO();
        String smAreaId = context.getSmAreaId();
        if (CommonParamUtil.isValidSmAreaId(smAreaId)) {
            query.setAreaId(Long.valueOf(smAreaId));
        } else {
            logger.warn("smAreaId is null, use 330110,smAreaId=" + smAreaId);
            query.setAreaId(330110L);
        }
        List<ItemId> itemIdList = itemIds.stream()
                .map(itemId -> ItemId.valueOf(itemId, ItemId.ItemType.B2C))
                .collect(Collectors.toList());
        query.setItemIds(itemIdList);

        query.setChannel(Channel.WAP);
        String queryTime = context.getPreviewTime();
        if(StringUtils.isNotEmpty(queryTime)){
            query.setQueryTime(queryTime);
        }

        Long userId = context.getUserId();
        if (userId != null && userId != 0) {
            query.setBuyerId(userId);
        }

        query.setSource("txcs-shoppingguide", "hall");

        QueryOptionDO option = new QueryOptionDO();
        option.setOpenMkt(true);
        String sceneCode = context.getSceneCode();
        if(StringUtils.isNotBlank(sceneCode)) {
            option.setSceneCode(sceneCode);
        } else {
            option.setSceneCode("conference.promotion");
        }
        option.setIncludeQuantity(true);
        option.setIncludeSales(true);
        option.setIncludeMaiFanCard(true);
        option.setUserPromotion(false);
        renderRequest.setQuery(query);
        renderRequest.setOption(option);
        return renderRequest;
    }
}
