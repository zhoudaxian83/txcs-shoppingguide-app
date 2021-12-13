package com.tmall.wireless.tac.biz.processor.processtemplate.timelimitedseckill;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.tmall.wireless.store.spi.recommend.model.RecommendContentEntityDTO;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.ProcessTemplateContext;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.ProcessTemplateRecommendService;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend.ItemSetRecommendModel;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend.ItemSetRecommendModelHandler;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend.RecommendResponseHandler;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import com.tmall.wireless.tac.client.handler.TacReactiveHandler4Ald;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TimeLimitedSecKillHandler extends TacReactiveHandler4Ald {

    @Autowired
    private ProcessTemplateRecommendService recommendService;
    @Autowired
    private TacLogger tacLogger;

    @Override
    public Flowable<TacResult<List<GeneralItem>>> executeFlowable(RequestContext4Ald requestContext4Ald) throws Exception {
        ProcessTemplateContext context = new ProcessTemplateContext();
        context.setUserId(1034513083L);
        Map<String, String> params = new HashMap<>();
        params.put("contentType", "3");
        params.put("itemSetIdList", "415609,415620");
        RecommendResponseHandler<RecommendContentEntityDTO, ItemSetRecommendModel> handler = new ItemSetRecommendModelHandler<>();
        tacLogger.warn("aaaa");
        //RecommendModel recommendModel = recommendService.recommendContent(21557L, context, params, handler);
        //tacLogger.warn("recommendResponse" + JSON.toJSONString(recommendModel.getAllItemIds()));
        return Flowable.just(TacResult.newResult(null));
    }
}