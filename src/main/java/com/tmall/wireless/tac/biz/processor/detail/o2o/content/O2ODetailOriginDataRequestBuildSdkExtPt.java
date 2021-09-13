package com.tmall.wireless.tac.biz.processor.detail.o2o.content;

import com.alibaba.cola.extension.Extension;

import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextContent;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.DetailConstant;
import com.tmall.wireless.tac.biz.processor.detail.common.extabstract.AbstractDetailOriginDataRequestBuildSdkExtPt;
import org.springframework.stereotype.Service;

/**
 * @author: guichen
 * @Data: 2021/9/10
 * @Description:
 */
@Extension(bizId = DetailConstant.BIZ_ID,
    useCase = DetailConstant.USE_CASE_O2O,
    scenario = DetailConstant.CONTENT_SCENERIO)
@Service
public class O2ODetailOriginDataRequestBuildSdkExtPt
    extends AbstractDetailOriginDataRequestBuildSdkExtPt implements ContentOriginDataRequestBuildSdkExtPt {

    @Override
    public Long getAppId(){
        return 21174L;
    }

    @Override
    public RecommendRequest process(SgFrameworkContextContent sgFrameworkContextContent) {
        return super.process(sgFrameworkContextContent);
    }

}
