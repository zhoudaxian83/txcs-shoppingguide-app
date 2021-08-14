package com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.impl;

import com.alipay.tradecsa.common.service.spi.response.PageFloorAtomicResultDTO;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.AtomicCardProcessRequest;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.IAtomicCardProcessor;
import org.springframework.stereotype.Service;

// 券原子模板
@Service
public class VoucherAtomicCardProcessor implements IAtomicCardProcessor {



    @Override
    public String atomicCardId() {
        return "CSDTemplate_VoucherCard";
    }

    @Override
    public PageFloorAtomicResultDTO process(AtomicCardProcessRequest atomicCardProcessRequest) {
        return null;
    }
}
