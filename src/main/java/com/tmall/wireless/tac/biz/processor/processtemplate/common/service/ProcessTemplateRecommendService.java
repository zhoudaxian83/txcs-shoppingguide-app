package com.tmall.wireless.tac.biz.processor.processtemplate.common.service;

import com.tmall.wireless.store.spi.recommend.model.RecommendContentEntityDTO;
import com.tmall.wireless.store.spi.recommend.model.RecommendResponseEntity;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.ProcessTemplateContext;

import java.util.Map;

public interface ProcessTemplateRecommendService {
    RecommendResponseEntity<RecommendContentEntityDTO> recommendContent(Long appId, ProcessTemplateContext context, Map<String, String>params);
}
