package com.tmall.wireless.tac.biz.processor.guessYourLikeShopCart4;

import com.alibaba.fastjson.JSON;
import com.tmall.tcls.gs.sdk.biz.extensions.item.origindata.DefaultItemOriginDataResponseConvertSdkExtPt;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataResponseConvertSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ResponseConvertRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.springframework.beans.factory.annotation.Autowired;

@SdkExtension(
        bizId = "supermarket",
        useCase = "b2c",
        scenario = "guessYourLikeShopCart4"
)
public class GuessYourLikeShopCart4ItemOriginDataResponseConvertSdkExtPt
        extends DefaultItemOriginDataResponseConvertSdkExtPt implements ItemOriginDataResponseConvertSdkExtPt {


    @Autowired
    TacLoggerImpl tacLogger;
    @Override
    public OriginDataDTO<ItemEntity> process(ResponseConvertRequest responseConvertRequest) {

        tacLogger.info("进入tpp返回值解析扩展点");
        tacLogger.info("responseConvertRequest内容：" + responseConvertRequest.getResponse());
        OriginDataDTO<ItemEntity> process = super.process(responseConvertRequest);
        tacLogger.info("tpp返回值解析扩展点解析后内容：" + JSON.toJSONString(process));
        tacLogger.info("出tpp返回值解析扩展点");
        return process;
    }
}
