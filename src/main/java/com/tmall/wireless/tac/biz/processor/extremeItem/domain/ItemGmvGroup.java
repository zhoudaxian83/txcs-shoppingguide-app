package com.tmall.wireless.tac.biz.processor.extremeItem.domain;

import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

@Data
public class ItemGmvGroup {
    private Integer groupNo;
    private List<ItemGmv> itemGmvList;

    public double ranceValue() {
        if(CollectionUtils.isEmpty(itemGmvList)) {
            return 0;
        }
        return itemGmvList.stream().mapToDouble(ItemGmv::raceValue).sum();
    }

    public double lastNDaysGmvSum() {
        if(CollectionUtils.isEmpty(itemGmvList)) {
            return 0;
        }
        return itemGmvList.stream().mapToDouble(ItemGmv::lastNDaysGmvSum).sum();
    }

    public double last1HourGmvSum() {
        if(CollectionUtils.isEmpty(itemGmvList)) {
            return 0;
        }
        return itemGmvList.stream().mapToDouble(ItemGmv::last1HourGmv).sum();
    }
}
