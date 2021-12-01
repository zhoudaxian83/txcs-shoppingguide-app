package com.tmall.wireless.tac.biz.processor.guessWhatYouLike.promotion;

import com.alibaba.fastjson.JSON;

import com.tmall.aselfcaptain.item.model.QueryOptionDO;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.framework.extensions.item.iteminfo.CaptainRequestBuildRequest;
import com.tmall.tcls.gs.sdk.framework.extensions.item.iteminfo.CaptainRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.sm.iteminfo.bysource.captain.DefaultCaptainRequestBuildSdkExtPt;
import com.tmall.wireless.store.spi.render.model.RenderRequest;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.gsh.itemsetrecommend.GshItemsetRecommendCaptainRequestBuildSdkExtPt;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhongwei
 * @date 2021/12/1
 */
@SdkExtension(
    bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.SCENARIO_SUB_PROMOTION_PAGE
)
public class GulItemSetCaptainRequestBuildSdkExtPt extends DefaultCaptainRequestBuildSdkExtPt implements CaptainRequestBuildSdkExtPt {
    Logger logger = LoggerFactory.getLogger(GshItemsetRecommendCaptainRequestBuildSdkExtPt.class);
    private final String captainSceneCode = "conference.gsh.common";

    @Override
    public RenderRequest process(CaptainRequestBuildRequest captainRequestBuildRequest) {
        //RenderRequest renderRequest = super.process(captainRequestBuildRequest);
        //QueryOptionDO option = renderRequest.getOption();
        //option.setSceneCode(captainSceneCode);
        //renderRequest.setOption(option);
        //return renderRequest;
        return super.process(captainRequestBuildRequest);
    }

}
