package com.tmall.wireless.tac.biz.processor.firstScreenMind;

import java.util.List;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.alibaba.fastjson.JSON;

import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.txcs.gs.framework.model.EntityVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FirstScreenMindItemHandler extends RpmReactiveHandler<SgFrameworkResponse<EntityVO>> {

    Logger LOGGER = LoggerFactory.getLogger(FirstScreenMindItemHandler.class);

    @Autowired
    TacLogger tacLogger;
    @Autowired
    FirstScreenMindItemScene firstScreenMindItemScene;
    @Override
    public Flowable<TacResult<SgFrameworkResponse<EntityVO>>> executeFlowable(Context context) throws Exception {
        Flowable<TacResult<SgFrameworkResponse<EntityVO>>> result = firstScreenMindItemScene.recommend(context);
        HadesLogUtil.stream(ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_ITEM)
            .kv("FirstScreenMindItemAldHandler","executeFlowable")
            .kv("result", JSON.toJSONString(result))
            .info();
        return result;
    }
}
