package com.tmall.wireless.tac.biz.processor.extremeItem.service.entity;

import lombok.Data;

@Data
public class GmvEntity {
    //"gmv", "item_id", "window_start", "window_end"
    private Double gmv;
    private Long itemId;
    private String windowStart;
    private String windowEnd;

}
