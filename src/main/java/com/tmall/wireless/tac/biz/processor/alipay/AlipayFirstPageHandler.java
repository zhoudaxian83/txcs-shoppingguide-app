package com.tmall.wireless.tac.biz.processor.alipay;

import com.alipay.recmixer.common.service.facade.model.MixerCollectRecRequest;
import com.alipay.recmixer.common.service.facade.model.MixerCollectRecResult;
import com.alipay.tradecsa.common.service.spi.request.MiddlePageSPIRequest;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Created by yangqing.byq on 2021/8/1.
 */
@Component
public class AlipayFirstPageHandler extends RpmReactiveHandler<MixerCollectRecResult> {



    @Override
    public Flowable<TacResult<MixerCollectRecResult>> executeFlowable(Context context) throws Exception {

        MixerCollectRecResult mixerCollectRecResult = new MixerCollectRecResult();
        Object param = Optional.of(context).map(Context::getParams).map(m -> m.get(AlipayMiddlePageHandler.PARAM_KEY)).orElse(null);

        if (!(param instanceof MixerCollectRecRequest)) {
            mixerCollectRecResult.setErrorCode("PARAMS_IS_NULL");
            return Flowable.just(TacResult.newResult(mixerCollectRecResult));
        }
        MixerCollectRecRequest mixerCollectRecRequest = (MixerCollectRecRequest) param;


        mixerCollectRecResult.setErrorMsg(mixerCollectRecRequest.getUtdid());
        return Flowable.just(TacResult.newResult(mixerCollectRecResult));
    }
}
