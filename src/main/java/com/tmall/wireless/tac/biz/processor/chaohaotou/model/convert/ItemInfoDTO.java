package com.tmall.wireless.tac.biz.processor.chaohaotou.model.convert;

import java.util.Map;

import com.google.common.collect.Maps;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import lombok.Data;

/**
 * @Author: luoJunChong
 * @Date: 2021/5/20 14:12
 * description:
 */
@Data
public class ItemInfoDTO {
    ItemEntity itemEntity;

    /**
     * itemInfoSource - ItemInfoBySourceDTO
     */
    public Map<String, ItemInfoBySourceDTO> itemInfos = Maps.newHashMap();
}
