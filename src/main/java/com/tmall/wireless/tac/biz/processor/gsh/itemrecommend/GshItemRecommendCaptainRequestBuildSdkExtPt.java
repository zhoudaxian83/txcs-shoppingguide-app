package com.tmall.wireless.tac.biz.processor.gsh.itemrecommend;

import com.alibaba.fastjson.JSON;

import com.tmall.aselfcaptain.item.model.QueryOptionDO;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.framework.extensions.item.iteminfo.CaptainRequestBuildRequest;
import com.tmall.tcls.gs.sdk.framework.extensions.item.iteminfo.CaptainRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.sm.iteminfo.bysource.captain.DefaultCaptainRequestBuildSdkExtPt;
import com.tmall.wireless.store.spi.render.model.RenderRequest;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 修改captain的sceneCode，获得卖点数据
 */
@SdkExtension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
    useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_GSH,
    scenario = HallScenarioConstant.HALL_SCENARIO_SCENARIO_GSH_ITEM_RECOMMEND)
public class GshItemRecommendCaptainRequestBuildSdkExtPt extends DefaultCaptainRequestBuildSdkExtPt implements CaptainRequestBuildSdkExtPt {

    Logger logger = LoggerFactory.getLogger(GshItemRecommendCaptainRequestBuildSdkExtPt.class);

    @Autowired
    TacLogger tacLogger;
    private final String captainSceneCode = "conference.gsh.common";

    @Override
    public RenderRequest process(CaptainRequestBuildRequest captainRequestBuildRequest) {
        tacLogger.debug("-----GshItemRecommendCaptainRequestBuildSdkExtPt----start");
        logger.error("-----GshItemRecommendCaptainRequestBuildSdkExtPt----start");
        RenderRequest renderRequest = super.process(captainRequestBuildRequest);
        QueryOptionDO option = renderRequest.getOption();
        option.setSceneCode(captainSceneCode);
        renderRequest.setOption(option);
        tacLogger.debug("-----GshItemRecommendCaptainRequestBuildSdkExtPt----request:" + JSON.toJSONString(renderRequest));
        logger.error("------GshItemRecommendCaptainRequestBuildSdkExtPt---request:{}", JSON.toJSONString(renderRequest));
        return renderRequest;
    }
}
