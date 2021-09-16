package com.tmall.wireless.tac.biz.processor.extremeItem.domain.service;

import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemConfigGroups;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemGmvGroupMap;
import com.tmall.wireless.tac.biz.processor.extremeItem.service.ItemGmvService;

public class GroupSortService {
    private ItemGmvService itemGmvService;

    public void groupSort(ItemConfigGroups itemConfigGroups) {
        if(itemConfigGroups.forceSort()) {
            itemConfigGroups.sortGroup();
            return;
        }
        raceSort(itemConfigGroups);
    }

    private void raceSort(ItemConfigGroups itemConfigGroups) {
        ItemGmvGroupMap itemGmvGroupMap = itemGmvService.queryGmv();
        itemConfigGroups.sortGroup(itemGmvGroupMap);
    }
}
