package com.tmall.wireless.tac.biz.processor.huichang.inventory.inventoryChannelPage;

import java.util.List;
import java.util.Map;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;

import com.alibaba.fastjson.JSONObject;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallCommonAldConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.utils.PageUrlUtil;
import com.tmall.wireless.tac.biz.processor.huichang.service.HallCommonContentRequestProxy;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import com.tmall.wireless.tac.client.handler.TacReactiveHandler4Ald;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *  清单会场
 */
@Component
public class InventoryChannelPageHandler extends TacReactiveHandler4Ald {

    @Autowired
    HallCommonContentRequestProxy hallCommonContentRequestProxy;
    @Autowired
    TacLogger tacLogger;
    @Override
    public Flowable<TacResult<List<GeneralItem>>> executeFlowable(RequestContext4Ald requestContext4Ald) throws Exception {
        BizScenario bizScenario = BizScenario.valueOf(HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
                HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
                HallScenarioConstant.HALL_SCENARIO_SCENARIO_INVENTORY_CHANNEL_PAGE);
//        bizScenario.addProducePackage("huichang");
        Flowable<TacResult<List<GeneralItem>>> ContentVOs = hallCommonContentRequestProxy.recommend(requestContext4Ald, bizScenario);
        // 获取场景组的信息（从url带来的主标题副标题等）
        GeneralItem sceneSetGeneralItem = buildSceneSet(requestContext4Ald);
        // 获取场景信息
        List<GeneralItem> generalContentList = ContentVOs.blockingFirst().getData();
        if(CollectionUtils.isNotEmpty(generalContentList)){
            // generalContentList 只有一个元素 itemAndContentList在这个元素里面
            generalContentList.get(0).put("extInfos", sceneSetGeneralItem);
        }
        return Flowable.just(TacResult.newResult(generalContentList));
    }

    private GeneralItem buildSceneSet(RequestContext4Ald requestContext4Ald) throws Exception {
        Map<String, Object> aldParams = requestContext4Ald.getAldParam();
        Map<String, Object> aldContext = requestContext4Ald.getAldContext();
        if(MapUtils.isEmpty(aldParams)) {
            tacLogger.debug("aldParams为空");
            HadesLogUtil.stream("InventoryChannelPage")
                    .kv("InventoryChannelPageHandler", "buildSceneSet")
                    .kv("数据缺失", "aldParams")
                    .error();
            throw new Exception("aldParams为空");
        }
        HadesLogUtil.stream("inventoryChannelPage")
                .kv("InventoryChannelPageHandler", "buildSceneSet")
                .kv("aldParams", JSONObject.toJSONString(aldParams))
                .info();
        // 从url获取主标题
        String contentSetTitle = PageUrlUtil.getParamFromCurPageUrl(aldParams, "contentSetTitle", tacLogger);
        if(StringUtils.isBlank(contentSetTitle)) {
            contentSetTitle = "购物车好物"; // Todo likunlin
        }
        // 从url获取副标题
        String contentSetSubtitle = PageUrlUtil.getParamFromCurPageUrl(aldParams, "contentSetSubTitle", tacLogger);
        if(StringUtils.isBlank(contentSetSubtitle)) {
            contentSetSubtitle = "超市购物攻略推荐"; // Todo likunlin
        }
        GeneralItem generalItem = new GeneralItem();
        generalItem.put("contentSetTitle", contentSetTitle);
        generalItem.put("contentSetSubtitle", contentSetSubtitle);
        generalItem.put("resourceId", String.valueOf(aldContext.get(HallCommonAldConstant.ALD_CURRENT_RES_ID)));
        return generalItem;
    }
}

