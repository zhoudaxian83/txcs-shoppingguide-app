package com.tmall.wireless.tac.biz.processor.processtemplate.timelimitedseckill;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.alibaba.fastjson.JSON;
import com.google.common.primitives.Longs;
import com.tmall.aselfcaptain.item.model.ItemDTO;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.util.Logger;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.util.LoggerProxy;
import com.tmall.wireless.tac.biz.processor.huichang.common.utils.PageUrlUtil;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.ProcessTemplateContext;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.ProcessTemplateRecommendService;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.ProcessTemplateRenderService;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend.ItemSetRecommendModelHandler;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend.RecommendModel;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.util.MetricsUtil;
import com.tmall.wireless.tac.biz.processor.processtemplate.timelimitedseckill.domain.SecKillActivity;
import com.tmall.wireless.tac.biz.processor.processtemplate.timelimitedseckill.domain.SecKillActivityConfig;
import com.tmall.wireless.tac.biz.processor.processtemplate.timelimitedseckill.domain.SelectedSecKillSession;
import com.tmall.wireless.tac.biz.processor.processtemplate.timelimitedseckill.dto.SecKillActivityDTO;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import com.tmall.wireless.tac.client.handler.TacReactiveHandler4Ald;
import io.reactivex.Flowable;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TimeLimitedSecKillHandler extends TacReactiveHandler4Ald {

    private static Logger logger = LoggerProxy.getLogger(TimeLimitedSecKillHandler.class);
    private static final String captainSceneCode = "conference.zhj";
    private static final Long APPID = 21557L;

    @Autowired
    private ProcessTemplateRenderService renderService;

    @Autowired
    private ProcessTemplateRecommendService recommendService;

    @Autowired
    private TacLogger tacLogger;

    @Override
    public Flowable<TacResult<List<GeneralItem>>> executeFlowable(RequestContext4Ald requestContext4Ald) throws Exception {
        Long mainProcessStart = System.currentTimeMillis();

        //初始化上下文
        ProcessTemplateContext context = ProcessTemplateContext.init(requestContext4Ald, TimeLimitedSecKillHandler.class);
        context.setSceneCode(captainSceneCode);
        try {
            //构造运营配置商品列表领域对象
            SecKillActivityConfig activityConfig = SecKillActivityConfig.valueOf(context.getAldManualConfigDataList());
            SecKillActivity secKillActivity = SecKillActivity.init(activityConfig);
            logger.info("secKillActivity: " + JSON.toJSONString(secKillActivity));

            //解析参数中的chooseId
            String chooseIdStr = PageUrlUtil.getParamFromCurPageUrl(requestContext4Ald.getAldParam(), "chooseId");
            logger.info("chooseId:" + chooseIdStr);
            Long chooseId = Optional.ofNullable(chooseIdStr).map(Longs::tryParse).orElse(null);

            //获取选中的秒杀场次
            SelectedSecKillSession selectedSecKillSession = secKillActivity.select(chooseId);
            logger.info("selectedSecKillSession: " + JSON.toJSONString(selectedSecKillSession));

            //推荐召回
            Map<String, String> recommendParams = buildTppParams(selectedSecKillSession, context);
            RecommendModel recommendModel = recommendService.recommendContent(APPID, context, recommendParams, new ItemSetRecommendModelHandler());
            logger.info("recommendModel: " + JSON.toJSONString(recommendModel));
            if (recommendModel == null) {
                return Flowable.just(TacResult.newResult(new ArrayList<>()));
            }

            //Captain渲染
            Long timeOfFuturePrice = selectedSecKillSession.timeOfFuturePrice();
            if(timeOfFuturePrice != null) {
                context.setPreviewTime(String.valueOf(timeOfFuturePrice));
            }
            Map<Long, ItemDTO> longItemDTOMap = renderService.batchQueryItem(recommendModel.getAllItemIds(), context);

            //结果组装
            SecKillActivityDTO secKillActivityDTO = SecKillActivityDTO.valueOf(secKillActivity, selectedSecKillSession, recommendModel.getAllItemIds(), longItemDTOMap);
            List<GeneralItem> generalItemList = secKillActivityDTO.toGeneralItemList();

            MetricsUtil.mainProcessSuccess(context, mainProcessStart);
            return Flowable.just(TacResult.newResult(generalItemList));
        } catch (Exception e) {
            MetricsUtil.mainProcessException(context, e);
        }
        return Flowable.just(TacResult.newResult(new ArrayList<>()));
    }

    @NotNull
    private Map<String, String> buildTppParams(SelectedSecKillSession selectedSecKillSession, ProcessTemplateContext context) {
        Map<String, String> params = new HashMap<>();
        params.put("smAreaId", context.getSmAreaId());
        params.put("logicAreaId", context.getLogicAreaId());
        params.put("userId", String.valueOf(context.getUserId()));
        params.put("itemSetIds", selectedSecKillSession.itemSetId());
        params.put("itemSetIdList", selectedSecKillSession.itemSetId());//进舟新版接口 字段名称变更
        params.put("brandRec", "true");
        params.put("pageSize", "1");
        params.put("itemCountPerContent", "20");//进舟新版接口 单个圈品集下面挂的商品数量
        params.put("contentType", "3"); //进舟新接口必填参数 需要写死3
        return params;
    }

}
