package com.tmall.wireless.tac.biz.processor.guessYourLikeShopCart4;

import com.alibaba.fastjson.JSON;
import com.tmall.tcls.gs.sdk.biz.extensions.item.filter.DefaultItemProcessBeforeReturnSdkExtPt;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.filter.ItemProcessBeforeReturnSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextItem;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created from template by 程斐斐 on 2021-09-29 17:21:34.
 */

@SdkExtension(
        bizId = "supermarket",
        useCase = "b2c",
        scenario = "guessYourLikeShopCart4"
)
public class GuessYourLikeShopCart4ItemProcessBeforeReturnSdkExtPt extends DefaultItemProcessBeforeReturnSdkExtPt implements ItemProcessBeforeReturnSdkExtPt {

    @Autowired
    TacLoggerImpl tacLogger;
    @Override
    public SgFrameworkContextItem process(SgFrameworkContextItem sgFrameworkContextItem) {

        SgFrameworkContextItem process = super.process(sgFrameworkContextItem);
        tacLogger.info("返回前处理：sgFrameworkContextItem="+ JSON.toJSONString(sgFrameworkContextItem));
        return process;
    }
}
