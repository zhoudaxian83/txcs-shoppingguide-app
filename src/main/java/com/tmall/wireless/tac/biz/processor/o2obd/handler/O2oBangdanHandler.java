package com.tmall.wireless.tac.biz.processor.o2obd.handler;

import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.txcs.gs.framework.model.ContentVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.FirstScreenMindContentScene;
import com.tmall.wireless.tac.biz.processor.o2obd.service.O2oBangdanService;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author haixiao.zhang
 * @date 2021/6/22
 */
public class O2oBangdanHandler extends RpmReactiveHandler<SgFrameworkResponse<ContentVO>> {

    @Autowired
    O2oBangdanService o2oBangdanService;

    @Override
    public Flowable<TacResult<SgFrameworkResponse<ContentVO>>> executeFlowable(Context context) throws Exception {
        return o2oBangdanService.recommend(context);
    }
}
