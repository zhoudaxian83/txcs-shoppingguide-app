package com.tmall.wireless.tac.biz.processor.wzt.model;

import java.util.List;
import java.util.Map;

import com.tmall.aself.shoppingguide.client.ResultDTO;
import lombok.Data;

@Data
public class ItemLimitResult extends ResultDTO {

    private Map<Long, List<ItemLimitDTO>> limitInfo;

}
