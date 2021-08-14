package com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic;

import com.alipay.tradecsa.common.service.spi.request.PageFloorAtomicDTO;
import lombok.Data;

@Data
public class AtomicCardProcessRequest {
    PageFloorAtomicDTO pageFloorAtomicDTO;
}
