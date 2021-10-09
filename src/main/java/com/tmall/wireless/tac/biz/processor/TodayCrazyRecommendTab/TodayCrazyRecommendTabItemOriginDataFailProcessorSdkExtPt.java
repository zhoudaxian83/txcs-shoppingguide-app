package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab;

import com.alibaba.cola.extension.BizScenario;
import com.alibaba.fastjson.JSON;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataFailProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.OriginDataProcessRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;
import com.tmall.txcs.gs.framework.extensions.failprocessor.ItemFailProcessorRequest;
import com.tmall.txcs.gs.model.biz.context.LocParams;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.service.TairCacheUtil;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created from template by 罗俊冲 on 2021-09-30 16:51:23.
 */

@SdkExtension(
        bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.TODAY_CRAZY_RECOMMEND_TAB
)
public class TodayCrazyRecommendTabItemOriginDataFailProcessorSdkExtPt extends Register implements ItemOriginDataFailProcessorSdkExtPt {
    @Autowired
    TacLoggerImpl tacLogger;

    @Autowired
    TairCacheUtil tairCacheUtil;

    @Override
    public OriginDataDTO<ItemEntity> process(OriginDataProcessRequest originDataProcessRequest) {
        tacLogger.info("tpp失败打底执行了");
        ItemFailProcessorRequest itemFailProcessorRequest = JSON.parseObject(JSON.toJSONString(originDataProcessRequest), ItemFailProcessorRequest.class);
        BizScenario bizScenario = BizScenario.valueOf(ScenarioConstantApp.BIZ_TYPE_SUPERMARKET, ScenarioConstantApp.LOC_TYPE_B2C, ScenarioConstantApp.TODAY_CRAZY_RECOMMEND_TAB);
        com.tmall.tcls.gs.sdk.framework.model.context.LocParams locParams1 = originDataProcessRequest.getSgFrameworkContextItem().getCommonUserParams().getLocParams();
        LocParams locParams = new LocParams();
        locParams.setRt1HourStoreId(locParams1.getRt1HourStoreId());
        locParams.setRtHalfDayStoreId(locParams1.getRtHalfDayStoreId());
        locParams.setRtNextDayStoreId(locParams1.getRtNextDayStoreId());
        itemFailProcessorRequest.getSgFrameworkContextItem().setLocParams(locParams);
        itemFailProcessorRequest.getSgFrameworkContextItem().setBizScenario(bizScenario);
        List<ItemEntity> itemEntityList = JSON.parseArray(JSON.toJSONString(tairCacheUtil.process(itemFailProcessorRequest).getResult()), ItemEntity.class);
        OriginDataDTO<ItemEntity> originDataDTO = new OriginDataDTO<>();
        originDataDTO.setResult(itemEntityList);
        originDataDTO.setIndex(0);
        originDataDTO.setHasMore(false);
        originDataDTO.setPvid("");
        originDataDTO.setScm("1007.0.0.0");
        return originDataDTO;
    }
}
