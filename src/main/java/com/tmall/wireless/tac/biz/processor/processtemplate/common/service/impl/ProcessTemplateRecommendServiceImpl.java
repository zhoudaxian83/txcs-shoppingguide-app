package com.tmall.wireless.tac.biz.processor.processtemplate.common.service.impl;

import com.tmall.tmallwireless.tac.spi.context.SPIResult;
import com.tmall.wireless.store.spi.recommend.RecommendSpi;
import com.tmall.wireless.store.spi.recommend.model.RecommendContentEntityDTO;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.store.spi.recommend.model.RecommendResponseEntity;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.ProcessTemplateContext;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.ProcessTemplateRecommendService;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend.RecommendModel;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend.RecommendResponseHandler;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.util.MetricsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
public class ProcessTemplateRecommendServiceImpl implements ProcessTemplateRecommendService {

    @Autowired
    private RecommendSpi recommendSpi;

    @Override
    public RecommendModel recommendContent(Long appId, ProcessTemplateContext context, Map<String, String> params, RecommendResponseHandler handler) {
        long mainProcessStart = System.currentTimeMillis();
        try {
            RecommendRequest recommendRequest = new RecommendRequest();
            recommendRequest.setAppId(appId);
            recommendRequest.setUserId(context.getUserId());
            recommendRequest.setParams(params);
            recommendRequest.setLogResult(false);
            SPIResult<RecommendResponseEntity<RecommendContentEntityDTO>> spiResult = recommendSpi.recommendContent(recommendRequest);
            if (spiResult.isSuccess()) {
                MetricsUtil.recommendSuccess(context, mainProcessStart);
                return handler.handle(spiResult.getData(), context, Integer.valueOf(params.get("pageSize")), Integer.valueOf(params.get("itemCountPerContent")));
            } else {
                MetricsUtil.recommendFail(context, spiResult.getMsgInfo());
            }
        } catch (Exception e) {
            MetricsUtil.recommendException(context, e);
        }
        return null;
    }
}
