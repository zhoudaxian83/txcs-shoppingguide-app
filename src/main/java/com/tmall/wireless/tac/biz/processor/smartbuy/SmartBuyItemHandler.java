package com.tmall.wireless.tac.biz.processor.smartbuy;

import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.txcs.gs.framework.model.EntityVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SmartBuyItemHandler extends RpmReactiveHandler<SgFrameworkResponse<EntityVO>> {

    @Autowired
    SmartBuyItemScene smartBuyItemScene;

    @Override
    public Flowable<TacResult<SgFrameworkResponse<EntityVO>>> executeFlowable(Context context) throws Exception {
        return smartBuyItemScene.recommend(context);
    }
}
