package com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend;

import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品推荐模型
 */
@Data
public class ItemRecommendModel implements RecommendModel {
    private List<Item> items;

    @Override
    public List<Long> fetchAllItemIds() {
        if(CollectionUtils.isEmpty(items)) {
            return new ArrayList<>();
        }
        return items.stream().map(Item::getItemId).collect(Collectors.toList());
    }
}
