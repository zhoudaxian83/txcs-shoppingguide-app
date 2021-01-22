package com.tmall.wireless.tac.biz.processor;

import com.tmall.recommend.biz.RpmReactiveHandler;
import com.tmall.recommend.biz.model.context.RpmRequestContext;
import com.tmall.recommend.framework.TestService;
import com.tmall.wireless.tac.client.common.TacResult;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Component
public class SampleProcessor extends RpmReactiveHandler<String> {

    @Autowired
    TestService testService;

    @Override
    public Flowable<TacResult<String>> rpmExecuteFlowable(RpmRequestContext rpmRequestContext) throws Exception {

        return Flowable.just(TacResult.newResult(testService.colaTest()));

    }
}
