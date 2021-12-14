package com.tmall.wireless.tac.biz.processor.processtemplate.timelimitedseckill;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.alibaba.fastjson.JSON;
import com.tmall.aselfcaptain.item.model.ItemDTO;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.util.Logger;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.util.LoggerProxy;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.ProcessTemplateContext;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.ProcessTemplateRecommendService;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.ProcessTemplateRenderService;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend.ItemSetRecommendModelHandler;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend.RecommendModel;
import com.tmall.wireless.tac.biz.processor.processtemplate.timelimitedseckill.domain.SecKillActivity;
import com.tmall.wireless.tac.biz.processor.processtemplate.timelimitedseckill.domain.SecKillActivityConfig;
import com.tmall.wireless.tac.biz.processor.processtemplate.timelimitedseckill.domain.SelectedSecKillSession;
import com.tmall.wireless.tac.biz.processor.processtemplate.timelimitedseckill.dto.SecKillActivityDTO;
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

    private static Logger logger = LoggerProxy.getLogger(TimeLimitedSecKillHandler.class);

    @Autowired
    private ProcessTemplateRenderService renderService;

    @Autowired
    private ProcessTemplateRecommendService recommendService;

    @Autowired
    private TacLogger tacLogger;

    @Override
    public Flowable<TacResult<List<GeneralItem>>> executeFlowable(RequestContext4Ald requestContext4Ald) throws Exception {
        //初始化上下文
        ProcessTemplateContext context = ProcessTemplateContext.init(requestContext4Ald);

        //构造运营配置商品列表领域对象
        SecKillActivityConfig activityConfig = SecKillActivityConfig.valueOf(context.getAldManualConfigDataList());
        SecKillActivity secKillActivity = SecKillActivity.init(activityConfig);

        logger.info("secKillActivity: " + JSON.toJSONString(secKillActivity));

        SelectedSecKillSession selectedSecKillSession = secKillActivity.select(null);

        logger.info("selectedSecKillSession: " + JSON.toJSONString(selectedSecKillSession));

        //推荐召回
        Map<String, String> recommendParams = new HashMap<>();
        recommendParams.put("contentType", "3");
        recommendParams.put("itemSetIdList", selectedSecKillSession.itemSetId());
        RecommendModel recommendModel = recommendService.recommendContent(21557L, context, recommendParams, new ItemSetRecommendModelHandler());
        logger.info("recommendModel: " + JSON.toJSONString(recommendModel));
        tacLogger.warn("allItemIds" + JSON.toJSONString(recommendModel.getAllItemIds()));

        //Captain渲染
        Map<Long, ItemDTO> longItemDTOMap = renderService.batchQueryItem(recommendModel.getAllItemIds(), context);
        logger.info("longItemDTOMap.size: " + longItemDTOMap.size());
        tacLogger.warn("longItemDTOMap.size: " + longItemDTOMap.size());

        //结果组装
        SecKillActivityDTO secKillActivityDTO = SecKillActivityDTO.valueOf(secKillActivity, selectedSecKillSession, recommendModel.getAllItemIds(), longItemDTOMap);
        List<GeneralItem> generalItemList = secKillActivityDTO.toGeneralItemList();
        logger.info("generalItemList: " + JSON.toJSONString(generalItemList));
        tacLogger.warn("generalItemList" + JSON.toJSONString(generalItemList));
        return Flowable.just(TacResult.newResult(generalItemList));
    }

}
