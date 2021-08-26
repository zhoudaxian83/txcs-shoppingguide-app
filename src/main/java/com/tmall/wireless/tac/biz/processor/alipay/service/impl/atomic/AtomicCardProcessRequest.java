package com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.alipay.tradecsa.common.service.spi.request.MiddlePageSPIRequest;
import com.alipay.tradecsa.common.service.spi.request.PageFloorAtomicDTO;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import lombok.Data;

import java.util.List;

@Data
public class AtomicCardProcessRequest {
    PageFloorAtomicDTO pageFloorAtomicDTO;
    GeneralItem aldData;
    List<ItemEntityVO> itemAndContentList;
    MiddlePageSPIRequest middlePageSPIRequest;
    Long userId;
}
