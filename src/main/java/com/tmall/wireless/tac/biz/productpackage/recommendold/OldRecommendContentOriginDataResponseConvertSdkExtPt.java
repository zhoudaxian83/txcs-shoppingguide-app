package com.tmall.wireless.tac.biz.productpackage.recommendold;

import com.tmall.tcls.gs.sdk.ext.annotation.SdkPackage;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataResponseConvertSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentResponseConvertRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.ContentEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;
import com.tmall.wireless.tac.biz.processor.common.PackageNameKey;
import org.springframework.stereotype.Service;

@Service
@SdkPackage(packageName = PackageNameKey.OLD_RECOMMEND)
public class OldRecommendContentOriginDataResponseConvertSdkExtPt extends Register implements ContentOriginDataResponseConvertSdkExtPt {
    @Override
    public OriginDataDTO<ContentEntity> process(ContentResponseConvertRequest contentResponseConvertRequest) {
        return TppConvertUtil.processContentEntity(contentResponseConvertRequest.getResponse());
    }
}
