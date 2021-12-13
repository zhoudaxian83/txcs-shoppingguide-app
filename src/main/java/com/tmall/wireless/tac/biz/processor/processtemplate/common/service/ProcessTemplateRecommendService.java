package com.tmall.wireless.tac.biz.processor.processtemplate.common.service;

import com.tmall.wireless.store.spi.recommend.model.RecommendContentEntityDTO;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.ProcessTemplateContext;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend.RecommendModel;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend.RecommendResponseHandler;

import java.util.Map;

public interface ProcessTemplateRecommendService {
    RecommendModel recommendContent(Long appId, ProcessTemplateContext context, Map<String, String>params, RecommendResponseHandler<RecommendContentEntityDTO> handler);
}
