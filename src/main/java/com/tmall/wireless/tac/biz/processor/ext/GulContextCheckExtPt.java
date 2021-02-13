package com.tmall.wireless.tac.biz.processor.ext;

import com.alibaba.cola.extension.Extension;
import com.tmall.txcs.gs.framework.extensions.paramcheck.ContextCheckExtPt;
import com.tmall.txcs.gs.framework.extensions.paramcheck.ContextCheckResult;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextMix;
import org.springframework.stereotype.Service;

/**
 * Created by yangqing.byq on 2021/2/13.
 */
@Extension(bizId = "txcs", useCase = "B2C", scenario = "gul")
public class GulContextCheckExtPt implements ContextCheckExtPt<SgFrameworkContextMix> {

    @Override
    public ContextCheckResult check(SgFrameworkContextMix contextMix) {
        ContextCheckResult contextCheckResult = new ContextCheckResult();
        contextCheckResult.setSuccess(false);
        contextCheckResult.setErrorMsg("ss");
        return contextCheckResult;
    }
}
