package com.tmall.wireless.tac.biz.processor.huichang.common.contentextpt;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.alibaba.cola.extension.Extension;

import com.tmall.tcls.gs.sdk.biz.uti.MapUtil;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkPackage;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.constant.RequestKeyConstant;
import com.tmall.tcls.gs.sdk.framework.model.context.CommonUserParams;
import com.tmall.tcls.gs.sdk.framework.model.context.LocParams;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextContent;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallCommonAldConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * 请求tpp数据的公用入参
 * 其他微服务继承使用
 * 注意！！！！！ 继承后，需要要设置recommendRequest.setAppId()
 * !!!!!!!!!!!!!请求tpp的参数需要重新添加pageSize和index
 * @author wangguohui
 */
@Extension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
    useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
    scenario = HallScenarioConstant.HALL_SCENARIO_SCENARIO)
public class HallCommonContentOriginDataRequestBuildSdkExtPt extends Register implements ContentOriginDataRequestBuildSdkExtPt {


    private static final Long DEFAULT_SM_AREAID = 310100L;
    private static final Long DEFAULT_LOG_AREAID = 107L;

    Logger LOGGER = LoggerFactory.getLogger(HallCommonContentOriginDataRequestBuildSdkExtPt.class);
    @Autowired
    TacLogger tacLogger;

    @Override
    public RecommendRequest process(SgFrameworkContextContent sgFrameworkContextContent) {

        CommonUserParams commonUserParams = sgFrameworkContextContent.getCommonUserParams();
        Context tacContext = sgFrameworkContextContent.getTacContext();
        RequestContext4Ald requestContext4Ald = (RequestContext4Ald)tacContext;
        Map<String, Object> aldParams = requestContext4Ald.getAldParam();//对应requestItem
        Map<String, Object> aldContext = requestContext4Ald.getAldContext();//对应solutionContext

        Map<String, String> tppRequestParams = new HashMap<>();

        Object aldCurrentResId = aldContext.get(HallCommonAldConstant.ALD_CURRENT_RES_ID);
        Object userId = aldContext.get(HallCommonAldConstant.USER_ID);
        tppRequestParams.put("resourceId", String.valueOf(aldCurrentResId));
        BizScenario bizScenario = sgFrameworkContextContent.getBizScenario();
        String uniqueIdentity = bizScenario.getUniqueIdentity();
        tppRequestParams.put("uniqueIdentity", uniqueIdentity);

        Long smAreaId = MapUtil.getLongWithDefault(aldParams, RequestKeyConstant.SMAREAID, DEFAULT_SM_AREAID);
        tppRequestParams.put("smAreaId", String.valueOf(smAreaId));

        LocParams locParams = commonUserParams.getLocParams();
        tppRequestParams.put("regionCode", Optional.ofNullable(locParams).map(locParams1 -> locParams1.getRegionCode()).map(String::valueOf).orElse(String.valueOf(DEFAULT_LOG_AREAID)));


        //先从绑定的流程模板参数中拿，如果拿不到，代表是二跳页面的模块，这个时候从URL里面获取，如果还获取不到，则默认是B2C
        String locType = MapUtil.getStringWithDefault(aldParams, HallCommonAldConstant.LOC_TYPE, "B2C");
        if("B2C".equals(locType) || locType == null){
            tppRequestParams.put("commerce","B2C");
        }else {
            tppRequestParams.put("commerce","O2O");
            if (Optional.ofNullable(locParams).map(locParams1 -> locParams1.getRt1HourStoreId()).orElse(0L) > 0){
                tppRequestParams.put("rtOneHourStoreId", String.valueOf(locParams.getRt1HourStoreId()));
            }else if(Optional.ofNullable(locParams).map(locParams1 -> locParams1.getRtHalfDayStoreId()).orElse(0L) > 0){
                tppRequestParams.put("rtHalfDayStoreId", String.valueOf(locParams.getRtHalfDayStoreId()));
            }
        }


        RecommendRequest recommendRequest = new RecommendRequest();
        recommendRequest.setParams(tppRequestParams);
        recommendRequest.setLogResult(true);
        recommendRequest.setUserId(Long.valueOf(String.valueOf(userId)));

        return recommendRequest;
    }



}
