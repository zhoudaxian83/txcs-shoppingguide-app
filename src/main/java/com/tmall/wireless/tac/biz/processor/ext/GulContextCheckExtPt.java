package com.tmall.wireless.tac.biz.processor.ext;

import com.alibaba.cola.extension.Extension;
import com.tmall.txcs.gs.framework.extensions.paramcheck.ContextCheckExtPt;
import com.tmall.txcs.gs.framework.extensions.paramcheck.ContextCheckResult;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextMix;
import com.tmall.txcs.gs.framework.model.constant.ScenarioConstant;
import org.springframework.stereotype.Service;

/**
 * Created by yangqing.byq on 2021/2/13.
 */
@Extension(bizId = ScenarioConstant.ENTITY_TYPE_ITEM,
        useCase = ScenarioConstant.BIZ_TYPE_B2C,
        scenario = "gul")
@Service
public class GulContextCheckExtPt implements ContextCheckExtPt {


    @Override
    public ContextCheckResult process(SgFrameworkContext sgFrameworkContext) {

        ContextCheckResult contextCheckResult = new ContextCheckResult();
        contextCheckResult.setSuccess(true);
        return contextCheckResult;
    }
}
