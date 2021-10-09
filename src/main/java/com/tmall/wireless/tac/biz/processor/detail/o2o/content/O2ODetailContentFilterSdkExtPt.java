package com.tmall.wireless.tac.biz.processor.detail.o2o.content;

import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.vo.ContentFilterSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ContentVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextContent;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.DetailConstant;

/**
 * @author: guichen
 * @Data: 2021/10/9
 * @Description:
 */
@SdkExtension(bizId = DetailConstant.BIZ_ID,
    useCase = DetailConstant.USE_CASE_O2O,
    scenario = DetailConstant.CONTENT_SCENERIO)
public class O2ODetailContentFilterSdkExtPt extends Register implements ContentFilterSdkExtPt {
    @Override
    public SgFrameworkResponse<ContentVO> process(SgFrameworkContextContent sgFrameworkContextContent) {
        return sgFrameworkContextContent.getContentVOSgFrameworkResponse();
    }
}
