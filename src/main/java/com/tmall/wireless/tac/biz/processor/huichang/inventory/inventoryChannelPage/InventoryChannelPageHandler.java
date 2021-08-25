package com.tmall.wireless.tac.biz.processor.huichang.inventory.inventoryChannelPage;
import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.utils.PageUrlUtil;
import com.tmall.wireless.tac.biz.processor.huichang.service.HallCommonContentRequestProxy;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import com.tmall.wireless.tac.client.handler.TacReactiveHandler4Ald;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

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
        bizScenario.addProducePackage("huichang");
        Flowable<TacResult<List<GeneralItem>>> ContentVOs = hallCommonContentRequestProxy.recommend(requestContext4Ald, bizScenario);
        GeneralItem sceneSetGeneralItem = buildSceneSet(requestContext4Ald);
        List<GeneralItem> generalItemList = ContentVOs.blockingFirst().getData();
        if(CollectionUtils.isNotEmpty(generalItemList)){
            generalItemList.get(0).put("extInfos", sceneSetGeneralItem);
        }
        return Flowable.just(TacResult.newResult(generalItemList));
    }

    private GeneralItem buildSceneSet(RequestContext4Ald requestContext4Ald) {
        Map<String, Object> aldParams = requestContext4Ald.getAldParam();
        String contentSetTitle = PageUrlUtil.getParamFromCurPageUrl(aldParams, "contentSetTitle", tacLogger);
        if(StringUtils.isBlank(contentSetTitle)) {
            contentSetTitle = "contentSetSubtitle打底";
        }
        String contentSetSubtitle = PageUrlUtil.getParamFromCurPageUrl(aldParams, "contentSetSubTitle", tacLogger);
        if(StringUtils.isBlank(contentSetSubtitle)) {
            contentSetSubtitle = "contentSetSubtitle打底";
        }
        GeneralItem generalItem = new GeneralItem();
        generalItem.put("contentSetTitle", contentSetTitle);
        generalItem.put("contentSetSubtitle", contentSetSubtitle);
        return generalItem;
    }
}

