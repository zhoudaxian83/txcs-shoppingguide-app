package com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend;

import com.tmall.wireless.store.spi.recommend.model.RecommendContentEntityDTO;
import com.tmall.wireless.store.spi.recommend.model.RecommendResponseEntity;

@FunctionalInterface
public interface RecommendResponseHandler {
    ItemSetRecommendModel handle(RecommendResponseEntity<RecommendContentEntityDTO> responseEntity);
}
