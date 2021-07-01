package com.tmall.wireless.tac.biz.processor.firstScreenMind;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.tmall.txcs.gs.framework.model.EntityVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import com.tmall.wireless.tac.client.handler.TacReactiveHandler4Ald;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FirstScreenMindItemAldHandler extends TacReactiveHandler4Ald {

    @Autowired
    FirstScreenMindItemScene firstScreenMindItemScene;
    @Override
    public Flowable<TacResult<List<GeneralItem>>> executeFlowable(RequestContext4Ald requestContext4Ald) throws Exception {
        return Flowable.just(TacResult.errorResult("error"));
    }
}
