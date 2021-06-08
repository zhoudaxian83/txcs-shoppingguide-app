package com.tmall.wireless.tac.biz.processor.newproduct.handler;

import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.txcs.gs.framework.model.EntityVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;

/**
 * @author haixiao.zhang
 * @date 2021/6/8
 */
public class SxlItemAndContentHandler extends RpmReactiveHandler<SgFrameworkResponse<EntityVO>> {

    @Override
    public Flowable<TacResult<SgFrameworkResponse<EntityVO>>> executeFlowable(Context context) throws Exception{

        return null;

    }
}
