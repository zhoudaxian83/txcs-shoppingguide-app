package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab;

import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataSuccessProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.OriginDataProcessRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;
import org.springframework.stereotype.Service;

/**
 * Created from template by 罗俊冲 on 2021-09-23 14:14:31.
 */

@SdkExtension(
        bizId = "supermarket",
        useCase = "b2c",
        scenario = "TodayCrazyRecommendTab"
)
public class TodayCrazyRecommendTabItemOriginDataSuccessProcessorSdkExtPt extends Register implements ItemOriginDataSuccessProcessorSdkExtPt {
    @Override
    public OriginDataDTO<ItemEntity> process(OriginDataProcessRequest originDataProcessRequest) {
        return null;
    }
}
