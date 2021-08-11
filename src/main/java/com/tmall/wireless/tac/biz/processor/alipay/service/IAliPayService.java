package com.tmall.wireless.tac.biz.processor.alipay.service;

import com.alipay.recmixer.common.service.facade.model.MixerCollectRecRequest;
import com.alipay.recmixer.common.service.facade.model.MixerCollectRecResult;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;

/**
 * Created by yangqing.byq on 2021/8/6.
 */
public interface IAliPayService {
    Flowable<MixerCollectRecResult> processFirstPage(Context context, MixerCollectRecRequest mixerCollectRecRequest);
}
