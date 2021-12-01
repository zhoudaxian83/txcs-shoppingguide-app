package com.tmall.wireless.tac.biz.processor.guessWhatYouLike.promotion;

import java.util.List;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;

import com.google.common.collect.Lists;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkItemService;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import com.tmall.wireless.tac.client.handler.TacReactiveHandler4Ald;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zhongwei
 * @date 2021/12/1
 */
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

        return shoppingguideSdkItemService.recommend(requestContext4Ald, bizScenario)
            .map(response -> {
                List<GeneralItem> generalItemList = Lists.newArrayList();
                List<ItemEntityVO> itemAndContentList = response.getItemAndContentList();
                itemAndContentList.forEach(contentVO -> {
                    GeneralItem generalItem = new GeneralItem();
                    contentVO.keySet().forEach(key -> {
                        generalItem.put(key, contentVO.get(key));
                    });
                    generalItemList.add(generalItem);
                });
                return generalItemList;
            })
            .map(TacResult::newResult)
            .onErrorReturn(r -> TacResult.errorResult(""));
    }
}
