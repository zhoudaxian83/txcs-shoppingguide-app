package com.tmall.wireless.tac.biz.processor.wzt.model;

import lombok.Data;

@Data
public class ItemLimitDTO  {

    //商品id
    private Long itemId;

    private Long skuId;

    //总体限购
    private Long totalLimit;

    //已经售卖的件数
    private Long usedCount;

    //用户限购信息
    private Long userLimit;

    //用户已经消费
    private Long userUsedCount;
}
