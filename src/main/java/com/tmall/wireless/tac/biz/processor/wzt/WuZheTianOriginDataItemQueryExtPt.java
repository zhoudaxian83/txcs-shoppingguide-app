package com.tmall.wireless.tac.biz.processor.wzt;

import com.alibaba.cola.extension.Extension;

import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataItemQueryExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author luojunchong
 */
@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.WU_ZHE_TIAN)
@Service
public class WuZheTianOriginDataItemQueryExtPt implements OriginDataItemQueryExtPt {

    @Autowired
    TacLogger tacLogger;

    @Override
    public Flowable<OriginDataDTO<ItemEntity>> process(SgFrameworkContextItem context) {
        tacLogger.info("WuZheTianOriginDataItemQueryExtPt");
        OriginDataDTO<ItemEntity> originDataDTO = new OriginDataDTO<>();
        return Flowable.just(originDataDTO);
    }
}
