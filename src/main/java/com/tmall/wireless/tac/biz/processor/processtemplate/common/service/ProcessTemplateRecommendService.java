package com.tmall.wireless.tac.biz.processor.processtemplate.common.service;

import com.tmall.wireless.tac.biz.processor.processtemplate.common.ProcessTemplateContext;

import java.util.Map;

public interface ProcessTemplateRecommendService {
    void recommend();
    Object recommendContent(Long appId, ProcessTemplateContext context, Map<String, String> params);
}
