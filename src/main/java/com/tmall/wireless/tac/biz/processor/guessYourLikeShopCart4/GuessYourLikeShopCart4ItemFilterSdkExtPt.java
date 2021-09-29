package com.tmall.wireless.tac.biz.processor.guessYourLikeShopCart4;

import com.alibaba.fastjson.JSON;
import com.tmall.tcls.gs.sdk.biz.extensions.item.filter.DefaultItemFilterSdkExtPt;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.filter.ItemFilterRequest;
import com.tmall.tcls.gs.sdk.framework.extensions.item.filter.ItemFilterSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
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
public class GuessYourLikeShopCart4ItemFilterSdkExtPt extends DefaultItemFilterSdkExtPt implements ItemFilterSdkExtPt {

    @Autowired
    TacLoggerImpl tacLogger;
    @Override
    public SgFrameworkResponse<ItemEntityVO> process(ItemFilterRequest itemFilterRequest) {

        SgFrameworkResponse<ItemEntityVO> process = super.process(itemFilterRequest);
        tacLogger.info("商品过滤处理：ItemEntityVOResponse="+ JSON.toJSONString(process));
        return process;
    }
}
