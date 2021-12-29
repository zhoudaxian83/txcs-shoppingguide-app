package com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend;

import com.tmall.wireless.store.spi.recommend.model.RecommendContentEntityDTO;
import com.tmall.wireless.store.spi.recommend.model.RecommendItemEntityDTO;
import com.tmall.wireless.store.spi.recommend.model.RecommendResponseEntity;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.ProcessTemplateContext;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.util.MetricsUtil;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class ItemSetContentRecommendModelHandler implements ContentRecommendResponseHandler {

    @Override
    public RecommendModel handle(RecommendResponseEntity<RecommendContentEntityDTO> responseEntity, ProcessTemplateContext context, Integer pageSize, Integer perContentSize) {
        ItemSetRecommendModel itemSetRecommendModel = new ItemSetRecommendModel();
        List<ItemSetItems> itemSetItemsList = new ArrayList<>();
        itemSetRecommendModel.setItemSetItemsList(itemSetItemsList);
        List<RecommendContentEntityDTO> recommendContentEntityDTOList = responseEntity.getResult();
        if(CollectionUtils.isNotEmpty(recommendContentEntityDTOList)) {
            for (RecommendContentEntityDTO recommendContentEntityDTO : recommendContentEntityDTOList) {
                if(itemSetItemsList.size() >= pageSize) {
                    MetricsUtil.contentExceeded(context, pageSize, recommendContentEntityDTOList.size());
                    break;
                }
                ItemSetItems itemSetItems = new ItemSetItems();
                itemSetItems.setItemSetId(recommendContentEntityDTO.getContentId());
                List<RecommendItemEntityDTO> recommendItemEntityDTOs = recommendContentEntityDTO.getItems();
                List<Item> items = new ArrayList<>();
                itemSetItems.setItems(items);
                if(CollectionUtils.isNotEmpty(recommendItemEntityDTOs)) {
                    for (RecommendItemEntityDTO recommendItemEntityDTO : recommendItemEntityDTOs) {
                        if(items.size() >= perContentSize) {
                            MetricsUtil.itemExceeded(context, perContentSize, recommendItemEntityDTOs.size());
                            break;
                        }
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
