package com.tmall.wireless.tac.biz.processor.firstScreenMind;

import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.model.FacadeResult;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FirstScreenMindContentHandler extends RpmReactiveHandler<FacadeResult> {

    @Autowired
    FirstScreenMindContentScene visitSupermarketItemScene;

    @Override
    public Flowable<TacResult<FacadeResult>> executeFlowable(Context context) throws Exception {
        return visitSupermarketItemScene.recommend(context);
    }
}
