package com.tmall.wireless.tac.biz.processor.extremeItem.common.service;

import com.tmall.aselfcaptain.item.model.ItemDTO;

import java.util.List;
import java.util.Map;

/**
 * 上下文映射方式——共享内核
 */
public interface SupermarketHallRenderService {
    Map<Long, ItemDTO> batchQueryItem(List<Long> itemIdList);
}
