package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab;

import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataResponseConvertSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ResponseConvertRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;
import org.springframework.stereotype.Service;

/**
 * Created from template by 罗俊冲 on 2021-09-15 17:54:58.
 */

@SdkExtension(
        bizId = "supermarket",
        useCase = "b2c",
        scenario = "TodayCrazyRecommendTab"
)
public class TodayCrazyRecommendTabItemOriginDataResponseConvertSdkExtPt extends Register implements ItemOriginDataResponseConvertSdkExtPt {
    @Override
    public OriginDataDTO<ItemEntity> process(ResponseConvertRequest responseConvertRequest) {
        return null;
    }
}
