package com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic;

import com.alipay.tradecsa.common.service.spi.response.PageFloorAtomicResultDTO;
import com.alipay.tradecsa.common.service.spi.response.PageFloorResultDTO;

public interface IAtomicCardProcessor {
    public String atomicCardId();

    public PageFloorAtomicResultDTO process(AtomicCardProcessRequest atomicCardProcessRequest);
}
