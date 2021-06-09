package com.tmall.wireless.tac.biz.processor.firstScreenMind;

import com.alibaba.cola.extension.Extension;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.extensions.origindata.request.ItemOriginDataRequestExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.model.biz.context.LocParams;
import com.tmall.txcs.gs.model.biz.context.PageInfoDO;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.txcs.gs.model.spi.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.common.RequestKeyConstantApp;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.common.FirstScreenConstant;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.RenderContentTypeEnum;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.TppItemBusinessTypeEnum;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.origindatarequest.OriginDataRequestFactory;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.utils.RenderAddressUtil;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.utils.RenderLangUtil;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;

@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_ITEM)
@Service
public class FirstScreenMindItemOriginDataRequestExtPt implements ItemOriginDataRequestExtPt {

    @Autowired
    OriginDataRequestFactory originDataRequestFactory;

    @Override
    public RecommendRequest process(SgFrameworkContextItem sgFrameworkContextItem) {

        RecommendRequest tppRequest = originDataRequestFactory.getRecommendRequest(FirstScreenConstant.ITEM_ITEM_FEEDS,sgFrameworkContextItem);
        return tppRequest;
        /*tacLogger.info("****FirstScreenMindItemOriginDataRequestExtPt sgFrameworkContextItem****:"+sgFrameworkContextItem.toString());

        boolean isO2o = isO2oScene(sgFrameworkContextItem);

        RecommendRequest tppRequest = new RecommendRequest();
        Map<String, String> params = Maps.newHashMap();
        params.put("pageSize", Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContext::getUserPageInfo).map(PageInfoDO::getPageSize).orElse(20).toString());
        params.put("smAreaId", Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContext::getLocParams).map(LocParams::getSmAreaId).orElse(0L).toString());
        params.put("logicAreaId", Joiner.on(",").join(Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContext::getLocParams).map(LocParams::getLogicIdByPriority).orElse(Lists.newArrayList(107L))));
        //params.put("itemBusinessType","B2C");
        Optional<Map> requestParams = Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContext::getRequestParams);
        params.put("itemSetIdList", requestParams.map(entry -> entry.get("itemSetIds")).orElse("").toString());
        String csa = requestParams.map(entry -> entry.get("csa")).orElse("").toString();
        boolean rt1HourStoreCover = RenderAddressUtil.rt1HourStoreCover(csa);
        boolean rtHalfDayStoreCover = RenderAddressUtil.rtHalfDayStoreCover(csa);
        boolean nextDayStoreCover = RenderAddressUtil.nextDayStoreCover(csa);
        Long rt1HourStoreId = RenderAddressUtil.getRt1HourStoreId(csa);
        Long rtHalfDayStoreId = RenderAddressUtil.getRtHalfDayStoreId(csa);
        Long rtNextDayStoreId = RenderAddressUtil.getRtNextDayStoreId(csa);


        //默认优先级 一小时达 > 半日达 > 外仓
        if (isO2o) {
            if(rt1HourStoreCover){
                params.put("itemBusinessType", TppItemBusinessTypeEnum.OneHour.getType());
                params.put("rt1HourStoreId", RenderLangUtil.safeString(rt1HourStoreId));
            } else if(rtHalfDayStoreCover){
                params.put("itemBusinessType", TppItemBusinessTypeEnum.HalfDay.getType());
                params.put("rtHalfDayStoreId", RenderLangUtil.safeString(rtHalfDayStoreId));
            } else {
                params.put("itemBusinessType", TppItemBusinessTypeEnum.B2C.getType());
            }
        } else {
            params.put("itemBusinessType", TppItemBusinessTypeEnum.B2C.getType());
        }
        params.put("exposureDataUserId",Optional.ofNullable(sgFrameworkContextItem).map(
            SgFrameworkContext::getUserDO).map(UserDO::getCna).orElse(""));
        params.put("sceneId", requestParams.map(entry -> entry.get("moduleId")).orElse("").toString());
        if(isBangdan(sgFrameworkContextItem)){
            tppRequest.setAppId(25399L);
        }else{
            tppRequest.setAppId(23410L);
        }
        *//***TPP相关常量*//*
        params.put("itemSetIdSource","crm");
        Integer index = Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContext::getUserPageInfo).map(PageInfoDO::getIndex).orElse(0);
        params.put("isFirstPage", index > 0 ? "false" : "true");


        tppRequest.setParams(params);
        tppRequest.setLogResult(true);
        tppRequest.setUserId(Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContext::getUserDO).map(UserDO::getUserId).orElse(0L));
        tacLogger.info("****FirstScreenMindItemOriginDataRequestExtPt tppRequest****:"+tppRequest.toString());
        return tppRequest;*/
    }

}
