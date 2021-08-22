package com.tmall.wireless.tac.biz.processor.huichang.inventory.acceptancePage;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import com.tmall.wireless.tac.client.handler.TacReactiveHandler4Ald;
import io.reactivex.Flowable;

import java.util.List;

public class AcceptancePageHandler extends TacReactiveHandler4Ald {
    @Override
    public Flowable<TacResult<List<GeneralItem>>> executeFlowable(RequestContext4Ald requestContext4Ald) throws Exception {
        return null;
    }
}
