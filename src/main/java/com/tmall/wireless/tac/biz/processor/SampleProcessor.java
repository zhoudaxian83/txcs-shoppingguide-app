package com.tmall.wireless.tac.biz.processor;

import com.alibaba.fastjson.JSON;

import com.tmall.recommend.biz.RpmReactiveHandler;
import com.tmall.recommend.biz.model.context.RpmRequestContext;
import com.tmall.recommend.framework.TestService;
import com.tmall.wireless.tac.client.common.TacResult;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SampleProcessor extends RpmReactiveHandler<String> {

    @Autowired
    TestService testService;
//
//    @Autowired
//    AppColaBootstrap appColaBootstrap;

    @Override
    public Flowable<TacResult<String>> rpmExecuteFlowable(RpmRequestContext rpmRequestContext) throws Exception {
//        Set<String> collect = appColaBootstrap.appExtPts.stream().map(pt -> pt.getClass().getName() + " " +
//                pt.getClass().getClassLoader().getClass().getName()).collect(Collectors.toSet());

        Object scenario = rpmRequestContext.getParamMap().get("scenario");

        return Flowable.just(TacResult.newResult(
                testService.colaTest(scenario == null ? "" : scenario.toString())
//        + JSON.toJSONString(collect)
        ));

    }
}
