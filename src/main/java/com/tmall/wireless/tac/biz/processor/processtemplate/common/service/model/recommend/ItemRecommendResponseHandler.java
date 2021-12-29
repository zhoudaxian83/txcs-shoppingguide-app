package com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend;

import com.tmall.txcs.gs.model.model.dto.RecommendResponseEntity;
import com.tmall.txcs.gs.model.model.dto.tpp.RecommendItemEntityDTO;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.ProcessTemplateContext;

@FunctionalInterface
public interface ItemRecommendResponseHandler {
    RecommendModel handle(RecommendResponseEntity<RecommendItemEntityDTO> responseEntity, ProcessTemplateContext context, Integer pageSize);
}
