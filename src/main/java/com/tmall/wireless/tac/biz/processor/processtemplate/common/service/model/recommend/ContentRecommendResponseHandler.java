package com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend;

import com.tmall.txcs.gs.model.model.dto.RecommendResponseEntity;
import com.tmall.txcs.gs.model.model.dto.tpp.RecommendContentEntityDTO;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.ProcessTemplateContext;

@FunctionalInterface
public interface ContentRecommendResponseHandler {
    RecommendModel handle(RecommendResponseEntity<RecommendContentEntityDTO> responseEntity, ProcessTemplateContext context, Integer pageSize, Integer perContentSize);
}
