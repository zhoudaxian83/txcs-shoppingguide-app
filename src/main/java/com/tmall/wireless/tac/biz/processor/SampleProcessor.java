package com.tmall.wireless.tac.biz.processor;

import com.alibaba.cola.extension.ExtensionPointI;
import com.alibaba.fastjson.JSON;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.txcs.gs.framework.model.*;
import com.tmall.txcs.gs.framework.service.impl.SgFrameworkServiceItem;
import com.tmall.txcs.gs.framework.service.impl.SgFrameworkServiceMix;
import com.tmall.txcs.gs.model.biz.context.SceneInfo;
import com.tmall.txcs.gs.service.facade.SyncFacade;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SampleProcessor extends RpmReactiveHandler<SgFrameworkResponse<ItemEntityVO>> {

    Logger LOGGER = LoggerFactory.getLogger(SyncFacade.class);


    @Autowired
    SgFrameworkServiceMix sgFrameworkServiceMix;

    @Autowired
    SgFrameworkServiceItem sgFrameworkServiceItem;
    @Autowired
    AppColaBootstrap appColaBootstrap;

    @Override
    public Flowable<TacResult<SgFrameworkResponse<ItemEntityVO>>> executeFlowable(Context context) throws Exception {
//        SgFrameworkContextMix sgFrameworkContextMix = new SgFrameworkContextMix();

        SgFrameworkContextItem sgFrameworkContextItem = new SgFrameworkContextItem();
        SceneInfo sceneInfo = new SceneInfo();
        sceneInfo.setScene("gul");
        sgFrameworkContextItem.setSceneInfo(sceneInfo);


        Map<String, ExtensionPointI> stringExtensionPointIMap = appColaBootstrap.queryExtMap();

        LOGGER.warn("stringExtensionPoints:{}", JSON.toJSONString(stringExtensionPointIMap.keySet()));

        return sgFrameworkServiceItem.recommend(sgFrameworkContextItem)
                .map(TacResult::newResult)
                .onErrorReturn(r -> TacResult.errorResult(""));

    }

}
