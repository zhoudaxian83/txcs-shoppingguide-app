package com.tmall.wireless.tac.biz.processor.guessWhatYouLikeMenu.ext;

import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataPostProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataProcessRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.ContentEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;

/**
 * @author Yushan
 * @date 2021/9/8 9:36 下午
 */
public class GulMenuContentOriginDataPostProcessorSdkExtPt extends Register implements ContentOriginDataPostProcessorSdkExtPt {
    @Override
    public OriginDataDTO<ContentEntity> process(ContentOriginDataProcessRequest contentOriginDataProcessRequest) {


        return null;
    }
}
