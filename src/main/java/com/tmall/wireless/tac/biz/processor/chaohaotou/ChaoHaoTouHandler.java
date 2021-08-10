package com.tmall.wireless.tac.biz.processor.chaohaotou;

import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.txcs.gs.framework.model.EntityVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: luoJunChong
 * @Date: 2021/5/13 18:02
 */
@Component
public class ChaoHaoTouHandler extends RpmReactiveHandler<SgFrameworkResponse<EntityVO>> {
    @Autowired
    ChaoHaoTouPageBannerItemInfoScene chaoHaoTouPageBannerItemInfoScene;

    @Override
    public Flowable<TacResult<SgFrameworkResponse<EntityVO>>> executeFlowable(Context context) throws Exception {
        return chaoHaoTouPageBannerItemInfoScene.recommend(context);
    }
}
