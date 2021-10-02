package com.tmall.wireless.tac.biz.productpackage.recommendold;

import com.tmall.tcls.gs.sdk.ext.annotation.SdkPackage;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataResponseConvertSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ResponseConvertRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;
import com.tmall.wireless.tac.biz.processor.common.PackageNameKey;

@SdkPackage(packageName = PackageNameKey.OLD_RECOMMEND)
public class OldRecommendItemOriginDataResponseConvertSdkExtPt extends Register implements ItemOriginDataResponseConvertSdkExtPt {
    @Override
    public OriginDataDTO<ItemEntity> process(ResponseConvertRequest responseConvertRequest) {
        return TppConvertUtil.processItemEntity(responseConvertRequest.getResponse());
    }
}
