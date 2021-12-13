package com.tmall.wireless.tac.biz.processor.processtemplate.common.service.impl;

import com.tmall.wireless.store.spi.recommend.RecommendSpi;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.ProcessTemplateContext;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.ProcessTemplateRecommendService;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend.ItemSetRecommendModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
public class ProcessTemplateRecommendServiceImpl implements ProcessTemplateRecommendService {

    @Autowired
    private RecommendSpi recommendSpi;

    @Override
    public void recommend() {

    }

    @Override
    public ItemSetRecommendModel recommendContent(Long appId, ProcessTemplateContext context, Map<String, String> params) {
        return null;
    }

    /*@Override
    public ItemSetRecommendModel recommendContent(Long appId, ProcessTemplateContext context, Map<String, String> params) {
        RecommendRequest recommendRequest = new RecommendRequest();
        recommendRequest.setAppId(appId);
        recommendRequest.setUserId(context.getUserId());
        recommendRequest.setParams(params);
        recommendRequest.setLogResult(false);
        SPIResult<RecommendResponseEntity<RecommendContentEntityDTO>> spiResult = recommendSpi.recommendContent(recommendRequest);
        if(spiResult.isSuccess()) {
            return null;
        } else {
            //TODO 加日志
            return null;
        }
    }*/
}
