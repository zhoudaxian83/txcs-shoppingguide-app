package com.tmall.wireless.tac.biz.processor;

import com.tmall.recommend.biz.RpmReactiveHandler;
import com.tmall.wireless.tac.client.common.TacResult;
import io.reactivex.Flowable;

import java.util.List;
import java.util.Map;

public class SampleProcessor extends RpmReactiveHandler<List<Map<String, Object>>> {


    @Override
    public Flowable<TacResult<List<Map<String, Object>>>> rpmExecuteFlowable(com.tmall.recommend.biz.model.context.RpmRequestContext rpmRequestContext) throws Exception {
        return Flowable.empty();
    }
}
