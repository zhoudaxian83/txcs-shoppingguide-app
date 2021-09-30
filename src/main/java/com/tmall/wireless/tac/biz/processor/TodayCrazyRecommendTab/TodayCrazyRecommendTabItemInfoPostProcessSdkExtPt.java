package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab;

import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.iteminfo.ItemInfoPostProcessSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextItem;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.model.ItemLimitDTO;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.service.TodayCrazyLimitService;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.wzt.constant.Constant;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@SdkExtension(
        bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.TODAY_CRAZY_RECOMMEND_TAB
)
public class TodayCrazyRecommendTabItemInfoPostProcessSdkExtPt extends Register implements ItemInfoPostProcessSdkExtPt {
    @Autowired
    TodayCrazyLimitService todayCrazyLimitService;

    @Autowired
    TacLoggerImpl tacLogger;
    private static final Logger LOGGER = LoggerFactory.getLogger(TodayCrazyRecommendTabItemInfoPostProcessSdkExtPt.class);

    @Override
    public SgFrameworkContextItem process(SgFrameworkContextItem sgFrameworkContextItem) {
        Map<Long, List<ItemLimitDTO>> itemLimitResult = todayCrazyLimitService.getItemLimitResult(sgFrameworkContextItem);
        if (itemLimitResult != null) {
            sgFrameworkContextItem.getUserParams().put(Constant.ITEM_LIMIT_RESULT, itemLimitResult);
        } else {
            tacLogger.warn("TodayCrazyRecommendTabItemOriginDataPostProcessorSdkExtPt_" + "获取限购数据为空");
            LOGGER.warn("TodayCrazyRecommendTabItemOriginDataPostProcessorSdkExtPt_" + "获取限购数据为空");
        }
        return sgFrameworkContextItem;
    }
}
