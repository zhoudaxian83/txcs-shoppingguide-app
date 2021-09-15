package com.tmall.wireless.tac.biz.processor.extremeItem.domain.service;

import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemConfigGroupList;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemGmvGroupMap;
import com.tmall.wireless.tac.biz.processor.extremeItem.service.ItemGmvService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

public class GroupSortService {
    private ItemGmvService itemGmvService;

    public void groupSort(ItemConfigGroupList itemConfigGroups) {
        if(itemConfigGroups.forceSort()) {
            itemConfigGroups.sortGroup();
            return;
        }
        raceSort(itemConfigGroups);
    }

    private void raceSort(ItemConfigGroupList itemConfigGroups) {
        ItemGmvGroupMap itemGmvGroupMap = itemGmvService.queryGmv();
        itemConfigGroups.sortGroup(itemGmvGroupMap);
    }
}
