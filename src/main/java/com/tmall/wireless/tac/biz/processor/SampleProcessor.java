package com.tmall.wireless.tac.biz.processor;

import com.alibaba.cola.extension.ExtensionPointI;
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

import java.util.List;
import java.util.Map;

@Component
public class SampleProcessor extends RpmReactiveHandler<String> {



    @Autowired
    SgFrameworkServiceMix sgFrameworkServiceMix;
    @Autowired
    AppColaBootstrap appColaBootstrap;

    @Override
    public Flowable<TacResult<String>> executeFlowable(Context context) throws Exception {
        SgFrameworkContextMix sgFrameworkContextMix = new SgFrameworkContextMix();

        SceneInfo sceneInfo = new SceneInfo();
        sceneInfo.setScene("gul");
        sgFrameworkContextMix.setSceneInfo(sceneInfo);
        SgFrameworkResponse<EntityVO> sgFrameworkResponse = sgFrameworkServiceMix.recommend(sgFrameworkContextMix);

        List<ExtensionPointI> appExtPts = appColaBootstrap.appExtPts;

        Map<String, ExtensionPointI> beansOfType =
                AppColaBootstrap.applicationContext.getBeansOfType(ExtensionPointI.class);
        int size = appExtPts.size();
        return Flowable.just(TacResult.newResult(
                beansOfType.size() + "  " + size
        ));
    }

}
