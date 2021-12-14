package com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend;

import com.tmall.wireless.store.spi.recommend.model.RecommendContentEntityDTO;
import com.tmall.wireless.store.spi.recommend.model.RecommendResponseEntity;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend.ItemSetRecommendModel;

@FunctionalInterface
public interface RecommendResponseHandler {
    RecommendModel handle(RecommendResponseEntity<RecommendContentEntityDTO> responseEntity);
}
