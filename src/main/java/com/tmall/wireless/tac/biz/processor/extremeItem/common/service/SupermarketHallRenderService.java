package com.tmall.wireless.tac.biz.processor.extremeItem.common.service;

import com.tmall.aselfcaptain.item.model.ItemDTO;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.SupermarketHallContext;

import java.util.List;
import java.util.Map;

public interface SupermarketHallRenderService {
    Map<Long, ItemDTO> batchQueryItem(List<Long> itemIdList, SupermarketHallContext supermarketHallContext);
}
