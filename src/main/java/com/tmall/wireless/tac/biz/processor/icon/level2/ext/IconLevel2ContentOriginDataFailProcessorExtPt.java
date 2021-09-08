package com.tmall.wireless.tac.biz.processor.icon.level2.ext;

import com.google.common.base.Preconditions;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataFailProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataProcessRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.ContentEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContext;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextContent;
import com.tmall.txcs.gs.framework.extensions.failprocessor.ContentOriginDataFailProcessorExtPt;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.icon.level2.Level2RecommendService;
import com.tmall.wireless.tac.biz.processor.icon.level2.Level2Request;

import java.util.Optional;


public class IconLevel2ContentOriginDataFailProcessorExtPt extends Register implements ContentOriginDataFailProcessorSdkExtPt {

    @Override
    public OriginDataDTO<ContentEntity> process(ContentOriginDataProcessRequest contentOriginDataProcessRequest) {
        Level2Request level2Request =(Level2Request) Optional.of(contentOriginDataProcessRequest)
                .map(ContentOriginDataProcessRequest::getSgFrameworkContextContent)
                .map(SgFrameworkContext::getTacContext)
                .map(c -> c.get(Level2RecommendService.level2RequestKey))
                .orElse(null);

        Preconditions.checkArgument(level2Request != null);
        RecommendRequest recommendRequest = new RecommendRequest();

        return null;

    }
}
