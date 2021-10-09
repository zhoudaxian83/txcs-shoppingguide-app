package com.tmall.wireless.tac.biz.processor.extremeItem;

import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.OriginDataItemQuerySdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextItem;
import io.reactivex.Flowable;
import org.springframework.stereotype.Service;

/**
 * Created from template by 言武 on 2021-09-10 14:39:48.
 */

@SdkExtension(
        bizId = "supermarket",
        useCase = "b2c",
        scenario = "extremeItem"
)
@Service
public class ExtremeItemOriginDataItemQuerySdkExtPt extends Register implements OriginDataItemQuerySdkExtPt {
    @Override
    public Flowable<OriginDataDTO<ItemEntity>> process(SgFrameworkContextItem sgFrameworkContextItem) {
        return null;
    }
}
