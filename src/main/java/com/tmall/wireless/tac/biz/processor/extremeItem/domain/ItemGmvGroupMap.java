package com.tmall.wireless.tac.biz.processor.extremeItem.domain;

import lombok.Data;

import java.util.Map;

@Data
public class ItemGmvGroupMap {
    private Map<Integer, ItemGmvGroup> itemGmvGroupMap;

    public double raceValueOf(Integer groupNo) {
        return itemGmvGroupMap.get(groupNo).ranceValue();
    }

}
