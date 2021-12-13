package com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend;

import lombok.Data;

import java.util.List;

@Data
public class ItemSetItems {
    private Long itemSetId;
    private List<Item> items;
}
