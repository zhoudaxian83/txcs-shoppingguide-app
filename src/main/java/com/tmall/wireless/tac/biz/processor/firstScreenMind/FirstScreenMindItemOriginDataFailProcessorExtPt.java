package com.tmall.wireless.tac.biz.processor.firstScreenMind;

import com.tmall.txcs.gs.framework.extensions.failprocessor.ItemFailProcessorRequest;
import com.tmall.txcs.gs.framework.extensions.failprocessor.ItemOriginDataFailProcessorExtPt;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;

public class FirstScreenMindItemOriginDataFailProcessorExtPt implements ItemOriginDataFailProcessorExtPt {
    @Override
    public OriginDataDTO<ItemEntity> process(ItemFailProcessorRequest itemFailProcessorRequest) {
        return null;
    }
}
