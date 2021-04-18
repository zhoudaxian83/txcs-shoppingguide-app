package com.tmall.wireless.tac.biz.processor.gcsfeeds;

import com.alibaba.cola.extension.Extension;
import com.tmall.txcs.gs.framework.extensions.failprocessor.ContentFailProcessorRequest;
import com.tmall.txcs.gs.framework.extensions.failprocessor.ContentOriginDataFailProcessorExtPt;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.model.model.dto.ContentEntity;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;

/**
 * Created by yangqing.byq on 2021/4/18.
 */
@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.SCENARIO_GCS_FEEDS)
public class GcsfeedsContentOriginDataFailProcessorExtPt implements ContentOriginDataFailProcessorExtPt {
    @Override
    public OriginDataDTO<ContentEntity> process(ContentFailProcessorRequest contentFailProcessorRequest) {
        return null;
    }
}
