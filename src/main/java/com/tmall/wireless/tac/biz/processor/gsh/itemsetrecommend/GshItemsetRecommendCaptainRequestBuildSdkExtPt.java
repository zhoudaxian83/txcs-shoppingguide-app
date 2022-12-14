package com.tmall.wireless.tac.biz.processor.gsh.itemsetrecommend;

import com.alibaba.fastjson.JSON;

import com.tmall.aselfcaptain.item.model.QueryOptionDO;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.framework.extensions.item.iteminfo.CaptainRequestBuildRequest;
import com.tmall.tcls.gs.sdk.framework.extensions.item.iteminfo.CaptainRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.sm.iteminfo.bysource.captain.DefaultCaptainRequestBuildSdkExtPt;
import com.tmall.wireless.store.spi.render.model.RenderRequest;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 修改captain的sceneCode，获得卖点数据
 */
@SdkExtension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
    useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_GSH,
    scenario = HallScenarioConstant.HALL_SCENARIO_SCENARIO_GSH_ITEM_SET_RECOMMEND)
public class GshItemsetRecommendCaptainRequestBuildSdkExtPt extends DefaultCaptainRequestBuildSdkExtPt implements CaptainRequestBuildSdkExtPt {
    Logger logger = LoggerFactory.getLogger(GshItemsetRecommendCaptainRequestBuildSdkExtPt.class);
    private final String captainSceneCode = "conference.gsh.common";

    @Override
    public RenderRequest process(CaptainRequestBuildRequest captainRequestBuildRequest) {
        logger.error("------GshItemsetRecommendCaptainRequestBuildSdkExtPt----.start");
        RenderRequest renderRequest = super.process(captainRequestBuildRequest);
        QueryOptionDO option = renderRequest.getOption();
        option.setSceneCode(captainSceneCode);
        renderRequest.setOption(option);
        logger.error("------GshItemsetRecommendCaptainRequestBuildSdkExtPt---request:{}", JSON.toJSONString(renderRequest));
        return renderRequest;
    }
}
