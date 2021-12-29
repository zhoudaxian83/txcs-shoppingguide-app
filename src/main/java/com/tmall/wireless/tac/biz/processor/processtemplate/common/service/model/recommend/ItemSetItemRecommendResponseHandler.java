package com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend;

import com.tmall.wireless.store.spi.recommend.model.RecommendItemEntityDTO;
import com.tmall.wireless.store.spi.recommend.model.RecommendResponseEntity;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.ProcessTemplateContext;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.util.MetricsUtil;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class ItemSetItemRecommendResponseHandler implements ItemRecommendResponseHandler {
    @Override
    public RecommendModel handle(RecommendResponseEntity<RecommendItemEntityDTO> responseEntity, ProcessTemplateContext context, Integer pageSize) {
        ItemRecommendModel itemRecommendModel = new ItemRecommendModel();
        List<Item> items = new ArrayList<>();
        itemRecommendModel.setItems(items);
        List<RecommendItemEntityDTO> recommendItemEntityDTOList = responseEntity.getResult();
        if(CollectionUtils.isNotEmpty(recommendItemEntityDTOList)) {
            for (RecommendItemEntityDTO recommendItemEntityDTO : recommendItemEntityDTOList) {
                if(items.size() >= pageSize) {
                    MetricsUtil.itemExceeded(context, pageSize, recommendItemEntityDTOList.size());
                    break;
                }
                Item item = new Item();
                item.setItemId(recommendItemEntityDTO.getItemId());
                items.add(item);
            }
        }
        return itemRecommendModel;
    }
}
