package com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.impl;

import com.alipay.tradecsa.common.service.spi.response.PageFloorAtomicResultDTO;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.AtomicCardProcessRequest;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.IAtomicCardProcessor;
import org.springframework.stereotype.Service;

// feeds流
@Service
public class FeedsAtomicCardProcessor implements IAtomicCardProcessor {
    @Override
    public String atomicCardId() {
        return "COMMON_DetailMutablecard";
    }

    @Override
    public PageFloorAtomicResultDTO process(AtomicCardProcessRequest atomicCardProcessRequest) {
        return null;
    }
}
