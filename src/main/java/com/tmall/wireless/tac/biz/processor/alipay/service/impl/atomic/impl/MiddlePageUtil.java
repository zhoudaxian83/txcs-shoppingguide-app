package com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.impl;

import com.alipay.tradecsa.common.service.spi.request.MiddlePageClientRequestDTO;
import com.alipay.tradecsa.common.service.spi.request.MiddlePageSPIRequest;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.AtomicCardProcessRequest;

import java.util.Optional;

public class MiddlePageUtil {

    public static boolean isFirstPage(MiddlePageSPIRequest middlePageSPIRequest) {
        return   Optional.ofNullable(middlePageSPIRequest)
                .map(MiddlePageSPIRequest::getMiddlePageClientRequestDTO)
                .map(MiddlePageClientRequestDTO::getPageNo)
                .orElse(1) == 1;
    }
}
