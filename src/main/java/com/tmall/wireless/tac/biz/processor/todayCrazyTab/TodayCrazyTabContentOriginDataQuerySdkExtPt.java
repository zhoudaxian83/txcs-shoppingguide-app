package com.tmall.tcls.gs.sdk.framework.extensions.content.atemp;

import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataQuerySdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.ContentEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextContent;
import io.reactivex.Flowable;

/**
 * Created from template by 进舟 on 2021-09-22 16:00:05.
 */

@SdkExtension(
        bizId = "supermarket",
        useCase = "b2c",
        scenario = "todayCrazyTab"
)
public class TodayCrazyTabContentOriginDataQuerySdkExtPt extends Register implements ContentOriginDataQuerySdkExtPt {
    @Override
    public Flowable<OriginDataDTO<ContentEntity>> process(SgFrameworkContextContent sgFrameworkContextContent) {
        return null;
    }
}
