package com.tmall.wireless.tac.biz.processor.wzt.model;

import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import lombok.Data;

@Data
public class SortItemEntity {
    private ItemEntity itemEntity;
    private Long index;
}
