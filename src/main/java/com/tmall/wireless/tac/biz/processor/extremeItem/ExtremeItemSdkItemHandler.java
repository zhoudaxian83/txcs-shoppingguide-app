package com.tmall.wireless.tac.biz.processor.extremeItem;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.tmall.aselfcaptain.item.constant.Channel;
import com.tmall.aselfcaptain.item.model.ItemDTO;
import com.tmall.aselfcaptain.item.model.ItemId;
import com.tmall.aselfcaptain.item.model.ItemQueryDO;
import com.tmall.aselfcaptain.item.model.QueryOptionDO;
import com.tmall.tcls.gs.sdk.framework.extensions.item.iteminfo.CaptainRequestBuildRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.*;
import com.tmall.tcls.gs.sdk.framework.model.meta.ItemInfoSourceMetaInfo;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkItemService;
import com.tmall.tcls.gs.sdk.framework.suport.iteminfo.sm.ItemInfoRequestSm;
import com.tmall.tmallwireless.tac.spi.context.SPIResult;
import com.tmall.wireless.store.spi.render.RenderSpi;
import com.tmall.wireless.store.spi.render.model.RenderRequest;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemConfigGroupList;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemConfigs;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import com.tmall.wireless.tac.client.handler.TacReactiveHandler4Ald;
import io.reactivex.Flowable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallCommonAldConstant.STATIC_SCHEDULE_DATA;


/**
 * Created from template by 言武 on 2021-09-10 14:36:48.
 *
 */

@Component
public class ExtremeItemSdkItemHandler extends TacReactiveHandler4Ald {

    @Autowired
    ShoppingguideSdkItemService shoppingguideSdkItemService;
    @Autowired
    TacLogger tacLogger;
    @Autowired
    RenderSpi renderSpi;


    @Override
    public Flowable<TacResult<List<GeneralItem>>> executeFlowable(RequestContext4Ald requestContext4Ald) throws Exception {
        try {
            //tacLogger.info("context:" + JSON.toJSONString(requestContext4Ald));
            List<Map<String, Object>> aldDataList = (List<Map<String, Object>>) requestContext4Ald.getAldContext().get(STATIC_SCHEDULE_DATA);
            //tacLogger.info("aldDataList:" + aldDataList);
            ItemConfigs itemConfigs = ItemConfigs.valueOf(aldDataList);
            //tacLogger.info("itemConfigs:" + JSON.toJSONString(itemConfigs));
            itemConfigs.checkItemConfig();
            ItemConfigGroupList itemConfigGroupList = itemConfigs.splitGroup();
            //tacLogger.info("itemConfigGroupList:" + JSON.toJSONString(itemConfigGroupList));
            itemConfigGroupList.sortGroup();
            //tacLogger.info("==========after sort itemConfigGroupList:" + JSON.toJSONString(itemConfigGroupList));

            //查询captain
            List<Long> itemIds = itemConfigs.extractItemIds();
            RenderRequest renderRequest = buildRenderRequest(itemIds, 0L, 330110L);
            SPIResult<List<ItemDTO>> itemDTOs = renderSpi.query(renderRequest);
            tacLogger.info("==========itemDTOs: " + JSON.toJSONString(itemDTOs));
        } catch (Exception e) {
            tacLogger.error(e.getMessage(), e);
        }
        return Flowable.just(TacResult.newResult(new ArrayList<>()));
    }

    public RenderRequest buildRenderRequest(List<Long> itemIds, Long buyerId, Long areaId) {
        RenderRequest renderRequest = new RenderRequest();
        ItemQueryDO query = new ItemQueryDO();
        List<ItemId> itemIdList = itemIds.stream()
                .map(itemId -> ItemId.valueOf(itemId, ItemId.ItemType.B2C))
                .collect(Collectors.toList());
        query.setItemIds(itemIdList);
        query.setBuyerId(buyerId);
        query.setSource("txcs-shoppingguide");
        query.setChannel(Channel.WAP);
        //query.setLocationId(storeId);
        query.setAreaId(areaId);
        QueryOptionDO option = new QueryOptionDO();
        /*if (StringUtils.isNotEmpty(itemInfoSourceMetaInfo.getUmpChannelKey())) {
            Map<String, String> extraParams = Maps.newHashMap();
            extraParams.put("umpChannel", itemInfoSourceMetaInfo.getUmpChannelKey());
            query.setExtraParams(extraParams);
        }*/

        option.setIncludeQuantity(true);
        option.setIncludeSales(true);
        option.setIncludeItemTags(true);
        option.setIncludeItemFeature(true);
        option.setIncludeMaiFanCard(true);
        option.setIncludeTiming(true);
        /*if (StringUtils.isNotEmpty(itemInfoSourceMetaInfo.getMktSceneCode())) {
            option.setSceneCode(itemInfoSourceMetaInfo.getMktSceneCode());
            option.setOpenMkt(true);
        }*/

        renderRequest.setQuery(query);
        renderRequest.setOption(option);
        return renderRequest;
    }
}
