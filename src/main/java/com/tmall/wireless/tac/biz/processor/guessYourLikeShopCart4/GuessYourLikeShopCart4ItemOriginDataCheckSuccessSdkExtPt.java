package com.tmall.wireless.tac.biz.processor.guessYourLikeShopCart4;

import com.tmall.tcls.gs.sdk.biz.extensions.item.origindata.DefaultItemOriginDataCheckSuccessSdkExtPt;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataCheckSuccessSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.OriginDataProcessRequest;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created from template by 程斐斐 on 2021-09-28 15:44:08.
 */

@SdkExtension(
        bizId = "supermarket",
        useCase = "b2c",
        scenario = "guessYourLikeShopCart4"
)
public class GuessYourLikeShopCart4ItemOriginDataCheckSuccessSdkExtPt extends DefaultItemOriginDataCheckSuccessSdkExtPt implements ItemOriginDataCheckSuccessSdkExtPt {

    @Autowired
    TacLoggerImpl tacLogger;
    @Override
    public Boolean process(OriginDataProcessRequest originDataProcessRequest) {
        Boolean process = super.process(originDataProcessRequest);
        tacLogger.info("CheckSuccess");
        return process;
    }
}
