package com.tmall.wireless.tac.biz.processor.firstScreenMind;

import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.txcs.gs.framework.model.ContentVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FirstScreenMindContentHandler extends RpmReactiveHandler<SgFrameworkResponse<ContentVO>> {

    @Autowired
    FirstScreenMindContentScene visitSupermarketItemScene;

    @Override
    public Flowable<TacResult<SgFrameworkResponse<ContentVO>>> executeFlowable(Context context) throws Exception {
        return visitSupermarketItemScene.recommend(context);
    }
}
