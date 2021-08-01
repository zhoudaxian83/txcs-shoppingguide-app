package com.tmall.wireless.tac.biz.processor.alipay;

import com.alipay.recmixer.common.service.facade.model.MixerCollectRecResult;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;

/**
 * Created by yangqing.byq on 2021/8/1.
 */
public class AlipayMiddlePageHandler extends RpmReactiveHandler<MixerCollectRecResult> {
    @Override
    public Flowable<TacResult<MixerCollectRecResult>> executeFlowable(Context context) throws Exception {
        MixerCollectRecResult mixerCollectRecResult = new MixerCollectRecResult();
        mixerCollectRecResult.setErrorMsg("jinzhou test middle");
        return Flowable.just(TacResult.newResult(mixerCollectRecResult));
    }
}
