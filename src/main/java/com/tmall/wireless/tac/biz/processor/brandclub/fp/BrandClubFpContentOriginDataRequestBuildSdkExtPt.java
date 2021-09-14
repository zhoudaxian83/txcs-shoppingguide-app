package com.tmall.wireless.tac.biz.processor.brandclub.fp;

import com.alibaba.fastjson.JSONArray;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkPackage;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.*;
import com.tmall.txcs.gs.model.constant.RpmContants;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.common.PackageNameKey;
import com.tmall.wireless.tac.biz.processor.common.RequestKeyConstantApp;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.TppItemBusinessTypeEnum;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.utils.RenderLangUtil;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.Enviroment;
import org.apache.zookeeper.Op;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@SdkExtension(
        bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.BRAND_CLUB_FP
)
@Service
public class BrandClubFpContentOriginDataRequestBuildSdkExtPt extends Register implements ContentOriginDataRequestBuildSdkExtPt {

    @Autowired
    BrandContentSetIdService brandContentSetIdService;

    @Override
    public RecommendRequest process(SgFrameworkContextContent sgFrameworkContextContent) {

        Long brandId = Optional.of(sgFrameworkContextContent)
                .map(SgFrameworkContext::getTacContext)
                .map(Context::getParams)
                .map(m -> m.get("brandId"))
                .map(Object::toString).map(Long::valueOf).orElse(0L);
        Map<String, Map<String, Object>> groupAndBrandMapping =
                brandContentSetIdService.getGroupAndBrandMapping(Lists.newArrayList(brandId));

        JSONArray rankingContentSetIdList = (JSONArray) Optional.of(groupAndBrandMapping).map(m -> m.get("tcls_ugc_scenegroup_mapping_v1_btao_" + brandId)).map(m -> m.get("boardSceneGroupIds")).orElse(new JSONArray());
        JSONArray b2cCommonContentSetIdList = (JSONArray) Optional.of(groupAndBrandMapping).map(m -> m.get("tcls_ugc_scenegroup_mapping_v1_btao_" + brandId)).map(m -> m.get("generalSceneGroupIds")).orElse(new JSONArray());

        Map<String, Object> requestParams = Optional.of(sgFrameworkContextContent)
                .map(SgFrameworkContext::getRequestParams)
                .orElse(null);
        if (requestParams != null) {
            requestParams.put(RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_RANKING, rankingContentSetIdList.stream().findFirst().map(Object::toString).orElse(""));
            requestParams.put(RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_B2C, b2cCommonContentSetIdList.stream().findFirst().map(Object::toString).orElse(""));
        }

        Map<String, Object> stringObjectMap = groupAndBrandMapping.values().stream().findFirst().orElse(Maps.newHashMap());

        List<Integer> contentSetIdList = Lists.newArrayList();
        stringObjectMap.values().stream().forEach(setIdList -> {
            List<Integer> setIds = (List<Integer>) setIdList;
            contentSetIdList.addAll(setIds);
        });
        RecommendRequest tppRequest = new RecommendRequest();
        Map<String, String> params = Maps.newHashMap();
        tppRequest.setParams(params);
        tppRequest.setLogResult(true);
        tppRequest.setUserId(Optional.of(sgFrameworkContextContent).
                map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getUserDO)
                .map(UserDO::getUserId).orElse(0L));
        tppRequest.setAppId(27402L);


        List<String> newContentSetIdList = contentSetIdList.stream().map(id -> "intelligentCombinationItems_" + id)
                .collect(
                        Collectors.toList());
        params.put("sceneSet", Joiner.on(",").join(newContentSetIdList));
        /**心智场景支持O2O场景**/
        Long oneHour = Optional.of(sgFrameworkContextContent).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getLocParams).map(LocParams::getRt1HourStoreId).orElse(0L);
        Long halfDay = Optional.of(sgFrameworkContextContent).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getLocParams).map(LocParams::getRtHalfDayStoreId).orElse(0L);
        List<String> itemBusinessTypeList = Lists.newArrayList(TppItemBusinessTypeEnum.B2C.getType());
        if (oneHour > 0) {
            itemBusinessTypeList.add(TppItemBusinessTypeEnum.O2O.getType());
            params.put("rtOneHourStoreId", RenderLangUtil.safeString(oneHour));
        } else if (halfDay > 0){
            itemBusinessTypeList.add(TppItemBusinessTypeEnum.O2O.getType());
            params.put("rtHalfDayStoreId", RenderLangUtil.safeString(halfDay));
        }
        params.put("commerce", Joiner.on(",").join(itemBusinessTypeList));


        params.put("regionCode", Joiner.on(",").join(Optional.of(sgFrameworkContextContent).map(
                SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getLocParams).map(LocParams::getLogicIdByPriority).orElse(Lists.newArrayList())));
        if (params.get("regionCode") == null || "".equals(params.get("regionCode"))) {
            params.put("regionCode", "107");
        }
        params.put("smAreaId", Optional.of(sgFrameworkContextContent).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getLocParams)
                .map(LocParams::getSmAreaId).orElse(0L).toString());
        params.put("exposureDataUserId", Optional.of(sgFrameworkContextContent).map(SgFrameworkContext::getCommonUserParams)
                .map(CommonUserParams::getUserDO).map(UserDO::getCna).orElse(""));
        String pageSize = params.put("pageSize", Optional.of(sgFrameworkContextContent).map(SgFrameworkContext::getCommonUserParams).map(
                CommonUserParams::getUserPageInfo).map(PageInfoDO::getPageSize).orElse(20).toString());
        Integer index = Optional.of(sgFrameworkContextContent).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getUserPageInfo).map(
                PageInfoDO::getIndex).orElse(0);
        params.put("index", String.valueOf(index));
        params.put("isFirstPage", index > 0 ? "false" : "true");
        if (Enviroment.PRE.equals(RpmContants.enviroment)) {
            params.put("_devEnv_", "1");
        }
        return tppRequest;
    }
}
