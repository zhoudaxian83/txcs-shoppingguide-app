package com.tmall.wireless.tac.biz.processor.alipay;

import com.alibaba.fastjson.JSON;
import com.alipay.recmixer.common.service.facade.model.MixerCollectRecRequest;
import com.alipay.recmixer.common.service.facade.model.MixerCollectRecResult;
import com.alipay.tradecsa.common.service.spi.request.MiddlePageSPIRequest;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.wireless.tac.biz.processor.alipay.service.IAliPayService;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


import java.util.Optional;

/**
 * Created by yangqing.byq on 2021/8/1.
 */
@Component
public class AlipayFirstPageHandler extends RpmReactiveHandler<MixerCollectRecResult> {


    @Autowired
    IAliPayService aliPayServiceImpl;

    @Override
    public Flowable<TacResult<MixerCollectRecResult>> executeFlowable(Context context) throws Exception {

        MixerCollectRecResult mixerCollectRecResult = new MixerCollectRecResult();

        return aliPayServiceImpl.processFirstPage(context).map(
                re -> TacResult.newResult(re)
        );

    }
}
