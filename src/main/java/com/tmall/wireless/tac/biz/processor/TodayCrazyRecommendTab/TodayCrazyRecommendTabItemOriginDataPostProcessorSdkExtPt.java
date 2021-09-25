package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab;

import com.alibaba.fastjson.JSON;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataPostProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.OriginDataProcessRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.service.TodayCrazyLimitService;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.wzt.constant.Constant;
import com.tmall.wireless.tac.biz.processor.wzt.model.ItemLimitDTO;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@SdkExtension(
        bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.TODAY_CRAZY_RECOMMEND_TAB
)
public class TodayCrazyRecommendTabItemOriginDataPostProcessorSdkExtPt extends Register implements ItemOriginDataPostProcessorSdkExtPt {
    @Autowired
    TodayCrazyLimitService todayCrazyLimitService;

    @Autowired
    TacLoggerImpl tacLogger;

    @Override
    public OriginDataDTO<ItemEntity> process(OriginDataProcessRequest originDataProcessRequest) {
        tacLogger.info("开始查询limit信息");
        Map<Long, List<ItemLimitDTO>> itemLimitResult = todayCrazyLimitService.getItemLimitResult(originDataProcessRequest.getSgFrameworkContextItem());
        if (itemLimitResult != null) {
            originDataProcessRequest.getSgFrameworkContextItem().getUserParams().put(Constant.ITEM_LIMIT_RESULT, itemLimitResult);
        } else {
            tacLogger.warn("TodayCrazyRecommendTabItemOriginDataPostProcessorSdkExtPt_" + "获取限购数据为空");
        }
        tacLogger.info("limit查询结果_" + JSON.toJSONString(itemLimitResult));
        return originDataProcessRequest.getItemEntityOriginDataDTO();
    }
}
