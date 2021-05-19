package com.tmall.wireless.tac.biz.processor.todaycrazy;

import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.txcs.gs.framework.model.EntityVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author guijian
 */
@Component
public class LimitTimeBuyHandler extends RpmReactiveHandler {
    Logger LOGGER = LoggerFactory.getLogger(LimitTimeBuyHandler.class);

    @Autowired
    LimitTimeBuyScene limitTimeBuyScene;
    @Autowired
    TacLogger tacLogger;

    @Override
    public Flowable<TacResult<SgFrameworkResponse<EntityVO>>> executeFlowable(Context context) throws Exception {
        tacLogger.info("***tacLogger LimitTimeBuyHandler context.getParams()****:"+context.getParams());
        LOGGER.info("***LOGGER LimitTimeBuyHandler context.getParams()****:"+context.getParams());
        return limitTimeBuyScene.recommend(context);
    }
}
