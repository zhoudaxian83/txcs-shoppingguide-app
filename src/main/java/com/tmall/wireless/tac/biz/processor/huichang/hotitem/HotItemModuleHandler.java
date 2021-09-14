package com.tmall.wireless.tac.biz.processor.huichang.hotitem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;

import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallCommonAldConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.biz.processor.huichang.service.HallCommonItemRequestProxy;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import com.tmall.wireless.tac.client.handler.TacReactiveHandler4Ald;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class HotItemModuleHandler extends TacReactiveHandler4Ald {

    Logger logger = LoggerFactory.getLogger(HotItemModuleHandler.class);

    private static final String defaultLocType = "B2C";

    @Autowired
    TacLogger tacLogger;

    @Autowired
    HallCommonItemRequestProxy hallCommonItemRequestProxy;

    @Override
    public Flowable<TacResult<List<GeneralItem>>> executeFlowable(RequestContext4Ald requestContext4Ald) throws Exception {

        BizScenario bizScenario = BizScenario.valueOf(HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
            HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
            HallScenarioConstant.HALL_SCENARIO_HOT_ITEM);
        bizScenario.addProducePackage(HallScenarioConstant.HALL_ITEM_SDK_PACKAGE);
        Flowable<TacResult<List<GeneralItem>>> itemVOs = hallCommonItemRequestProxy.recommend(requestContext4Ald, bizScenario);
        return itemVOs;


        //Map<String, Object> aldContext = requestContext4Ald.getAldContext();
        //Map<String, Object> aldParam = requestContext4Ald.getAldParam();
        //Object aldStaticData  = aldContext.get(HallCommonAldConstant.STATIC_SCHEDULE_DATA);
        //if(aldStaticData == null){
        //    throw new Exception("静态数据获取为空");
        //}
        //List<Map<String, Object>> aldStaticDataList = (List<Map<String, Object>>)aldStaticData;
        //String locType = defaultLocType;
        //String tacParams = MapUtil.getStringWithDefault(aldParam, "tacParams", "");
        //if(StringUtils.isNotBlank(tacParams)){
        //    JSONObject tacParamsMap = JSON.parseObject(tacParams);
        //    locType = Optional.ofNullable(tacParamsMap.getString(HallCommonAldConstant.LOC_TYPE)).orElse(defaultLocType);
        //}
        //
        //
        //return null;
    }

    /**
     * 校验数据准确性
     */
    private void staticDataCheck(List<Map<String, Object>> aldStaticDataList){

    }

    //https://tui.taobao.com/recommend?appid=27753&pageSize=2&index=0&itemSets
    // =crm_378428&commerce=B2C&smAreaId=330200&_devEnv_=0&regionCode=107
    // &exposureDataUserId=FYRsGO9rTyUCAXWISzVJIRez
    // &itemAndIndustry=649361494634:1100:1;651103243384:1200:1
    private Map<String, String> buildTppParams(Map<String, Object> aldContext, Map<String, Object> aldParam){
        Map<String, String> params = new HashMap<>();
        Object aldStaticData  = aldContext.get(HallCommonAldConstant.STATIC_SCHEDULE_DATA);
        List<Map<String, Object>> aldStaticDataList = (List<Map<String, Object>>)aldStaticData;
        params.put("index", "0");
        params.put("pageSize", "200");
        params.put("pageSize", "200");
        return null;
    }

}
