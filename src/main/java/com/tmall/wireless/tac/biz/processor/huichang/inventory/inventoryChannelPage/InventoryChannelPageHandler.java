package com.tmall.wireless.tac.biz.processor.huichang.inventory.inventoryChannelPage;
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

import java.util.List;

/**
 *  清单会场
 */
@Component
public class InventoryChannelPageHandler extends TacReactiveHandler4Ald {

    @Autowired
    HallCommonContentRequestProxy hallCommonContentRequestProxy;

    @Override
    public Flowable<TacResult<List<GeneralItem>>> executeFlowable(RequestContext4Ald requestContext4Ald) throws Exception {
        BizScenario bizScenario = BizScenario.valueOf(HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
                HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
                HallScenarioConstant.HALL_SCENARIO_SCENARIO_INVENTORY_CHANNEL_PAGE);
        bizScenario.addProducePackage("huichang");
        return hallCommonContentRequestProxy.recommend(requestContext4Ald, bizScenario);
    }
}

