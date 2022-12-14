package com.tmall.wireless.tac.biz.processor.tacHandler;

import com.tmall.txcs.biz.supermarket.scene.GcsRecommendScene;
import com.tmall.txcs.biz.supermarket.scene.gul.GulSubTabScene;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.txcs.gs.framework.model.ContentVO;
import com.tmall.txcs.gs.framework.model.EntityVO;
import com.tmall.txcs.gs.framework.model.ItemEntityVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.text.html.parser.Entity;

import com.alibaba.fastjson.JSON;

/**
 * Created by yangqing.byq on 2021/3/9.
 */
@Slf4j
@Component
public class GulSubTabHandler extends RpmReactiveHandler<SgFrameworkResponse<EntityVO>> {
    @Autowired
    GulSubTabScene gulSubTabScene;

    @Override
    public Flowable<TacResult<SgFrameworkResponse<EntityVO>>> executeFlowable(Context context) throws Exception {
        log.error("entryContext." + "GUL_SUB_TAB" + ",context:{}", JSON.toJSONString(context));
        return gulSubTabScene.recommend(context);
    }
}
