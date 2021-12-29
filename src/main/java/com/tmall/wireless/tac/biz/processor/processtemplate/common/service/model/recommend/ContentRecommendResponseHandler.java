package com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend;

import com.tmall.wireless.store.spi.recommend.model.RecommendContentEntityDTO;
import com.tmall.wireless.store.spi.recommend.model.RecommendResponseEntity;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.ProcessTemplateContext;

@FunctionalInterface
public interface ContentRecommendResponseHandler {
    RecommendModel handle(RecommendResponseEntity<RecommendContentEntityDTO> responseEntity, ProcessTemplateContext context, Integer pageSize, Integer perContentSize);
}
