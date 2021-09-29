package com.tmall.wireless.tac.biz.processor.chaohaotou;

import com.alibaba.fastjson.JSON;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.txcs.gs.framework.model.EntityVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.wireless.tac.biz.processor.chaohaotou.service.CommercialFeedsService;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(ChaoHaoTouHandler.class);

    @Override
    public Flowable<TacResult<SgFrameworkResponse<EntityVO>>> executeFlowable(Context context) throws Exception {
        LOGGER.info("ChaoHaoTouHandler_params"+ JSON.toJSONString(context));
        return chaoHaoTouPageBannerItemInfoScene.recommend(context);
    }
}
