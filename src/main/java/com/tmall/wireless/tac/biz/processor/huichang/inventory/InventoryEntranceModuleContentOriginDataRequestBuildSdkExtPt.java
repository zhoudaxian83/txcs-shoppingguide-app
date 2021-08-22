package com.tmall.wireless.tac.biz.processor.huichang.inventory;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;

import com.tmall.tcls.gs.sdk.ext.BizScenario;
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
@Extension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
    useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
    scenario = HallScenarioConstant.HALL_SCENARIO_SCENARIO_INVENTORY_ENTRANCE_MODULE)
public class InventoryEntranceModuleContentOriginDataRequestBuildSdkExtPt extends Register implements ContentOriginDataRequestBuildSdkExtPt {

    Logger LOGGER = LoggerFactory.getLogger(InventoryEntranceModuleContentOriginDataRequestBuildSdkExtPt.class);

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
        //TODO 雾列 这个地方怎么扩展
        recommendRequest.setAppId(0L);

        return recommendRequest;
    }



}
