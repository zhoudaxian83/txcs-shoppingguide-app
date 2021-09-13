package com.tmall.wireless.tac.biz.processor.huichang.inventory.InventoryEntranceModule;

import java.util.List;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.alibaba.fastjson.JSON;

import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.biz.processor.huichang.service.HallCommonContentRequestProxy;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import com.tmall.wireless.tac.client.handler.TacReactiveHandler4Ald;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 清单会场入口模块
 * 1、拿到运营填写的所有数据
 * 2、去请求tpp做个性化
 * 3、拿到个性化结果组装
 * 4、查询
 */
@Component
public class InventoryEntranceModuleHandler extends TacReactiveHandler4Ald {

    Logger logger = LoggerFactory.getLogger(InventoryEntranceModuleHandler.class);
    @Autowired
    TacLogger tacLogger;

    @Autowired
    HallCommonContentRequestProxy hallCommonContentRequestProxy;

    @Override
    public Flowable<TacResult<List<GeneralItem>>> executeFlowable(RequestContext4Ald requestContext4Ald) throws Exception {
        try {
            tacLogger.info("tacLogger.InventoryEntranceModuleHandler.statr");
            logger.warn("LOGGER.InventoryEntranceModuleHandler.statr", JSON.toJSONString(requestContext4Ald));
            BizScenario bizScenario = BizScenario.valueOf(HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
                HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
                HallScenarioConstant.HALL_SCENARIO_SCENARIO_INVENTORY_ENTRANCE_MODULE);
            bizScenario.addProducePackage(HallScenarioConstant.HALL_CONTENT_SDK_PACKAGE);
            return hallCommonContentRequestProxy.recommend(requestContext4Ald, bizScenario);
        }catch (Exception e){
            logger.error("LOGGER.InventoryEntranceModuleHandler.error.", e);
            return Flowable.error(e);
        }

    }
}
