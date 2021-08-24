package com.tmall.wireless.tac.biz.processor.huichang.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;

import com.tmall.tcls.gs.sdk.biz.uti.MapUtil;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.constant.RequestKeyConstant;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextContent;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallCommonAldConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.util.UrlUtils;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.dataservice.TacOptLogger;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 请求tpp数据的入参
 *
 * @author wangguohui
 */
@SdkExtension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
    useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
    scenario = HallScenarioConstant.HALL_SCENARIO_SCENARIO_INVENTORY_ENTRANCE_MODULE)
public class InventoryEntranceModuleContentOriginDataRequestBuildSdkExtPt extends Register
    implements ContentOriginDataRequestBuildSdkExtPt {

    @Autowired
    TacLogger tacLogger;

    @Autowired
    private TacOptLogger tacOptLogger;

    Logger LOGGER = LoggerFactory.getLogger(InventoryEntranceModuleContentOriginDataRequestBuildSdkExtPt.class);

    public static final Long SCENE_RECOMMEND_APPID = 27401L;

    private static final String prex_sceneSet = "intelligentCombinationItems_";


    @Override
    public RecommendRequest process(SgFrameworkContextContent sgFrameworkContextContent) {
        tacLogger.debug("tacLogger.InventoryEntranceModuleContentOriginDataRequestBuildSdkExtPt.start");
        tacOptLogger.debug("tacOptLogger.InventoryEntranceModuleContentOriginDataRequestBuildSdkExtPt.start");

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


        RecommendRequest recommendRequest = new RecommendRequest();
        recommendRequest.setParams(tppRequestParams);
        recommendRequest.setLogResult(true);
        recommendRequest.setUserId(Long.valueOf(String.valueOf(userId)));
        recommendRequest.setAppId(SCENE_RECOMMEND_APPID);

        Map<String, Object> userParams = sgFrameworkContextContent.getUserParams();
        buildTppParams(tppRequestParams, aldParam, aldContext, userParams);
        String urlParamsByMap = UrlUtils.getUrlParamsByMap(tppRequestParams);
        tacLogger.debug("urlParamsByMap:" + JSON.toJSONString(urlParamsByMap));
        return recommendRequest;
    }

    //http://tuipre.taobao.com/recommend?appid=27401&index=0&pageSize=3&commerce=B2C&smAreaId=330110
    // &sceneSet=intelligentCombinationItems_155012,intelligentCombinationItems_162002,
    // intelligentCombinationItems_153017&regionCode=107
    private void buildTppParams(Map<String, String> params, Map<String, Object> aldParam,
        Map<String, Object> aldContext, Map<String, Object> userParams) {
        params.put("index", "0");
        params.put("pageSize", "2"); //

        String smAreaId = MapUtil.getStringWithDefault(aldParam, HallCommonAldConstant.SM_AREAID, "330100");
        params.put("smAreaId", smAreaId);

        String logicAreaId = MapUtil.getStringWithDefault(aldParam, HallCommonAldConstant.LOGIC_AREA_ID, "107");
        params.put("regionCode", logicAreaId);

        String locType = MapUtil.getStringWithDefault(aldParam, HallCommonAldConstant.LOC_TYPE, "B2C");
        params.put("commerce", locType);

        Object staticScheduleData = aldContext.get(HallCommonAldConstant.STATIC_SCHEDULE_DATA);
        if(staticScheduleData == null){
            //todo 雾列 需要处理
        }
        List<Map<String, Object>> staticScheduleDataList = (List<Map<String, Object>>)staticScheduleData;
        List<Map<String, String>> dealStaticDataList = new ArrayList<>();
        StringBuilder contentSetBuilder = new StringBuilder();
        for(Map<String, Object> data : staticScheduleDataList){
            Map<String, String> dataMap = new HashMap<>();
            String contentSetId = MapUtil.getStringWithDefault(data, "contentSetId", "");
            contentSetBuilder.append(prex_sceneSet + contentSetId).append(",");
            String contentSetTitle = MapUtil.getStringWithDefault(data, "contentSetTitle", "");
            String contentSetSubTitle = MapUtil.getStringWithDefault(data, "contentSetSubTitle", "");
            dataMap.put("contentSetId", contentSetId);
            dataMap.put("contentSetTitle", contentSetTitle);
            dataMap.put("contentSetSubTitle", contentSetSubTitle);
            dealStaticDataList.add(dataMap);
        }

        params.put("sceneSet", contentSetBuilder.toString()); // 场景集id
        params.put("appId", String.valueOf(SCENE_RECOMMEND_APPID));

        //把处理好的静态数从新设置一下，后面还需要
        userParams.put("dealStaticDataList", JSON.toJSONString(dealStaticDataList));

    }

}
