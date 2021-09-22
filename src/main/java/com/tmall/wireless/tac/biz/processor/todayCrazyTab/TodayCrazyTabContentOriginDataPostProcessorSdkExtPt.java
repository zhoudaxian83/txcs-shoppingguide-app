package com.tmall.tcls.gs.sdk.framework.extensions.content.atemp;

import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataPostProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataProcessRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.ContentEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;

/**
 * Created from template by 进舟 on 2021-09-22 16:00:05.
 */

@SdkExtension(
        bizId = "supermarket",
        useCase = "b2c",
        scenario = "todayCrazyTab"
)
public class TodayCrazyTabContentOriginDataPostProcessorSdkExtPt extends Register implements ContentOriginDataPostProcessorSdkExtPt {
    @Override
    public OriginDataDTO<ContentEntity> process(ContentOriginDataProcessRequest contentOriginDataProcessRequest) {
        return null;
    }
}
