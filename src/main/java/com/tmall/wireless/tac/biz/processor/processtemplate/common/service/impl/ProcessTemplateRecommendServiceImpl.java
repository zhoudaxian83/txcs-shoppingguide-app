package com.tmall.wireless.tac.biz.processor.processtemplate.common.service.impl;

import com.tmall.txcs.gs.model.Response;
import com.tmall.txcs.gs.model.model.dto.RecommendResponseEntity;
import com.tmall.txcs.gs.model.model.dto.tpp.RecommendContentEntityDTO;
import com.tmall.txcs.gs.model.model.dto.tpp.RecommendItemEntityDTO;
import com.tmall.txcs.gs.model.spi.model.RecommendRequest;
import com.tmall.txcs.gs.spi.recommend.RecommendSpiV2;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.ProcessTemplateContext;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.ProcessTemplateRecommendService;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend.ContentRecommendResponseHandler;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend.ItemRecommendResponseHandler;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend.RecommendModel;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.util.MetricsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
public class ProcessTemplateRecommendServiceImpl implements ProcessTemplateRecommendService {

    @Autowired
    private RecommendSpiV2 recommendSpiV2;

    @Override
    public RecommendModel recommendContent(Long appId, ProcessTemplateContext context, Map<String, String> params, ContentRecommendResponseHandler handler) {
        long mainProcessStart = System.currentTimeMillis();
        try {
            RecommendRequest recommendRequest = new RecommendRequest();
            recommendRequest.setAppId(appId);
            recommendRequest.setUserId(context.getUserId());
            recommendRequest.setParams(params);
            recommendRequest.setLogResult(false);
            Response<RecommendResponseEntity<RecommendContentEntityDTO>> response = recommendSpiV2.recommendContent(recommendRequest).blockingFirst();
            if (response.isSuccess()) {
                MetricsUtil.recommendSuccess(context, mainProcessStart);
                return handler.handle(response.getValue(), context, Integer.valueOf(params.get("pageSize")), Integer.valueOf(params.get("itemCountPerContent")));
            } else {
                MetricsUtil.recommendFail(context, response.getErrorMsg());
            }
        } catch (Exception e) {
            MetricsUtil.recommendException(context, e);
        }
        return null;
    }

    @Override
    public RecommendModel recommendItem(Long appId, ProcessTemplateContext context, Map<String, String> params, ItemRecommendResponseHandler handler) {
        long mainProcessStart = System.currentTimeMillis();
        try {
            RecommendRequest recommendRequest = new RecommendRequest();
            recommendRequest.setAppId(appId);
            recommendRequest.setUserId(context.getUserId());
            recommendRequest.setParams(params);
            recommendRequest.setLogResult(false);
            Response<RecommendResponseEntity<RecommendItemEntityDTO>> response = recommendSpiV2.recommendItem(recommendRequest).blockingFirst();
            if (response.isSuccess()) {
                MetricsUtil.recommendSuccess(context, mainProcessStart);
                return handler.handle(response.getValue(), context, Integer.valueOf(params.get("pageSize")));
            } else {
                MetricsUtil.recommendFail(context, response.getErrorMsg());
            }
        } catch (Exception e) {
            MetricsUtil.recommendException(context, e);
        }
        return null;
    }
}
