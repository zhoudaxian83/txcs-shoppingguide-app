package com.tmall.wireless.tac.biz.processor.newproduct.handler;

import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.txcs.gs.framework.model.ContentVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.wireless.tac.biz.processor.newproduct.service.SxlContentRecService;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @author haixiao.zhang
 * @date 2021/6/8
 */
@Component
public class SxlContentHandler  extends RpmReactiveHandler<SgFrameworkResponse<ContentVO>> {

    @Autowired
    private SxlContentRecService sxlContentRecService;


    @Override
    public Flowable<TacResult<SgFrameworkResponse<ContentVO>>> executeFlowable(Context context) throws Exception {
        return sxlContentRecService.recommend(context);
    }

}
