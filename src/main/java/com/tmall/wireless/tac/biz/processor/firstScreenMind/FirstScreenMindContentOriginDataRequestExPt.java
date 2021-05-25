package com.tmall.wireless.tac.biz.processor.firstScreenMind;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.alibaba.cola.extension.Extension;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.extensions.origindata.request.ContentOriginDataRequestExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextContent;
import com.tmall.txcs.gs.framework.model.meta.ContentMetaInfo;
import com.tmall.txcs.gs.framework.model.meta.ContentRecommendMetaInfo;
import com.tmall.txcs.gs.model.biz.context.LocParams;
import com.tmall.txcs.gs.model.biz.context.PageInfoDO;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.txcs.gs.model.constant.RpmContants;
import com.tmall.txcs.gs.model.spi.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.common.RequestKeyConstantApp;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.TppItemBusinessTypeEnum;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.utils.RenderLangUtil;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.Enviroment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_CONTENT)
@Service
public class FirstScreenMindContentOriginDataRequestExPt implements ContentOriginDataRequestExtPt {
    Logger LOGGER = LoggerFactory.getLogger(FirstScreenMindContentOriginDataRequestExPt.class);

    @Autowired
    TacLogger tacLogger;

    @Override
    public RecommendRequest process(SgFrameworkContextContent sgFrameworkContextContent) {

        boolean isMind = isMind(sgFrameworkContextContent.getRequestParams());

        ContentRecommendMetaInfo contentRecommendMetaInfo = Optional.of(sgFrameworkContextContent)
            .map(SgFrameworkContextContent::getContentMetaInfo)
            .map(ContentMetaInfo::getContentRecommendMetaInfo)
            .orElse(null);
        tacLogger.info(
            "****FirstScreenMindContentOriginDataRequestExPt sgFrameworkContextContent***:" + sgFrameworkContextContent
                .toString());

        Map<String, Object> requestParams = sgFrameworkContextContent.getRequestParams();
        if (requestParams == null || requestParams.isEmpty()) {
            return null;
        }
        List<Long> contentSetIdList = Lists.newArrayList();
        if(isItemFeeds(requestParams)){
            contentSetIdList = getContentSetIdListItemFeeds(requestParams);
        }else {
            contentSetIdList = getContentSetIdList(requestParams);
        }


        if (isMind) {
            RecommendRequest tppRequest = new RecommendRequest();
            Map<String, String> params = Maps.newHashMap();
            tppRequest.setParams(params);
            tppRequest.setLogResult(true);
            tppRequest.setUserId(Optional.of(sgFrameworkContextContent).map(SgFrameworkContext::getUserDO)
                .map(UserDO::getUserId).orElse(0L));
            tppRequest.setAppId(25379L);
            contentSetIdList = getMindContentSetIdList(requestParams);// 新版本的内容集id
            List<String> newContentSetIdList = contentSetIdList.stream().map(id -> "intelligentCombinationItems_" + id)
                .collect(
                    Collectors.toList());
            params.put("sceneSet", Joiner.on(",").join(newContentSetIdList));
            params.put("commerce", "B2C");
            params.put("regionCode", Joiner.on(",").join(Optional.ofNullable(sgFrameworkContextContent).map(
                SgFrameworkContext::getLocParams).map(LocParams::getLogicIdByPriority).orElse(Lists.newArrayList())));
            if(params.get("regionCode") == null || "".equals(params.get("regionCode") )){
                params.put("regionCode","107");
            }
            params.put("smAreaId", Optional.ofNullable(sgFrameworkContextContent).map(SgFrameworkContext::getLocParams)
                .map(
                    LocParams::getSmAreaId).orElse(0L).toString());
            params.put("exposureDataUserId", Optional.ofNullable(sgFrameworkContextContent).map(
                SgFrameworkContext::getUserDO).map(UserDO::getCna).orElse(""));
            params.put("pageSize", Optional.ofNullable(sgFrameworkContextContent).map(
                SgFrameworkContext::getUserPageInfo).map(
                PageInfoDO::getPageSize).orElse(20).toString());
            Integer index = Optional.ofNullable(sgFrameworkContextContent).map(SgFrameworkContext::getUserPageInfo).map(
                PageInfoDO::getIndex).orElse(0);
            params.put("index", String.valueOf(index));
            params.put("isFirstPage", index > 0 ? "false" : "true");
            if (Enviroment.PRE.equals(RpmContants.enviroment)) {
                params.put("_devEnv_", "1");
            }
            tacLogger.info("****FirstScreenMindContentOriginDataRequestExPt tppRequest***:" + tppRequest.toString());
            LOGGER.info("****FirstScreenMindContentOriginDataRequestExPt tppRequest***:" + tppRequest.toString());
            return tppRequest;
        }



        RecommendRequest tppRequest = new RecommendRequest();
        Map<String, String> params = Maps.newHashMap();

        Long oneHour = Optional.of(sgFrameworkContextContent).map(SgFrameworkContext::getLocParams).map(LocParams::getRt1HourStoreId).orElse(0L);
        Long halfDay = Optional.of(sgFrameworkContextContent).map(SgFrameworkContext::getLocParams).map(LocParams::getRtHalfDayStoreId).orElse(0L);

        List<String> itemBusinessTypeList = Lists.newArrayList(TppItemBusinessTypeEnum.B2C.getType());
        if (oneHour > 0) {
            itemBusinessTypeList.add(TppItemBusinessTypeEnum.OneHour.getType());
            params.put("rt1HourStoreId", RenderLangUtil.safeString(oneHour));
        } else if (halfDay >= 0){
            itemBusinessTypeList.add(TppItemBusinessTypeEnum.HalfDay.getType());
            params.put("rtHalfDayStoreId", RenderLangUtil.safeString(halfDay));
        }
        params.put("itemBusinessType", Joiner.on(",").join(itemBusinessTypeList));




        params.put("contentSetIdList", Joiner.on(",").join(contentSetIdList));

        // 新版本的内容集id
        List<String> newContentSetIdList = contentSetIdList.stream().map(id -> "intelligentCombinationItems_" + id)
            .collect(Collectors.toList());
        params.put("sceneSet", Joiner.on(",").join(newContentSetIdList));

        params.put("pageSize", Optional.ofNullable(sgFrameworkContextContent).map(SgFrameworkContext::getUserPageInfo)
            .map(PageInfoDO::getPageSize).orElse(4).toString());
        //逛超市TPP内容召回每个内容挂载的商品数量
        params.put("itemCountPerContent", "10");
        params.put("contentType", "7");
        params.put("contentSetSource", "intelligentCombinationItems");
        //未登陆用户唯一身份ID，确认是否必须
        params.put("exposureDataUserId", Optional.ofNullable(sgFrameworkContextContent).map(
            SgFrameworkContext::getUserDO).map(UserDO::getCna).orElse(""));
        params.put("smAreaId", Optional.ofNullable(sgFrameworkContextContent).map(SgFrameworkContext::getLocParams).map(
            LocParams::getSmAreaId).orElse(0L).toString());
        params.put("logicAreaId", Joiner.on(",").join(Optional.ofNullable(sgFrameworkContextContent).map(
            SgFrameworkContext::getLocParams).map(LocParams::getLogicIdByPriority).orElse(
            Lists.newArrayList())));
        Integer index = Optional.ofNullable(sgFrameworkContextContent).map(SgFrameworkContext::getUserPageInfo).map(
            PageInfoDO::getIndex).orElse(0);
        params.put("isFirstPage", index > 0 ? "false" : "true");

        if (contentRecommendMetaInfo != null) {
            contentRecommendMetaInfo.setUseRecommendSpiV2(false);
        }
        tppRequest.setAppId(25409L);
        tppRequest.setParams(params);
        tppRequest.setLogResult(true);
        tppRequest.setUserId(Optional.ofNullable(sgFrameworkContextContent).map(SgFrameworkContext::getUserDO)
            .map(UserDO::getUserId).orElse(0L));
        tacLogger.info("****FirstScreenMindContentOriginDataRequestExPt tppRequest***:" + tppRequest.toString());
        LOGGER.info("****FirstScreenMindContentOriginDataRequestExPt tppRequest***:" + tppRequest.toString());
        return tppRequest;
    }

    private boolean isMind(Map<String, Object> requestParams) {

        Long mindContentCode = MapUtil.getLongWithDefault(requestParams,
            RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_MIND, 0L);

        if (mindContentCode <= 0) {
            return false;
        }

        Object isFixPositionBanner = requestParams.get("isFixPositionBanner");

        Boolean isMind = false;
        if (isFixPositionBanner == null || "".equals(isFixPositionBanner)) {
            isMind = true;
        } else if (isFixPositionBanner instanceof Boolean) {
            isMind = (Boolean)isFixPositionBanner;
        } else if (isFixPositionBanner instanceof String && "true".equals(isFixPositionBanner)) {
            isMind = true;
        }
        if(isItemFeeds(requestParams)){
            isMind = false;
        }
        return isMind;
    }
    private boolean isItemFeeds(Map<String, Object> requestParams){
        Boolean isItemFeeds = false;
        String requestFrom = MapUtil.getStringWithDefault(requestParams,"requestFrom","");
        if("itemFeeds".equals(requestFrom)){
            isItemFeeds = true;
        }
        return isItemFeeds;
    }

    private List<Long> getMindContentSetIdList(Map<String, Object> requestParams) {

        List<Long> result = Lists.newArrayList();
        result.add(
            MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_MIND, 0L));

        return result.stream().filter(contentSetId -> contentSetId > 0).collect(Collectors.toList());
    }

    private List<Long> getContentSetIdList(Map<String, Object> requestParams) {

        List<Long> result = Lists.newArrayList();
        result.add(MapUtil
            .getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_RANKING, 0L));
        result.add(
            MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_RECIPE, 0L));
        result.add(
            MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_BRAND, 0L));
        result.add(
            MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_MIND, 0L));
        result.add(
            MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_O2O, 0L));
        result.add(
            MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_B2C, 0L));

        return result.stream().filter(contentSetId -> contentSetId > 0).collect(Collectors.toList());
    }

    /**
     * 承接页不出菜谱
     * @param requestParams
     * @return
     */
    private List<Long> getContentSetIdListItemFeeds(Map<String, Object> requestParams) {

        List<Long> result = Lists.newArrayList();
        result.add(MapUtil
            .getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_RANKING, 0L));
        /*result.add(
            MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_RECIPE, 0L));*/
        result.add(
            MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_BRAND, 0L));
        result.add(
            MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_MIND, 0L));
        result.add(
            MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_O2O, 0L));
        result.add(
            MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_B2C, 0L));

        return result.stream().filter(contentSetId -> contentSetId > 0).collect(Collectors.toList());
    }
}
