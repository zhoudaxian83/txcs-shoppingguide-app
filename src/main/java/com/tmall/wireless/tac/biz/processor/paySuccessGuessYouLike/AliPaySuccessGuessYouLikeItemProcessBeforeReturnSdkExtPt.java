package com.tmall.wireless.tac.biz.processor.paySuccessGuessYouLike;

import com.alibaba.fastjson.JSON;
import com.tmall.tcls.gs.sdk.biz.extensions.item.filter.DefaultItemProcessBeforeReturnSdkExtPt;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.framework.extensions.item.filter.ItemProcessBeforeReturnSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextItem;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.springframework.beans.factory.annotation.Autowired;

@SdkExtension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C
        , scenario = ScenarioConstantApp.PAY_FOR_SUCCESS_GUESS_YOU_LIKE
)
public class AliPaySuccessGuessYouLikeItemProcessBeforeReturnSdkExtPt extends DefaultItemProcessBeforeReturnSdkExtPt
        implements ItemProcessBeforeReturnSdkExtPt {


    @Autowired
    TacLoggerImpl tacLogger;
    @Override
    public SgFrameworkContextItem process(SgFrameworkContextItem sgFrameworkContextItem) {

        tacLogger.info("返回前扩展点处理内容前：" + JSON.toJSONString(sgFrameworkContextItem));
        SgFrameworkContextItem process = super.process(sgFrameworkContextItem);
        tacLogger.info("返回前扩展点处理内容后：" + JSON.toJSONString(process));
        return process;
    }
}
