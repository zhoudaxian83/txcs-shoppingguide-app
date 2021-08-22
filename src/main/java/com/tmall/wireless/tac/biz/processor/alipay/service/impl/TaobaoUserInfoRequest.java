package com.tmall.wireless.tac.biz.processor.alipay.service.impl;

import lombok.Data;

@Data
public class TaobaoUserInfoRequest {
    String alipayUserId;
    Long alipayCityCode;
}
