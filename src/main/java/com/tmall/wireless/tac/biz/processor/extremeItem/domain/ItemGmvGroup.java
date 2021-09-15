package com.tmall.wireless.tac.biz.processor.extremeItem.domain;

import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

@Data
public class ItemGmvGroup {
    private Long groupNo;
    private List<ItemGmv> itemGmvList;

    public double ranceValue() {
        if(CollectionUtils.isEmpty(itemGmvList)) {
            return 0;
        }
        return itemGmvList.stream().mapToDouble(ItemGmv::raceValue).sum();
    }
}
