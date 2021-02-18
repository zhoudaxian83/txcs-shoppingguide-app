package com.tmall.wireless.tac.biz.processor.ext;

import com.alibaba.cola.extension.Extension;
import com.tmall.txcs.gs.framework.extensions.origindata.request.DefaultItemOriginDataRequestExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.framework.model.constant.ScenarioConstant;
import com.tmall.txcs.gs.service.model.TppRequest;
import org.springframework.stereotype.Service;

/**
 * Created by yangqing.byq on 2021/2/18.
 */
@Extension(bizId = ScenarioConstant.ENTITY_TYPE_ITEM,
        useCase = ScenarioConstant.BIZ_TYPE_B2C,
        scenario = "gul")
@Service
public class GulItemOriginDataRequestExtPt extends DefaultItemOriginDataRequestExtPt {
    @Override
    public TppRequest processOriginDataRequest(SgFrameworkContextItem sgFrameworkContextItem) {
        TppRequest tppRequest = super.processOriginDataRequest(sgFrameworkContextItem);
        tppRequest.getParams().put("pageId", "cainixihuan1");
        return tppRequest;
    }

}
