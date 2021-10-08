package com.tmall.wireless.tac.biz.processor.detail.o2o.content;

import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.context.ContentMetaInfoBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.meta.ContentMetaInfo;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.DetailConstant;
import com.tmall.wireless.tac.client.domain.Context;

/**
 * @author: guichen
 * @Data: 2021/9/15
 * @Description:
 */
@SdkExtension(bizId = DetailConstant.BIZ_ID,
    useCase = DetailConstant.USE_CASE_O2O,
    scenario = DetailConstant.CONTENT_SCENERIO)
public class O2ODetailRecContentMetaInfoBuildSdkExtPt extends Register
    implements ContentMetaInfoBuildSdkExtPt {
    @Override
    public ContentMetaInfo process(Context context) {
        ContentMetaInfo contentMetaInfo = new ContentMetaInfo();
        contentMetaInfo.setQueryItemList(false);
        return contentMetaInfo;
    }
}
