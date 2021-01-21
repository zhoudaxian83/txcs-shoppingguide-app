package com.tmall.wireless.tac.biz.processor;

import com.tmall.recommend.biz.RpmReactiveHandler;
import com.tmall.recommend.biz.model.context.RpmRequestContext;
import com.tmall.wireless.tac.client.common.TacResult;
import io.reactivex.Flowable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SampleProcessor extends RpmReactiveHandler<List<Map<String, Object>>> {


    @Override
    public Flowable<TacResult<List<Map<String, Object>>>> rpmExecuteFlowable(RpmRequestContext rpmRequestContext) throws Exception {
        return Flowable.empty();
    }
}
