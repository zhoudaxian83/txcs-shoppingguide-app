package com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend;

import com.tmall.wireless.store.spi.recommend.model.RecommendContentEntityDTO;
import com.tmall.wireless.store.spi.recommend.model.RecommendEntityDTO;
import com.tmall.wireless.store.spi.recommend.model.RecommendItemEntityDTO;
import com.tmall.wireless.store.spi.recommend.model.RecommendResponseEntity;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class ItemSetRecommendModelHandler<T extends RecommendContentEntityDTO> implements RecommendResponseHandler<T>{

    public ItemSetRecommendModel handle(RecommendResponseEntity<T> responseEntity) {
        ItemSetRecommendModel itemSetRecommendModel = new ItemSetRecommendModel();
        List<ItemSetItems> itemSetItemsList = new ArrayList<>();
        itemSetRecommendModel.setItemSetItemsList(itemSetItemsList);
        List<T> recommendContentEntityDTOList = responseEntity.getResult();
        if(CollectionUtils.isNotEmpty(recommendContentEntityDTOList)) {
            for (T recommendContentEntityDTO : recommendContentEntityDTOList) {
                ItemSetItems itemSetItems = new ItemSetItems();
                itemSetItems.setItemSetId(recommendContentEntityDTO.getContentId());
                List<RecommendItemEntityDTO> recommendItemEntityDTOs = recommendContentEntityDTO.getItems();
                List<Item> items = new ArrayList<>();
                itemSetItems.setItems(items);
                if(CollectionUtils.isNotEmpty(recommendItemEntityDTOs)) {
                    for (RecommendItemEntityDTO recommendItemEntityDTO : recommendItemEntityDTOs) {
                        Item item = new Item();
                        item.setItemId(recommendItemEntityDTO.getItemId());
                        items.add(item);
                    }
                }
                itemSetItemsList.add(itemSetItems);
            }
        }
        return itemSetRecommendModel;
    }
}
