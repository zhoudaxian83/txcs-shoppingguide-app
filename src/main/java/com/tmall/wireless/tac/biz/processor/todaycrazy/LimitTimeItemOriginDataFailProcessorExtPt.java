package com.tmall.wireless.tac.biz.processor.todaycrazy;

import com.alibaba.cola.extension.Extension;
import com.tmall.txcs.gs.framework.extensions.failprocessor.ItemFailProcessorRequest;
import com.tmall.txcs.gs.framework.extensions.failprocessor.ItemOriginDataFailProcessorExtPt;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import org.springframework.stereotype.Service;

/**
 * @author guijian
 */
@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.SCENARIO_TODAY_CRAZY_LIMIT_TIME_BUY)
@Service
public class LimitTimeItemOriginDataFailProcessorExtPt implements ItemOriginDataFailProcessorExtPt {

    @Override
    public OriginDataDTO<ItemEntity> process(ItemFailProcessorRequest itemFailProcessorRequest) {
        return itemFailProcessorRequest.getItemEntityOriginDataDTO();
    }
}
