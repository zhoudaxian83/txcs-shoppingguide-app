package com.tmall.wireless.tac.biz.processor.gsh.itemsetrecommend;

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
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.biz.processor.huichang.service.HallCommonItemRequestProxy;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import com.tmall.wireless.tac.client.handler.TacReactiveHandler4Ald;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GshItemSetRecommendHandler extends TacReactiveHandler4Ald {

    Logger logger = LoggerFactory.getLogger(GshItemSetRecommendHandler.class);

    @Autowired
    TacLogger tacLogger;

    @Autowired
    ShoppingguideSdkItemService shoppingguideSdkItemService;

    @Autowired
    HallCommonItemRequestProxy hallCommonItemRequestProxy;

    @Override
    public Flowable<TacResult<List<GeneralItem>>> executeFlowable(RequestContext4Ald requestContext4Ald)
        throws Exception {
        logger.error("-------requestContext4Ald:{}", JSON.toJSONString(requestContext4Ald));
        BizScenario bizScenario = BizScenario.valueOf(HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
            HallScenarioConstant.HALL_SCENARIO_USE_CASE_GSH,
            HallScenarioConstant.HALL_SCENARIO_SCENARIO_GSH_ITEM_SET_RECOMMEND);
        bizScenario.addProducePackage(HallScenarioConstant.HALL_ITEM_SDK_PACKAGE);

        //Flowable<TacResult<List<GeneralItem>>> recommend = hallCommonItemRequestProxy.recommend(requestContext4Ald,
        //    bizScenario);
        //return recommend;
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
                TacResult<List<GeneralItem>> tacResult = TacResult.newResult(generalItemList);
                tacResult.setHasMore(response.isHasMore());
                Map<String, Object> bizExtMap = new HashMap<>();
                bizExtMap.put("hasMore", response.isHasMore());
                bizExtMap.put("index", response.getIndex());
                tacResult.setBizExtMap(bizExtMap);
                logger.error("-------bizExtMap:{}", JSON.toJSONString(bizExtMap));
                return tacResult;
            });
    }
//    GeneralItem generalItem = new GeneralItem();
    //        generalItem.put("success", response.isSuccess());
    //        generalItem.put("errorCode", response.getErrorCode());
    //        generalItem.put("errorMsg", response.getErrorMsg());
    //        generalItem.put("itemAndContentList", response.getItemAndContentList());
    //        generalItem.put("extInfos", response.getExtInfos());
    //        generalItem.put("hasMore", response.isHasMore());
    //        generalItem.put("index", response.getIndex());
}
