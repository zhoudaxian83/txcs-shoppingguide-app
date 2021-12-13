package com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend;

import com.tmall.wireless.store.spi.recommend.model.RecommendEntityDTO;
import com.tmall.wireless.store.spi.recommend.model.RecommendResponseEntity;

public interface RecommendResponseHandler<IN extends RecommendEntityDTO, OUT extends ItemSetRecommendModel> {
    OUT handle(RecommendResponseEntity<IN> responseEntity);
}
