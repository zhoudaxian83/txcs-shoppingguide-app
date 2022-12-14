package com.tmall.wireless.tac.biz.processor.common.handler;

import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.txcs.gs.framework.model.EntityVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;

/**
 * Created by yangqing.byq on 2021/6/28.
 */
public class TemplateCommonItemHandler extends RpmReactiveHandler<SgFrameworkResponse<EntityVO>> {

    @Override
    public Flowable<TacResult<SgFrameworkResponse<EntityVO>>> executeFlowable(Context context) throws Exception {
        return Flowable.just(TacResult.errorResult(""));
    }
}
