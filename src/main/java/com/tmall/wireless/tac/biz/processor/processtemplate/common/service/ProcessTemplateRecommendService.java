package com.tmall.wireless.tac.biz.processor.processtemplate.common.service;

import com.tmall.wireless.tac.biz.processor.processtemplate.common.ProcessTemplateContext;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend.ItemSetRecommendModel;

import java.util.Map;

public interface ProcessTemplateRecommendService {
    void recommend();
    void recommendContent(Long appId, ProcessTemplateContext context, Map<String, String> params);
}
