package com.tmall.wireless.tac.biz.processor.todaycrazy;

import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.txcs.gs.framework.model.EntityVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LimitTimeBuyHandler extends RpmReactiveHandler {

    @Autowired
    LimitTimeBuyScene limitTimeBuyScene;
    @Autowired
    TacLogger tacLogger;

    @Override
    public Flowable<TacResult<SgFrameworkResponse<EntityVO>>> executeFlowable(Context context) throws Exception {
        tacLogger.info("***LimitTimeBuyHandler context.getParams()****:"+context.getParams());
        tacLogger.info("***LimitTimeBuyHandler context.getParams().toString()****:"+context.getParams().toString());
        return limitTimeBuyScene.recommend(context);
    }
}
