package com.tmall.wireless.tac.biz.processor.detail.o2o.content;

import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.framework.extensions.content.context.ContentUserCommonParamsBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.CommonUserParams;
import com.tmall.wireless.tac.biz.processor.detail.common.DetailCommonParamsBuildExtPt;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.DetailConstant;
import com.tmall.wireless.tac.client.domain.Context;

/**
 * @author: guichen
 * @Data: 2021/10/21
 * @Description:
 */
@SdkExtension(bizId = DetailConstant.BIZ_ID,
    useCase = DetailConstant.USE_CASE_O2O,
    scenario = DetailConstant.CONTENT_SCENERIO)
public class O2ODetailContentUserCommonParamsBuildSdkExtPt extends DetailCommonParamsBuildExtPt implements
    ContentUserCommonParamsBuildSdkExtPt {

    @Override
    public CommonUserParams process(Context context) {
        return super.process(context);
    }
}
