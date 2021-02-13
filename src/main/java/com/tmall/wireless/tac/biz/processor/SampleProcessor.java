package com.tmall.wireless.tac.biz.processor;

import com.alibaba.fastjson.JSON;


import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.txcs.gs.framework.model.EntityVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextMix;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.txcs.gs.framework.service.impl.SgFrameworkServiceMix;
import com.tmall.txcs.gs.model.biz.context.SceneInfo;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SampleProcessor extends RpmReactiveHandler<String> {



    @Autowired
    SgFrameworkServiceMix sgFrameworkServiceMix;

    @Override
    public Flowable<TacResult<String>> executeFlowable(Context context) throws Exception {
        SgFrameworkContextMix sgFrameworkContextMix = new SgFrameworkContextMix();

        SceneInfo sceneInfo = new SceneInfo();
        sceneInfo.setScene("gul");
        sgFrameworkContextMix.setSceneInfo(sceneInfo);
        SgFrameworkResponse<EntityVO> sgFrameworkResponse = sgFrameworkServiceMix.recommend(sgFrameworkContextMix);

        return Flowable.just(TacResult.newResult(
                JSON.toJSONString(sgFrameworkResponse)
        ));
    }

}
