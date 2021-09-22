package com.tmall.tcls.gs.sdk.framework.extensions.content.atemp;

import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextContent;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;

/**
 * Created from template by 进舟 on 2021-09-22 16:01:32.
 */

@SdkExtension(
        bizId = "supermarket",
        useCase = "b2c",
        scenario = "todayCrazyTab"
)
public class TodayCrazyTabContentOriginDataRequestBuildSdkExtPt extends Register implements ContentOriginDataRequestBuildSdkExtPt {
    @Override
    public RecommendRequest process(SgFrameworkContextContent sgFrameworkContextContent) {
        return null;
    }
}
