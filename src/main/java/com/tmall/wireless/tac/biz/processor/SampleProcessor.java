package com.tmall.wireless.tac.biz.processor;

import com.alibaba.fastjson.JSON;


import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SampleProcessor extends RpmReactiveHandler<String> {





    @Override
    public Flowable<TacResult<String>> executeFlowable(Context context) throws Exception {
        return Flowable.just(TacResult.newResult(
                "success"
        ));
    }

}
