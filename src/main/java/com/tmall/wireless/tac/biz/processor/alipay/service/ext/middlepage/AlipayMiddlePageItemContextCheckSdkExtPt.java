package com.tmall.wireless.tac.biz.processor.alipay.service.ext.middlepage;

import com.alipay.recmixer.common.service.facade.model.MixerCollectRecRequest;
import com.alipay.tradecsa.common.service.spi.request.MiddlePageSPIRequest;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.contextcheck.ContextCheckResult;
import com.tmall.tcls.gs.sdk.framework.extensions.item.contextcheck.ItemContextCheckSdkExtPt;
import com.tmall.wireless.tac.biz.processor.alipay.AlipayMiddlePageHandler;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.domain.Context;

import java.util.Optional;


@SdkExtension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C
        , scenario = ScenarioConstantApp.SCENARIO_ALI_PAY_MIDDLE_PAGE
)
public class AlipayMiddlePageItemContextCheckSdkExtPt extends Register implements ItemContextCheckSdkExtPt {
    @Override
    public ContextCheckResult process(Context context) {
        ContextCheckResult contextCheckResult = new ContextCheckResult();
        Object param = Optional.of(context).map(Context::getParams).map(m -> m.get(AlipayMiddlePageHandler.PARAM_KEY)).orElse(null);

        if (param instanceof MiddlePageSPIRequest) {
            contextCheckResult.setSuccess(true);
            return contextCheckResult;
        }
        contextCheckResult.setErrorMsg("MiddlePageSPIRequest is null");
        contextCheckResult.setSuccess(false);
        return contextCheckResult;
    }
}
