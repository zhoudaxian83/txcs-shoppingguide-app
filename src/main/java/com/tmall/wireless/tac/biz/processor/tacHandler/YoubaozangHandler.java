package com.tmall.wireless.tac.biz.processor.tacHandler;

import com.tmall.txcs.biz.supermarket.scene.gul.GulSubTabScene;
import com.tmall.txcs.biz.supermarket.scene.youbaozang.YoubaozangScene;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.txcs.gs.framework.model.EntityVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by yangqing.byq on 2021/3/9.
 */
@Component
public class YoubaozangHandler extends RpmReactiveHandler<SgFrameworkResponse<EntityVO>> {
    @Autowired
    YoubaozangScene youbaozangScene;

    @Override
    public Flowable<TacResult<SgFrameworkResponse<EntityVO>>> executeFlowable(Context context) throws Exception {
        return youbaozangScene.recommend(context);
    }
}
