package com.tmall.wireless.tac.biz.processor.huichang.common.contentextpt;

import com.alibaba.cola.extension.Extension;

import com.tmall.tcls.gs.sdk.framework.extensions.content.context.ContentContextCheckSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.item.contextcheck.ContextCheckResult;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.client.domain.Context;
import org.springframework.stereotype.Service;

/**
 * 会场通用上下文校验扩展点
 *
 * @author wangguohui
 */
@Extension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
    useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
    scenario = HallScenarioConstant.HALL_SCENARIO_SCENARIO)
@Service
public class HallCommonContentContextCheckSdkExtPt implements ContentContextCheckSdkExtPt {
    @Override
    public ContextCheckResult process(Context context) {
        ContextCheckResult contextCheckResult = new ContextCheckResult();
        contextCheckResult.setSuccess(true);
        return contextCheckResult;
    }
}
