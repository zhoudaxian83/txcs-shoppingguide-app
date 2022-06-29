package com.tmall.wireless.tac.biz.processor.guessWhatYouLike.promotion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.alibaba.fastjson.JSON;

import com.google.common.collect.Lists;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkItemService;
import com.tmall.txcs.gs.framework.model.EntityVO;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import com.tmall.wireless.tac.client.handler.TacReactiveHandler4Ald;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zhongwei
 * @date 2021/12/1
 */
@Slf4j
@Component
public class GulItemSetRecommendHandler extends TacReactiveHandler4Ald {

    @Autowired
    private ShoppingguideSdkItemService shoppingguideSdkItemService;


    @Override
    public Flowable<TacResult<List<GeneralItem>>> executeFlowable(RequestContext4Ald requestContext4Ald) throws Exception {
        BizScenario bizScenario = BizScenario.valueOf(ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
            ScenarioConstantApp.LOC_TYPE_B2C,
            ScenarioConstantApp.SCENARIO_SUB_PROMOTION_PAGE);
        bizScenario.addProducePackage(HallScenarioConstant.HALL_ITEM_SDK_PACKAGE);
        log.error("entryContext." + ScenarioConstantApp.SCENARIO_SUB_PROMOTION_PAGE + ",context:{}", JSON.toJSONString(requestContext4Ald));

        return shoppingguideSdkItemService.recommend(requestContext4Ald, bizScenario)
            .map(response -> {
                List<GeneralItem> generalItemList = Lists.newArrayList();
                generalItemList.add(convertAldItem(response));
                //itemAndContentList.forEach(contentVO -> {
                //    GeneralItem generalItem = new GeneralItem();
                //    contentVO.keySet().forEach(key -> {
                //        generalItem.put(key, contentVO.get(key));
                //    });
                //    generalItemList.add(generalItem);
                //});
                //TacResult<List<GeneralItem>> tacResult = TacResult.newResult(generalItemList);
                //tacResult.setHasMore(response.isHasMore());
                //Map<String, Object> bizExtMap = new HashMap<>();
                //bizExtMap.put("hasMore", response.isHasMore());
                //bizExtMap.put("index", response.getIndex());
                //tacResult.setBizExtMap(bizExtMap);
                //return tacResult;
                TacResult<List<GeneralItem>> tacResult = TacResult.newResult(generalItemList);
                return tacResult;
            }).onErrorReturn(r -> TacResult.errorResult(""));
    }


    private GeneralItem convertAldItem(SgFrameworkResponse<ItemEntityVO> response) {
        GeneralItem generalItem = new GeneralItem();
        generalItem.put("success", response.isSuccess());
        generalItem.put("errorCode", response.getErrorCode());
        generalItem.put("errorMsg", response.getErrorMsg());
        generalItem.put("items", response.getItemAndContentList());
        generalItem.put("extInfos", response.getExtInfos());
        generalItem.put("hasMore", response.isHasMore());
        generalItem.put("index", response.getIndex());

        return generalItem;
    }

}
