package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.model;

import com.tmall.tcls.gs.sdk.framework.model.context.ItemEntity;
import lombok.Data;

@Data
public class TodayCrazySortItemEntity {
    private ItemEntity itemEntity;
    private Long index;
}
