package com.tmall.wireless.tac.biz.processor.huichang.inventory.inventoryChannelPage;

import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.tmall.tcls.gs.sdk.biz.uti.MapUtil;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.constant.RequestKeyConstant;
import com.tmall.tcls.gs.sdk.framework.model.context.LocParams;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextContent;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallCommonAldConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.utils.PageUrlUtil;
import com.tmall.wireless.tac.biz.processor.huichang.common.utils.ParseCsa;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * Tpp请求场景，入参组装
 */
@SdkExtension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
        useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
        scenario = HallScenarioConstant.HALL_SCENARIO_SCENARIO_INVENTORY_CHANNEL_PAGE)
public class InventoryChannelPageContentOriginDataRequestBuildSdkExtPt extends Register implements ContentOriginDataRequestBuildSdkExtPt {
    @Autowired
    TacLogger tacLogger;

    private static final Long DefaultSmAreaId = 310100L;
    private static final Long DefaultLogAreaId = 107L;
    private static final String SCENE_SET_PREFIX = "intelligentCombinationItems_";
    public static final Long APPID = 26563L;  //Todo likunlin
    private static final Long DefaultUserId = 0L; // Todo likunlin
    private static final int PAGE_SIZE = 2; //Todo likunlin
    @Override
    public RecommendRequest process(SgFrameworkContextContent sgFrameworkContextContent) {
        tacLogger.debug("扩展点InventoryChannelPageContentOriginDataRequestBuildSdkExtPt");
        Context context = sgFrameworkContextContent.getTacContext();
        RequestContext4Ald requestContext4Ald = (RequestContext4Ald) context;
        Map<String, Object> aldParams = requestContext4Ald.getAldParam();
        Map<String, Object> aldContext = requestContext4Ald.getAldContext();
        Map<String, String> params = Maps.newHashMap();
        tacLogger.debug("aldParams: " + JSONObject.toJSONString(aldParams));

        String index = PageUrlUtil.getParamFromCurPageUrl(aldParams, "index", tacLogger);
        if(StringUtils.isNotBlank(index)) {
            params.put("index", index); // Todo
        } else {
            params.put("index", String.valueOf(sgFrameworkContextContent.getCommonUserParams().getUserPageInfo().getIndex())); // Todo
        }

        params.put("pageSize", String.valueOf(PAGE_SIZE)); // Todo
        Long smAreaId = MapUtil.getLongWithDefault(aldParams, RequestKeyConstant.SMAREAID, 330100L);
        params.put("smAreaId", String.valueOf(smAreaId));

        LocParams locParams = ParseCsa.parseCsaObj(aldParams.get(RequestKeyConstant.USER_PARAMS_KEY_CSA), smAreaId);

        params.put("regionCode", String.valueOf(Optional.ofNullable(locParams.getRegionCode()).orElse(DefaultLogAreaId)));

        String locType = PageUrlUtil.getParamFromCurPageUrl(aldParams, "locType", tacLogger);
        if("B2C".equals(locType) || locType == null){
            params.put("commerce","B2C");
        }else {
            params.put("commerce","O2O");
            if (Optional.of(locParams.getRt1HourStoreId()).orElse(0L) > 0){
                params.put("rtOneHourStoreId", String.valueOf(sgFrameworkContextContent.getCommonUserParams().getLocParams().getRt1HourStoreId()));
            }else if(Optional.of(locParams.getRtHalfDayStoreId()).orElse(0L) > 0){
                params.put("rtHalfDayStoreId", String.valueOf(sgFrameworkContextContent.getCommonUserParams().getLocParams().getRtHalfDayStoreId()));
            }
        }

        String sceneSetId = PageUrlUtil.getParamFromCurPageUrl(aldParams, "sceneSet", tacLogger); // Todo likunlin
        if(!sceneSetId.contains(SCENE_SET_PREFIX)) {
            sceneSetId = SCENE_SET_PREFIX + sceneSetId;
        }
        params.put("sceneSet", sceneSetId); // 场景集id

        String sceneExclude = PageUrlUtil.getParamFromCurPageUrl(aldParams, "filter", tacLogger); // Todo likunlin
        if(StringUtils.isNotBlank(sceneExclude)) {
            params.put("sceneExclude", sceneExclude); // 过滤的场景
        }

        String sceneTop = PageUrlUtil.getParamFromCurPageUrl(aldParams, "sceneTop", tacLogger); // Todo likunlin
        if(StringUtils.isNotBlank(sceneTop)) {
            params.put("sceneTop", sceneTop); // 置顶的场景
        }

        RecommendRequest recommendRequest = new RecommendRequest();
        recommendRequest.setAppId(APPID);
        recommendRequest.setUserId(MapUtil.getLongWithDefault(aldContext, HallCommonAldConstant.USER_ID, 0L));
        recommendRequest.setParams(params);
        recommendRequest.setLogResult(true);
        tacLogger.debug("Tpp请求参数是：" + JSONObject.toJSONString(recommendRequest));
        return recommendRequest;

    }
}
