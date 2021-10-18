package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.tmall.aselfcaptain.item.constant.Channel;
import com.tmall.aselfcaptain.item.model.ItemId;
import com.tmall.aselfcaptain.item.model.ItemQueryDO;
import com.tmall.aselfcaptain.item.model.QueryOptionDO;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.iteminfo.CaptainRequestBuildRequest;
import com.tmall.tcls.gs.sdk.framework.extensions.item.iteminfo.CaptainRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.*;
import com.tmall.tcls.gs.sdk.framework.model.meta.ItemInfoSourceMetaInfo;
import com.tmall.tcls.gs.sdk.framework.suport.iteminfo.sm.ItemInfoRequestSm;
import com.tmall.wireless.store.spi.render.model.RenderRequest;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.constant.CommonConstant;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created from template by 罗俊冲 on 2021-10-08 11:07:19.
 * captain请求组装 - captain请求组装.
 */

@SdkExtension(
        bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.TODAY_CRAZY_RECOMMEND_TAB
)
public class TodayCrazyRecommendTabCaptainRequestBuildSdkExtPt extends Register implements CaptainRequestBuildSdkExtPt {
    public static final String UMP_CHANNEL = "umpChannel";

    @Autowired
    TacLoggerImpl tacLogger;

    @Override
    public RenderRequest process(CaptainRequestBuildRequest captainRequestBuildRequest) {
        RenderRequest renderRequest = new RenderRequest();
        ItemQueryDO query = new ItemQueryDO();
        SgFrameworkContextItem contextItem = captainRequestBuildRequest.getContextItem();
        ItemInfoRequestSm itemInfoRequest = captainRequestBuildRequest.getItemInfoRequest();
        ItemInfoSourceMetaInfo itemInfoSourceMetaInfo = captainRequestBuildRequest.getItemInfoSourceMetaInfo();
        //TODO 后面整理到参数构建扩展点     public static final String CHANNEL_KEY = "panicBuyingToday";
        itemInfoSourceMetaInfo.setUmpChannelKey(CommonConstant.CHANNEL_KEY);
        Long storeId = itemInfoRequest.getStoreId();
        List<ItemId> itemIdList = itemInfoRequest.getList().stream().map(itemEntity ->
                ItemId.valueOf(itemEntity.getItemId(), ItemId.ItemType.valueOf(itemEntity.getO2oType()))).collect(Collectors.toList());
        query.setItemIds(itemIdList);
        query.setBuyerId(Optional.of(contextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getUserDO).map(UserDO::getUserId).orElse(0L));
        query.setSource("txcs-shoppingguide");
        query.setChannel(Channel.WAP);
        query.setLocationId(storeId);
        query.setAreaId(Optional.of(contextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getLocParams).map(LocParams::getSmAreaId).orElse(0L));


        QueryOptionDO option = new QueryOptionDO();
        if (StringUtils.isNotEmpty(itemInfoSourceMetaInfo.getUmpChannelKey())) {
            Map<String, String> extraParams = Maps.newHashMap();
            extraParams.put(UMP_CHANNEL, itemInfoSourceMetaInfo.getUmpChannelKey());
            query.setExtraParams(extraParams);
        }

        option.setIncludeQuantity(true);
        option.setIncludeSales(true);
        option.setIncludeItemTags(true);
        option.setIncludeItemFeature(true);
        option.setIncludeMaiFanCard(true);
        option.setIncludeTiming(true);
        option.setSceneCode(CommonConstant.SUPER_MARKET_TODAY_CRAZY);
        option.setOpenMkt(true);

//        if (StringUtils.isNotEmpty(itemInfoSourceMetaInfo.getMktSceneCode())) {
//            option.setSceneCode(itemInfoSourceMetaInfo.getMktSceneCode());
//        }


        renderRequest.setQuery(query);
        renderRequest.setOption(option);
        //
        tacLogger.info("captain入参：" + JSON.toJSONString(renderRequest));
        HadesLogUtil.stream(ScenarioConstantApp.TODAY_CRAZY_RECOMMEND_TAB)
                .kv("renderRequest", JSON.toJSONString(renderRequest))
                .info();
        return renderRequest;
    }
}
