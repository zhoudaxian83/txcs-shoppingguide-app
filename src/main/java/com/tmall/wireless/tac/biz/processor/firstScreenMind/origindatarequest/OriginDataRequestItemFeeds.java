package com.tmall.wireless.tac.biz.processor.firstScreenMind.origindatarequest;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.txcs.gs.model.biz.context.LocParams;
import com.tmall.txcs.gs.model.biz.context.PageInfoDO;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.txcs.gs.model.spi.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.common.RequestKeyConstantApp;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.RenderContentTypeEnum;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.TppItemBusinessTypeEnum;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.utils.RenderLangUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Optional;

import com.alibaba.fastjson.JSON;

/**
 * @author guijian
 */
public class OriginDataRequestItemFeeds implements OriginDataRequest{
    @Override
    public RecommendRequest buildRecommendRequest(SgFrameworkContext sgFrameworkContext) {
        RecommendRequest tppRequest = new RecommendRequest();
        Map<String, String> params = Maps.newHashMap();
        params.put("pageSize", Optional.ofNullable(sgFrameworkContext).map(SgFrameworkContext::getUserPageInfo).map(
            PageInfoDO::getPageSize).orElse(20).toString());
        params.put("smAreaId", Optional.ofNullable(sgFrameworkContext).map(SgFrameworkContext::getLocParams).map(
            LocParams::getSmAreaId).orElse(0L).toString());
        params.put("logicAreaId", Joiner
            .on(",").join(Optional.ofNullable(sgFrameworkContext).map(SgFrameworkContext::getLocParams).map(LocParams::getLogicIdByPriority).orElse(
                Lists.newArrayList(107L))));
        Optional<Map> requestParams = Optional.ofNullable(sgFrameworkContext).map(SgFrameworkContext::getRequestParams);
        params.put("itemSetIdList", requestParams.map(entry -> entry.get("itemSetIds")).orElse("").toString());


        Long rt1HourStoreId = Optional.ofNullable(sgFrameworkContext).map(SgFrameworkContext::getLocParams).map(LocParams::getRt1HourStoreId).orElse(0L);
        Long rtHalfDayStoreId = Optional.ofNullable(sgFrameworkContext).map(SgFrameworkContext::getLocParams).map(LocParams::getRtHalfDayStoreId).orElse(0L);

        boolean rt1HourStoreCover = rt1HourStoreId > 0L;
        boolean rtHalfDayStoreCover = rtHalfDayStoreId > 0L;

        boolean isO2o = isO2oScene(sgFrameworkContext);
        //??????????????? ???????????? > ????????? > ??????
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
        params.put("exposureDataUserId",Optional.ofNullable(sgFrameworkContext).map(
            SgFrameworkContext::getUserDO).map(UserDO::getCna).orElse(""));
        params.put("sceneId", getModuleId(requestParams));
        if(isBangdan(sgFrameworkContext)){
            tppRequest.setAppId(25399L);
        }else{
            tppRequest.setAppId(23410L);
        }
        /***TPP????????????*/
        params.put("itemSetIdSource","crm");
        Integer index = Optional.ofNullable(sgFrameworkContext).map(SgFrameworkContext::getUserPageInfo).map(PageInfoDO::getIndex).orElse(0);
        params.put("isFirstPage", index > 0 ? "false" : "true");


        tppRequest.setParams(params);
        tppRequest.setLogResult(true);
        tppRequest.setUserId(Optional.ofNullable(sgFrameworkContext).map(SgFrameworkContext::getUserDO).map(UserDO::getUserId).orElse(0L));
        return tppRequest;
    }

    private String getModuleId(Optional<Map> requestParams) {
        HadesLogUtil.stream(ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_ITEM)
            .kv("OriginDataRequestItemFeeds","getModuleId")
            .kv("requestParams", JSON.toJSONString(requestParams))
            .info();
        String moduleId = requestParams.map(entry -> entry.get("moduleId")).orElse("").toString();
        String contentId = requestParams.map(entry -> entry.get("contentId")).orElse("").toString();
        String exposureContentIds = requestParams.map(entry -> entry.get("exposureContentIds")).orElse("").toString();
        if (StringUtils.isNotBlank(moduleId)) {
            return moduleId;
        }else if(StringUtils.isNotBlank(contentId)){
            return contentId;
        }else if(StringUtils.isNotBlank(exposureContentIds)){
            /**?????????????????????**/
            String exposureContentId = exposureContentIds.split(",")[0];
            return exposureContentId;
        }
        return contentId;
    }

    private boolean isO2oScene(SgFrameworkContext sgFrameworkContext) {

        String contentType = MapUtil.getStringWithDefault(sgFrameworkContext.getRequestParams(), RequestKeyConstantApp.CONTENT_TYPE, RenderContentTypeEnum.b2cNormalContent.getType());
        return RenderContentTypeEnum.checkO2OContentType(contentType);
    }

    private boolean isBangdan(SgFrameworkContext sgFrameworkContext) {
        String contentType = MapUtil.getStringWithDefault(sgFrameworkContext.getRequestParams(), RequestKeyConstantApp.CONTENT_TYPE, RenderContentTypeEnum.b2cNormalContent.getType());
        return RenderContentTypeEnum.bangdanContent.getType().equals(contentType)
        || RenderContentTypeEnum.bangdanO2OContent.getType().equals(contentType);
    }

}
