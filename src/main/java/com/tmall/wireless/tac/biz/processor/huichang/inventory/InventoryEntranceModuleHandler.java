package com.tmall.wireless.tac.biz.processor.huichang.inventory;

import java.util.List;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;

import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.biz.processor.huichang.service.HallCommonContentRequestProxy;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import com.tmall.wireless.tac.client.handler.TacReactiveHandler4Ald;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 清单会场入口模块
 */
@Component
public class InventoryEntranceModuleHandler extends TacReactiveHandler4Ald {

    @Autowired
    HallCommonContentRequestProxy hallCommonContentRequestProxy;

    @Override
    public Flowable<TacResult<List<GeneralItem>>> executeFlowable(RequestContext4Ald requestContext4Ald) throws Exception {
        BizScenario bizScenario = BizScenario.valueOf(HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
            HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
            HallScenarioConstant.HALL_SCENARIO_SCENARIO_INVENTORY_ENTRANCE_MODULE);
        //bizScenario.addProducePackage("huichang");
        return hallCommonContentRequestProxy.recommend(requestContext4Ald, bizScenario);
    }
}
