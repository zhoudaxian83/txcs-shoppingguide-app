package com.tmall.wireless.tac.biz.processor.extremeItem.domain.service;

import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemConfig;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemConfigGroup;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemConfigGroups;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ItemPickService {
    public Map<Integer, ItemConfig> pickItems(ItemConfigGroups itemConfigGroups, Map<Long, Boolean> itemSoldOutMap) {
        Map<Integer, ItemConfig> result = new HashMap<>();
        for (ItemConfigGroup itemConfigGroup : itemConfigGroups.getItemConfigGroups()) {
            //库存过滤
            itemConfigGroup.inventoryFilter(itemSoldOutMap);
            //曝光为0过滤
            itemConfigGroup.exposureRateFilter();
            //挑选最终商品
            result.put(itemConfigGroup.getGroupNo(), itemConfigGroup.pickItem());
        }
        return result;
    }
}
