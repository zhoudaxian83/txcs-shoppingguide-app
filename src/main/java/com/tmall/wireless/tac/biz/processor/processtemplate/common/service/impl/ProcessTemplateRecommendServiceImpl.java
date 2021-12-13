package com.tmall.wireless.tac.biz.processor.processtemplate.common.service.impl;

import com.tmall.tmallwireless.tac.spi.context.SPIResult;
import com.tmall.wireless.store.spi.recommend.RecommendSpi;
import com.tmall.wireless.store.spi.recommend.model.RecommendContentEntityDTO;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.store.spi.recommend.model.RecommendResponseEntity;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.ProcessTemplateContext;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.ProcessTemplateRecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
public class ProcessTemplateRecommendServiceImpl implements ProcessTemplateRecommendService {

    @Autowired
    private RecommendSpi recommendSpi;

    @Override
    public Object recommendContent(Long appId, ProcessTemplateContext context, Map<String, String> params) {
        RecommendRequest recommendRequest = new RecommendRequest();
        recommendRequest.setAppId(appId);
        recommendRequest.setUserId(context.getUserId());
        recommendRequest.setParams(params);
        recommendRequest.setLogResult(false);
        SPIResult<RecommendResponseEntity<RecommendContentEntityDTO>> spiResult = recommendSpi.recommendContent(recommendRequest);
        if(spiResult.isSuccess()) {
            return spiResult.getData();
        } else {
            //TODO 加日志
            return null;
        }
    }
}
