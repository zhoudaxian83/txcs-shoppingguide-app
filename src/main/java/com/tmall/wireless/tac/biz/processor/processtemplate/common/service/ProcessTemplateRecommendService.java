package com.tmall.wireless.tac.biz.processor.processtemplate.common.service;

import com.tmall.wireless.tac.biz.processor.processtemplate.common.ProcessTemplateContext;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend.ItemSetRecommendModel2;

import java.util.Map;

public interface ProcessTemplateRecommendService {
    void recommend();
    ItemSetRecommendModel2 recommendContent(Long appId, ProcessTemplateContext context, Map<String, String> params);
}
