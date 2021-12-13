package com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 圈品集推荐模型，根据圈品集推商品的数据模型
 */
public class ItemSetRecommendModel2 {
    private List<ItemSetItems> itemSetItemsList;

    public List<Long> getAllItemIds() {
        if(CollectionUtils.isEmpty(itemSetItemsList)) {
            return new ArrayList<>();
        }
        /*return itemSetItemsList.stream()
                .flatMap(itemSetItems -> itemSetItems.getItems().stream()).map(Item::getItemId)
                .distinct()
                .collect(Collectors.toList());*/
        return null;
    }
}
