package com.tmall.wireless.tac.biz.processor.newTemplate;


import com.tmall.tcls.gs.sdk.framework.model.EntityVO;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextItem;
import com.tmall.tcls.gs.sdk.framework.service.SgFrameworkServiceItem;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by yangqing.byq on 2021/7/15.
 */
@Component
public class NewTemplateTestHandler extends RpmReactiveHandler<SgFrameworkResponse<ItemEntityVO>> {

    @Resource
    SgFrameworkServiceItem sgFrameworkServiceItem;

    @Override
    public Flowable<TacResult<SgFrameworkResponse<ItemEntityVO>>> executeFlowable(Context context) throws Exception {
        SgFrameworkContextItem sgFrameworkContextItem = new SgFrameworkContextItem();
        return sgFrameworkServiceItem.recommend0(sgFrameworkContextItem).map(
                TacResult::newResult
        );
    }
}
