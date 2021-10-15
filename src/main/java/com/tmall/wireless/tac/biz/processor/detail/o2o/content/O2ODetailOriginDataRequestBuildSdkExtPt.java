package com.tmall.wireless.tac.biz.processor.detail.o2o.content;


import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContext;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextContent;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.detail.common.config.DetailSwitch;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.DetailConstant;
import com.tmall.wireless.tac.biz.processor.detail.common.extabstract.AbstractDetailOriginDataRequestBuildSdkExtPt;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecommendRequest;
import com.tmall.wireless.tac.biz.processor.detail.model.config.DetailRequestConfig;

/**
 * @author: guichen
 * @Data: 2021/9/10
 * @Description:
 */
@SdkExtension(bizId = DetailConstant.BIZ_ID,
    useCase = DetailConstant.USE_CASE_O2O,
    scenario = DetailConstant.CONTENT_SCENERIO)
public class O2ODetailOriginDataRequestBuildSdkExtPt
    extends AbstractDetailOriginDataRequestBuildSdkExtPt implements ContentOriginDataRequestBuildSdkExtPt {

    @Override
    public Long getAppId(String recType, SgFrameworkContext sgFrameworkContextContent){
        return DetailRequestConfig.parse(recType).getTppId();
    }

    @Override
    public RecommendRequest process(SgFrameworkContextContent sgFrameworkContextContent) {
        RecommendRequest process = super.processRequest(sgFrameworkContextContent);

        DetailRecommendRequest detailRequest = DetailRecommendRequest.getDetailRequest(
            sgFrameworkContextContent.getTacContext());
        process.getParams().put("triggerItemIds",String.valueOf(detailRequest.getDetailItemId()));

        return process;
    }

}
