package com.tmall.wireless.tac.biz.processor.wzt.model;

import java.util.List;

import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import lombok.Data;

/**
 * @Author: luoJunChong
 * @Date: 2021/5/18 14:30
 * description:
 */
@Data
public class DataContext {
    private List<Long> items;
    private OriginDataDTO<ItemEntity> originDataDTO;
    private Long index;
    private Long pageSize;

}
