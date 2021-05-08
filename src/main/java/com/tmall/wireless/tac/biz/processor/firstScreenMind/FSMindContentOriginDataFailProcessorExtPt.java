package com.tmall.wireless.tac.biz.processor.firstScreenMind;

import com.alibaba.cola.extension.Extension;
import com.tmall.txcs.gs.framework.extensions.failprocessor.ContentFailProcessorRequest;
import com.tmall.txcs.gs.framework.extensions.failprocessor.ContentOriginDataFailProcessorExtPt;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.model.model.dto.ContentEntity;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;

@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_CONTENT)
public class FSMindContentOriginDataFailProcessorExtPt implements ContentOriginDataFailProcessorExtPt {

    @Override
    public OriginDataDTO<ContentEntity> process(ContentFailProcessorRequest contentFailProcessorRequest) {
        return contentFailProcessorRequest.getContentEntityOriginDataDTO();
    }
}
