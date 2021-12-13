package com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend;

import com.tmall.wireless.store.spi.recommend.model.RecommendContentEntityDTO;
import com.tmall.wireless.store.spi.recommend.model.RecommendResponseEntity;

public class ItemSetRecommendModelHandler implements RecommendResponseHandler {

    @Override
    public ItemSetRecommendModel2 handle(RecommendResponseEntity<RecommendContentEntityDTO> responseEntity) {
        ItemSetRecommendModel2 itemSetRecommendModel = new ItemSetRecommendModel2();
        /*List<ItemSetItems> itemSetItemsList = new ArrayList<>();
        itemSetRecommendModel.setItemSetItemsList(itemSetItemsList);
        List<RecommendContentEntityDTO> recommendContentEntityDTOList = responseEntity.getResult();
        if(CollectionUtils.isNotEmpty(recommendContentEntityDTOList)) {
            for (RecommendContentEntityDTO recommendContentEntityDTO : recommendContentEntityDTOList) {
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
        }*/
        return itemSetRecommendModel;
    }
}
