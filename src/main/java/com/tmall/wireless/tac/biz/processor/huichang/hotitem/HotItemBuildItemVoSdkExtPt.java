package com.tmall.wireless.tac.biz.processor.huichang.hotitem;

import com.alibaba.fastjson.JSON;

import com.tmall.tcls.gs.sdk.biz.extensions.item.vo.DefaultBuildItemVoSdkExtPt;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.framework.extensions.item.vo.BuildItemVoRequest;
import com.tmall.tcls.gs.sdk.framework.extensions.item.vo.BuildItemVoSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.Response;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wangguohui
 */
@SdkExtension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
    useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
    scenario = HallScenarioConstant.HALL_SCENARIO_HOT_ITEM)
public class HotItemBuildItemVoSdkExtPt extends DefaultBuildItemVoSdkExtPt implements BuildItemVoSdkExtPt {

    Logger logger = LoggerFactory.getLogger(HotItemBuildItemVoSdkExtPt.class);

    @Override
    public Response<ItemEntityVO> process(BuildItemVoRequest buildItemVoRequest) {
        logger.error("-----------HotItemBuildItemVoSdkExtPt.start.------------");
        Response<ItemEntityVO> process = super.process(buildItemVoRequest);
        logger.error("-----------HotItemBuildItemVoSdkExtPt.super.process.result:{}", JSON.toJSONString(process));
        return process;
    }
}
