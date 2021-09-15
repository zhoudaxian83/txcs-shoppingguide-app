package com.tmall.wireless.tac.biz.processor.extremeItem.domain;

import lombok.Data;

import java.util.Map;

@Data
public class ItemGmvGroupMap {
    private Map<Long, ItemGmvGroup> itemGmvGroupMap;

    public double raceValueOf(Long groupNo) {
        return itemGmvGroupMap.get(groupNo).ranceValue();
    }

}
