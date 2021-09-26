package com.tmall.wireless.tac.biz.processor.extremeItem.service;

import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemConfigGroups;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemGmvGroupMap;

import java.util.List;

public interface ItemGmvService {
    ItemGmvGroupMap queryGmv(ItemConfigGroups itemConfigGroups, List<Long> itemIdList, int days);
}
