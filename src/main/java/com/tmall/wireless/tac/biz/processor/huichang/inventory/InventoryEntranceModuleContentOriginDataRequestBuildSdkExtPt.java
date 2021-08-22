package com.tmall.wireless.tac.biz.processor.huichang.inventory;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;

import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextContent;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallCommonAldConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.util.UrlUtils;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * 请求tpp数据的入参
 * @author wangguohui
 */
@SdkExtension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
    useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
    scenario = HallScenarioConstant.HALL_SCENARIO_SCENARIO_INVENTORY_ENTRANCE_MODULE)
public class InventoryEntranceModuleContentOriginDataRequestBuildSdkExtPt extends Register implements ContentOriginDataRequestBuildSdkExtPt {

    Logger LOGGER = LoggerFactory.getLogger(InventoryEntranceModuleContentOriginDataRequestBuildSdkExtPt.class);


    public static final Long SCENE_RECOMMEND_APPID = 26563L;

    @Override
    public RecommendRequest process(SgFrameworkContextContent sgFrameworkContextContent) {

        Context tacContext = sgFrameworkContextContent.getTacContext();
        RequestContext4Ald requestContext4Ald = (RequestContext4Ald)tacContext;
        Map<String, Object> aldParam = requestContext4Ald.getAldParam();//对应requestItem
        Map<String, Object> aldContext = requestContext4Ald.getAldContext();//对应solutionContext

        Map<String, String> tppRequestParams = new HashMap<>();

        Object aldCurrentResId = aldContext.get(HallCommonAldConstant.ALD_CURRENT_RES_ID);
        Object userId = aldContext.get(HallCommonAldConstant.USER_ID);
        tppRequestParams.put("resourceId", String.valueOf(aldCurrentResId));
        BizScenario bizScenario = sgFrameworkContextContent.getBizScenario();
        String uniqueIdentity = bizScenario.getUniqueIdentity();
        tppRequestParams.put("uniqueIdentity", uniqueIdentity);
        String urlParamsByMap = UrlUtils.getUrlParamsByMap(tppRequestParams);
        LOGGER.error("urlParamsByMap:{}", JSON.toJSONString(urlParamsByMap));

        RecommendRequest recommendRequest = new RecommendRequest();
        recommendRequest.setParams(tppRequestParams);
        recommendRequest.setLogResult(true);
        recommendRequest.setUserId(Long.valueOf(String.valueOf(userId)));
        recommendRequest.setAppId(SCENE_RECOMMEND_APPID);

        buildTppParams(tppRequestParams, aldParam);

        return recommendRequest;
    }
    //https://tui.taobao.com/recommend?appid=26563&sceneSet=intelligentCombinationItems_182009&commerce=B2C
    // &regionCode=107&smAreaId=330110&pageSize=10
    private void buildTppParams(Map<String, String> params, Map<String, Object> aldParam){
        params.put("index", "0");
        params.put("pageSize", "10"); //
        params.put("commerce","B2C");
        params.put("smAreaId", "330110");
        params.put("regionCode", "107");
        Object sceneSet = aldParam.get("sceneSet");
        String sceneSetId = String.valueOf(sceneSet);
        params.put("sceneSet", sceneSetId); // 场景集id
        params.put("appId", String.valueOf(SCENE_RECOMMEND_APPID));
    }

}
