package com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend;

import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 圈品集推荐模型，根据圈品集推商品的数据模型
 */
@Data
public class ItemSetRecommendModel implements RecommendModel {
    private List<ItemSetItems> itemSetItemsList;

    @Override
    public List<Long> fetchAllItemIds() {
        if(CollectionUtils.isEmpty(itemSetItemsList)) {
            return new ArrayList<>();
        }
        return itemSetItemsList.stream()
                .flatMap(itemSetItems -> itemSetItems.getItems().stream()).map(Item::getItemId)
                .distinct()
                .collect(Collectors.toList());
    }
}
