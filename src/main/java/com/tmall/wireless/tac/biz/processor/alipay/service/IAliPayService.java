package com.tmall.wireless.tac.biz.processor.alipay.service;

import com.alipay.recmixer.common.service.facade.model.MixerCollectRecRequest;
import com.alipay.recmixer.common.service.facade.model.MixerCollectRecResult;

/**
 * Created by yangqing.byq on 2021/8/6.
 */
public interface IAliPayService {
    MixerCollectRecResult processFirstPage(MixerCollectRecRequest mixerCollectRecRequest);
}
