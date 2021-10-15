package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.model;

import lombok.Data;

@Data
public class ItemLimitDTO {

    //商品id
    private long itemId;

    private long skuId;

    //总体限购
    private long totalLimit;

    //已经售卖的件数
    private long usedCount;

    //用户限购信息
    private long userLimit;

    //用户已经消费
    private long userUsedCount;
}
