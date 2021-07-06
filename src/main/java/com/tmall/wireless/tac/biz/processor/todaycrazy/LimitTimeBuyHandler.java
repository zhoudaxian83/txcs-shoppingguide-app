package com.tmall.wireless.tac.biz.processor.todaycrazy;

import java.util.List;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import com.tmall.wireless.tac.client.handler.TacReactiveHandler4Ald;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author guijian
 */
@Component
public class LimitTimeBuyHandler extends TacReactiveHandler4Ald {

    @Autowired
    LimitTimeBuyScene limitTimeBuyScene;

    @Override
    public Flowable<TacResult<List<GeneralItem>>> executeFlowable(RequestContext4Ald requestContext4Ald)
        throws Exception {
        return limitTimeBuyScene.recommend(requestContext4Ald);
    }
}
