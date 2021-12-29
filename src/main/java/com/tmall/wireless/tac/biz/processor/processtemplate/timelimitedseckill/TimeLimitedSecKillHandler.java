package com.tmall.wireless.tac.biz.processor.processtemplate.timelimitedseckill;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.alibaba.fastjson.JSON;
import com.google.common.primitives.Longs;
import com.tcls.mkt.atmosphere.model.response.Price;
import com.tmall.aselfcaptain.item.model.ItemDTO;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.util.DateTimeUtil;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.util.Logger;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.util.LoggerProxy;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.ProcessTemplateContext;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.ProcessTemplateRecommendService;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.ProcessTemplateRenderService;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.ProcessTemplateTppBottomService;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend.ItemSetItemRecommendResponseHandler;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend.ItemSetRecommendModel;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend.RecommendModel;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.util.MetricsUtil;
import com.tmall.wireless.tac.biz.processor.processtemplate.timelimitedseckill.domain.SecKillActivity;
import com.tmall.wireless.tac.biz.processor.processtemplate.timelimitedseckill.domain.SecKillActivityConfig;
import com.tmall.wireless.tac.biz.processor.processtemplate.timelimitedseckill.domain.SelectedSecKillSession;
import com.tmall.wireless.tac.biz.processor.processtemplate.timelimitedseckill.dto.SecKillActivityDTO;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import com.tmall.wireless.tac.client.handler.TacReactiveHandler4Ald;
import io.reactivex.Flowable;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.tmall.wireless.tac.biz.processor.processtemplate.common.config.ProcessTemplateSwitch.*;

/**
 * 会场限时秒杀模块
 * 核心功能点，针对未开始的秒杀场次，支持传递未来时间查询未来的价格和利益点
 * Aone需求地址：https://aone.alibaba-inc.com/req/37876639
 *
 * @author 言武
 */
@Component
public class TimeLimitedSecKillHandler extends TacReactiveHandler4Ald {

    private static Logger logger = LoggerProxy.getLogger(TimeLimitedSecKillHandler.class);
    private static final String captainSceneCode = "conference.zhj";
    private static final Long APPID = 21690L;
    private static final String NO_VALID_SEC_KILL_SESSION = "no_valid_sec_kill_session";

    @Autowired
    private ProcessTemplateRenderService renderService;

    @Autowired
    private ProcessTemplateRecommendService recommendService;

    @Autowired
    private ProcessTemplateTppBottomService tppBottomService;

    @Override
    public Flowable<TacResult<List<GeneralItem>>> executeFlowable(RequestContext4Ald requestContext4Ald) throws Exception {
        if(mockTacException) {
            throw new RuntimeException("mock tac exception");
        }
        if(mockTacTimeout) {
            Thread.sleep(10000);
        }

        Long mainProcessStart = System.currentTimeMillis();

        //初始化上下文
        ProcessTemplateContext context = ProcessTemplateContext.init(requestContext4Ald, TimeLimitedSecKillHandler.class);
        //设置使用折后价的祥云码
        context.setSceneCode(captainSceneCode);
        try {
            //根据运营在鸿雁填写的配置数据构造"秒杀活动配置"领域对象
            SecKillActivityConfig activityConfig = SecKillActivityConfig.valueOf(context.getAldManualConfigDataList());
            //根据配置初始化"秒杀活动"领域对象
            SecKillActivity secKillActivity = SecKillActivity.init(activityConfig);

            //解析参数中的categoryId，用于决策当前选中的秒杀场次
            String chooseIdStr = (String)requestContext4Ald.getAldParam().getOrDefault("categoryId", null);
            Long chooseId = Optional.ofNullable(chooseIdStr).map(Longs::tryParse).orElse(null);
            //获取选中的秒杀场次
            SelectedSecKillSession selectedSecKillSession = secKillActivity.select(chooseId);

            //如果选中的秒杀场次为空，即没有有效的秒杀场次，则返回给前端一个特殊的占位秒杀场次，前端判断如果是该特殊的秒杀场次，则不展示秒杀模块
            //目的是避免返回空数据导致ald识别为错误，从而走到打底逻辑，而秒杀模块是不允许使用打底的。
            //ald本身是支持流程模板中指定不走打底的，但这需要在ald转调tac的总入口McTacHsfSolution中改造
            if(selectedSecKillSession == null) {
                SecKillActivityDTO secKillActivityDTO = SecKillActivityDTO.PLACEHOLDER_SEC_KILL_ACTIVITY;
                List<GeneralItem> generalItemList = secKillActivityDTO.toGeneralItemList();
                MetricsUtil.customProcessFail(NO_VALID_SEC_KILL_SESSION, context, "没有有效的秒杀场次");
                return Flowable.just(TacResult.newResult(generalItemList));
            }

            //推荐召回
            Map<String, String> recommendParams = buildTppParams(selectedSecKillSession, context);
            RecommendModel recommendModel = null;
            if(!mockTppCrash) {
                recommendModel = recommendService.recommendItem(APPID, context, recommendParams, new ItemSetItemRecommendResponseHandler());
                logger.info("recommendModel: " + JSON.toJSONString(recommendModel));
            }
            //如果tpp返回为空，走打底
            if (recommendModel == null) {
                recommendModel = tppBottomService.readBottomData(context, selectedSecKillSession.itemSetId(), ItemSetRecommendModel.class);
            } else {
                tppBottomService.writeBottomData(context, selectedSecKillSession.itemSetId(), recommendModel);
            }

            //从推荐模型中获取商品ID列表
            List<Long> itemIds = new ArrayList<>();
            if(recommendModel != null) {
                itemIds = recommendModel.fetchAllItemIds();
            }

            //如果开启了查询未来价格才给祥云传递未来的时间，查询未来价格会直连UMP，对性能有影响，需要加个开关，用于降级
            if(openFuturePrice) {
                Long timeOfFuturePrice = selectedSecKillSession.timeOfFuturePrice();
                if (timeOfFuturePrice != null) {
                    context.setPreviewTime(DateTimeUtil.formatTimestamp(timeOfFuturePrice));
                }
            }
            //Captain渲染
            Map<Long, ItemDTO> longItemDTOMap = null;
            if(!mockCaptainCrash) {
                longItemDTOMap = renderService.batchQueryItem(itemIds, context);
            }

            //过滤出有折扣价的品，业务@拾祎特定要求
            if(longItemDTOMap != null) {
                longItemDTOMap = longItemDTOMap.entrySet().stream().filter(e -> hasDiscountPrice(e.getValue()))
                        .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
            }

            //结果组装
            SecKillActivityDTO secKillActivityDTO = SecKillActivityDTO.valueOf(context.getCurrentResourceId(), secKillActivity, selectedSecKillSession, itemIds, longItemDTOMap);
            List<GeneralItem> generalItemList = secKillActivityDTO.toGeneralItemList();

            MetricsUtil.mainProcessSuccess(context, mainProcessStart);
            return Flowable.just(TacResult.newResult(generalItemList));
        } catch (Exception e) {
            MetricsUtil.mainProcessException(context, e);
            SecKillActivityDTO secKillActivityDTO = SecKillActivityDTO.PLACEHOLDER_SEC_KILL_ACTIVITY;
            List<GeneralItem> generalItemList = secKillActivityDTO.toGeneralItemList();
            return Flowable.just(TacResult.newResult(generalItemList));
        }
    }

    /**
     * 如果chaoshiPrice和showPrice都存在，并且showPrice < chaoshiPrice返回true
     *
     * @param itemDTO
     * @return
     */
    private boolean hasDiscountPrice(ItemDTO itemDTO) {
        if(itemDTO == null
                || itemDTO.getItemPromotionResp() == null
                || itemDTO.getItemPromotionResp().getUnifyPrice() != null
                || itemDTO.getItemPromotionResp().getUnifyPrice().getShowPrice() == null
                || itemDTO.getItemPromotionResp().getUnifyPrice().getChaoShiPrice() == null)
        return false;
        Price showPrice = itemDTO.getItemPromotionResp().getUnifyPrice().getShowPrice();
        Price chaoShiPrice = itemDTO.getItemPromotionResp().getUnifyPrice().getChaoShiPrice();
        if(showPrice.getCent() == null || chaoShiPrice.getCent() == null) {
            return false;
        }
        return showPrice.getCent() < chaoShiPrice.getCent();
    }

    @NotNull
    private Map<String, String> buildTppParams(SelectedSecKillSession selectedSecKillSession, ProcessTemplateContext context) {
        Map<String, String> params = new HashMap<>();
        params.put("smAreaId", context.getSmAreaId());
        params.put("logicAreaId", context.getLogicAreaId());
        params.put("userId", String.valueOf(context.getUserId()));
        params.put("userid", String.valueOf(context.getUserId()));
        params.put("itemSetIdList", selectedSecKillSession.itemSetId());
        params.put("brandRec", "true");
        params.put("pageSize", "20");
        params.put("index", "0");
        params.put("appid", "21690");
        return params;
    }

}
