package com.tmall.wireless.tac.biz.processor.alipay.service.impl;

import lombok.Data;
import org.springframework.stereotype.Service;

@Data
public class TaobaoUserInfoDTO {
    Long userId;
    Long smAreaId;
    String logicAreaId;
}
