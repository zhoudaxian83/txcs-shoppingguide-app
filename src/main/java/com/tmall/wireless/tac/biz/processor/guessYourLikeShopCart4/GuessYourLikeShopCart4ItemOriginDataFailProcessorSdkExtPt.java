package com.tmall.wireless.tac.biz.processor.guessYourLikeShopCart4;

import com.alibaba.fastjson.JSON;
import com.tmall.tcls.gs.sdk.biz.extensions.item.origindata.DefaultItemOriginDataFailProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataFailProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.OriginDataProcessRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created from template by 程斐斐 on 2021-09-28 15:56:22.
 */

@SdkExtension(
        bizId = "supermarket",
        useCase = "b2c",
        scenario = "guessYourLikeShopCart4"
)
public class GuessYourLikeShopCart4ItemOriginDataFailProcessorSdkExtPt extends DefaultItemOriginDataFailProcessorSdkExtPt implements ItemOriginDataFailProcessorSdkExtPt {

    @Autowired
    TacLoggerImpl tacLogger;
    @Override
    public OriginDataDTO<ItemEntity> process(OriginDataProcessRequest originDataProcessRequest) {
        tacLogger.info("originDataProcessRequest = "+ JSON.toJSONString(originDataProcessRequest));
        OriginDataDTO<ItemEntity> process = super.process(originDataProcessRequest);
        tacLogger.info("process = "+ JSON.toJSONString(process));
        return process;
    }
}
