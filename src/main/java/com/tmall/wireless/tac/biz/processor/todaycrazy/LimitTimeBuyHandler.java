package com.tmall.wireless.tac.biz.processor.todaycrazy;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.alibaba.fastjson.JSON;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import com.tmall.wireless.tac.client.handler.TacReactiveHandler4Ald;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author guijian
 */
@Slf4j
@Component
public class LimitTimeBuyHandler extends TacReactiveHandler4Ald {

    @Autowired
    LimitTimeBuyScene limitTimeBuyScene;

    @Override
    public Flowable<TacResult<List<GeneralItem>>> executeFlowable(RequestContext4Ald requestContext4Ald) throws Exception {
        log.error("entryContext." + "TODAY_CRAZY_LIMIT_TIME_BUY" + ",context:{}", JSON.toJSONString(requestContext4Ald));
        return limitTimeBuyScene.recommend(requestContext4Ald);
    }
}
