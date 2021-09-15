package com.tmall.wireless.tac.biz.processor.extremeItem.domain.service;

import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemConfig;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemConfigGroup;

import java.util.HashMap;
import java.util.Map;

public class ItemPickService {
    public ItemConfig pickItem(ItemConfigGroup itemConfigGroup) {
        //库存过滤
        Map<Long, Boolean> itemSoldOutMap = new HashMap<>();
        itemConfigGroup.inventoryFilter(itemSoldOutMap);
        //曝光为0过滤
        itemConfigGroup.exposureRateFilter();
        //挑选最终商品
        return itemConfigGroup.pickItem();
    }
}
