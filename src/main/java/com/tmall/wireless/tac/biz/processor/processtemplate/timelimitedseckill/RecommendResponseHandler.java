package com.tmall.wireless.tac.biz.processor.processtemplate.timelimitedseckill;

import com.tmall.wireless.store.spi.recommend.model.RecommendContentEntityDTO;
import com.tmall.wireless.store.spi.recommend.model.RecommendResponseEntity;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend.ItemSetRecommendModel;

@FunctionalInterface
public interface RecommendResponseHandler {
    ItemSetRecommendModel handle(RecommendResponseEntity<RecommendContentEntityDTO> responseEntity);
}
