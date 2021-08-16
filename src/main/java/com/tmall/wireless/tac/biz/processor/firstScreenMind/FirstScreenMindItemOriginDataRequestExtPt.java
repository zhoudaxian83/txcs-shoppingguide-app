package com.tmall.wireless.tac.biz.processor.firstScreenMind;

import com.alibaba.cola.extension.Extension;
import com.tmall.txcs.gs.framework.extensions.origindata.request.ItemOriginDataRequestExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.model.spi.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.common.FirstScreenConstant;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.origindatarequest.OriginDataRequestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_ITEM)
@Service
public class FirstScreenMindItemOriginDataRequestExtPt implements ItemOriginDataRequestExtPt {

    @Autowired
    OriginDataRequestFactory originDataRequestFactory;

    @Override
    public RecommendRequest process(SgFrameworkContextItem sgFrameworkContextItem) {

        RecommendRequest tppRequest = originDataRequestFactory.getRecommendRequest(FirstScreenConstant.SUB_ITEM_FEEDS,sgFrameworkContextItem);
        return tppRequest;
    }

}
