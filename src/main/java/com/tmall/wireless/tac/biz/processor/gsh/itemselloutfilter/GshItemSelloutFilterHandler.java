package com.tmall.wireless.tac.biz.processor.gsh.itemselloutfilter;

import java.util.List;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;

import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import com.tmall.wireless.tac.client.handler.TacReactiveHandler4Ald;
import io.reactivex.Flowable;
import org.springframework.stereotype.Component;

/**
 * @author wangguohui
 */
@Component
public class GshItemSelloutFilterHandler extends TacReactiveHandler4Ald {

    @Override
    public Flowable<TacResult<List<GeneralItem>>> executeFlowable(RequestContext4Ald requestContext4Ald)
        throws Exception {

        return null;
    }
}
