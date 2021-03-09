package com.tmall.wireless.tac.biz.processor.tacHandler;

import com.tmall.txcs.biz.supermarket.scene.GcsRecommendScene;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.txcs.gs.framework.model.ContentVO;
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
public class GcsTestProcessor extends RpmReactiveHandler<SgFrameworkResponse<ContentVO>> {

    @Autowired
    GcsRecommendScene gcsRecommendScene;

    @Override
    public Flowable<TacResult<SgFrameworkResponse<ContentVO>>> executeFlowable(Context context) throws Exception {
        return gcsRecommendScene.recommend(context);
    }
}
