package com.tmall.wireless.tac.biz.processor.processtemplate.common.service;

import com.tmall.wireless.tac.biz.processor.processtemplate.common.ProcessTemplateContext;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend.ItemRecommendResponseHandler;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend.RecommendModel;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend.ContentRecommendResponseHandler;

import java.util.Map;

public interface ProcessTemplateRecommendService {
    RecommendModel recommendContent(Long appId, ProcessTemplateContext context, Map<String, String> params, ContentRecommendResponseHandler handler);
    RecommendModel recommendItem(Long appId, ProcessTemplateContext context, Map<String, String> params, ItemRecommendResponseHandler handler);
}
