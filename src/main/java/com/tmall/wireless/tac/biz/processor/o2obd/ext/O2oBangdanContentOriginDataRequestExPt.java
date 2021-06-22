package com.tmall.wireless.tac.biz.processor.o2obd.ext;

import com.alibaba.cola.extension.Extension;
import com.tmall.txcs.gs.framework.extensions.origindata.request.ContentOriginDataRequestExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextContent;
import com.tmall.txcs.gs.model.spi.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import org.springframework.stereotype.Service;

/**
 * @author haixiao.zhang
 * @date 2021/6/22
 */
@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_O2O,
    scenario = ScenarioConstantApp.O2O_BANG_DAN)
@Service
public class O2oBangdanContentOriginDataRequestExPt implements ContentOriginDataRequestExtPt {
    @Override
    public RecommendRequest process(SgFrameworkContextContent sgFrameworkContextContent) {

        return null;
    }
}
