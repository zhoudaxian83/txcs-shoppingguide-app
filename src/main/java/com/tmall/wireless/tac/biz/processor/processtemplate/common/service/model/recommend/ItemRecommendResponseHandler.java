package com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend;

import com.tmall.wireless.store.spi.recommend.model.RecommendItemEntityDTO;
import com.tmall.wireless.store.spi.recommend.model.RecommendResponseEntity;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.ProcessTemplateContext;

@FunctionalInterface
public interface ItemRecommendResponseHandler {
    RecommendModel handle(RecommendResponseEntity<RecommendItemEntityDTO> responseEntity, ProcessTemplateContext context, Integer pageSize);
}
