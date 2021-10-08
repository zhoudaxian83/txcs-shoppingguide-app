package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab;

import com.alibaba.cola.extension.BizScenario;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.taobao.tair.ResultCode;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataFailProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.OriginDataProcessRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;
import com.tmall.txcs.gs.framework.extensions.excutor.SgExtensionExecutor;
import com.tmall.txcs.gs.framework.extensions.failprocessor.ItemFailProcessorRequest;
import com.tmall.txcs.gs.framework.extensions.failprocessor.ItemOriginDataFailKeyBuilderExtPt;
import com.tmall.txcs.gs.framework.model.ErrorCode;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.framework.model.meta.ItemMetaInfo;
import com.tmall.txcs.gs.model.biz.context.LocParams;
import com.tmall.txcs.gs.spi.recommend.TairFactorySpi;
import com.tmall.txcs.gs.spi.recommend.TairManager;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.service.TairCacheUtil;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

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
    public static final int SAMPLING_INTERVAL = 10;

    private static final AtomicLong counter = new AtomicLong(0L);

    @Autowired
    TairFactorySpi tairFactorySpi;

    @Autowired
    private SgExtensionExecutor sgExtensionExecutor;

    @Autowired
    TairCacheUtil tairCacheUtil;

    String logKey = "originDataFailProcessor";
    String originDataSuccessKey = "originDataSuccess";

    @Override
    public OriginDataDTO<ItemEntity> process(OriginDataProcessRequest originDataProcessRequest) {
        tacLogger.info("bizScenario:" + JSON.toJSONString(originDataProcessRequest.getSgFrameworkContextItem().getBizScenario()));
        tacLogger.info("tpp失败打底逻辑");
        tacLogger.info("tpp入参：" + JSON.toJSONString(originDataProcessRequest));
        tacLogger.info("tpp转换结果：" + JSON.toJSONString(JSON.parseObject(JSON.toJSONString(originDataProcessRequest), ItemFailProcessorRequest.class)));
        ItemFailProcessorRequest itemFailProcessorRequest = JSON.parseObject(JSON.toJSONString(originDataProcessRequest), ItemFailProcessorRequest.class);
        BizScenario bizScenario = BizScenario.valueOf(ScenarioConstantApp.BIZ_TYPE_SUPERMARKET, ScenarioConstantApp.LOC_TYPE_B2C, ScenarioConstantApp.TODAY_CRAZY_RECOMMEND_TAB);
        com.tmall.tcls.gs.sdk.framework.model.context.LocParams locParams1 = originDataProcessRequest.getSgFrameworkContextItem().getCommonUserParams().getLocParams();
        LocParams locParams = new LocParams();
        locParams.setRt1HourStoreId(locParams1.getRt1HourStoreId());
        locParams.setRtHalfDayStoreId(locParams1.getRtHalfDayStoreId());
        locParams.setRtNextDayStoreId(locParams1.getRtNextDayStoreId());
        itemFailProcessorRequest.getSgFrameworkContextItem().setLocParams(locParams);
        itemFailProcessorRequest.getSgFrameworkContextItem().setBizScenario(bizScenario);
        SgFrameworkContextItem sgFrameworkContextItem = itemFailProcessorRequest.getSgFrameworkContextItem();
        int interval = Optional.of(itemFailProcessorRequest)
                .map(ItemFailProcessorRequest::getSgFrameworkContextItem)
                .map(SgFrameworkContextItem::getItemMetaInfo)
                .map(ItemMetaInfo::getSamplingInterval)
                .orElse(SAMPLING_INTERVAL);
        if (interval <= 0) {
            interval = SAMPLING_INTERVAL;
        }
        tacLogger.info("tpp失败打底逻辑-1");
        String tairKey = buildTairKey(itemFailProcessorRequest);
        long currentCount = counter.addAndGet(1);
        boolean success = itemFailProcessorRequest.getItemEntityOriginDataDTO() != null
                && CollectionUtils.isNotEmpty(itemFailProcessorRequest.getItemEntityOriginDataDTO().getResult());
        TairManager merchantsTair = tairFactorySpi.getOriginDataFailProcessTair();
        tacLogger.info("tpp失败打底逻辑-1-2");
        if (merchantsTair == null || merchantsTair.getMultiClusterTairManager() == null || merchantsTair.getNameSpace() <= 0) {
            HadesLogUtil.stream(sgFrameworkContextItem.getBizScenario().getUniqueIdentity())
                    .kv("step", logKey)
                    .kv("errorCode", ErrorCode.ITEM_FAIL_PROCESSOR_TAIR_MANAGER_NULL)
                    .error();
            return originDataProcessRequest.getItemEntityOriginDataDTO();
        }
        tacLogger.info("tpp失败打底逻辑-2");
        if (success) {
            HadesLogUtil.stream(sgFrameworkContextItem.getBizScenario().getUniqueIdentity())
                    .kv("step", logKey)
                    .kv("info", originDataSuccessKey)
                    .info();

            if (currentCount % interval == 1) {
                ResultCode put = merchantsTair.getMultiClusterTairManager().put(merchantsTair.getNameSpace(),
                        tairKey,
                        JSON.toJSONString(itemFailProcessorRequest.getItemEntityOriginDataDTO().getResult()),
                        0);

                if (put == null || put.getCode() != ResultCode.SUCCESS.getCode()) {
                    HadesLogUtil.stream(sgFrameworkContextItem.getBizScenario().getUniqueIdentity())
                            .kv("step", logKey)
                            .kv("errorCode", ErrorCode.ITEM_FAIL_PROCESSOR_TAIR_PUT_ERROR)
                            .error();
                }

            }
            tacLogger.info("tpp失败打底逻辑-3");
            return originDataProcessRequest.getItemEntityOriginDataDTO();

        } else {
            tacLogger.info("tpp失败打底逻辑-4");
//            HadesLogUtil.stream(sgFrameworkContextItem.getBizScenario().getUniqueIdentity())
//                    .kv("step", logKey)
//                    .kv("errorCode", ErrorCode.ITEM_FAIL_PROCESSOR_ORIGIN_DATA_FAIL)
//                    .error();
//
//            List<ItemEntity> itemEntityList = readFromTair(tairKey, merchantsTair);
//            tacLogger.info("tpp失败打底逻辑-itemEntityList：" + JSON.toJSONString(itemEntityList));
//            if (CollectionUtils.isEmpty(itemEntityList)) {
//                HadesLogUtil.stream(sgFrameworkContextItem.getBizScenario().getUniqueIdentity())
//                        .kv("step", logKey)
//                        .kv("errorCode", ErrorCode.ITEM_FAIL_PROCESSOR_READ_FROM_TARI_FAIL)
//                        .error();
//
//                return originDataProcessRequest.getItemEntityOriginDataDTO();
//            }
//            tacLogger.info("tpp失败打底逻辑-5");
//            HadesLogUtil.stream(sgFrameworkContextItem.getBizScenario().getUniqueIdentity())
//                    .kv("step", logKey)
//                    .kv("errorCode", ErrorCode.ITEM_FAIL_PROCESSOR_READ_FROM_TARI_SUCCESS)
//                    .error();
            tacLogger.info("tpp失败打底逻辑-5");
            List<ItemEntity> itemEntityList = JSON.parseArray(JSON.toJSONString(tairCacheUtil.process(itemFailProcessorRequest).getResult()), ItemEntity.class);
            OriginDataDTO<ItemEntity> originDataDTO = new OriginDataDTO<>();
            originDataDTO.setResult(itemEntityList);
            originDataDTO.setIndex(0);
            originDataDTO.setHasMore(false);
            originDataDTO.setPvid("");
            originDataDTO.setScm("1007.0.0.0");
            tacLogger.info("tpp失败打底数据：" + JSON.toJSONString(originDataDTO));
            return originDataDTO;
        }

    }


    private String buildTairKey(ItemFailProcessorRequest itemFailProcessorRequest) {
        tacLogger.info("buildTairKey_" + JSON.toJSONString(itemFailProcessorRequest.getSgFrameworkContextItem().getBizScenario()));
        return sgExtensionExecutor.execute(ItemOriginDataFailKeyBuilderExtPt.class,
                itemFailProcessorRequest.getSgFrameworkContextItem().getBizScenario(),
                pt -> pt.process0(itemFailProcessorRequest));

    }

    private List<ItemEntity> readFromTair(String tairKey, TairManager tairManager) {

        List<ItemEntity> list = Lists.newArrayList();
        tacLogger.info("_tairKey：" + tairKey + ";nameSpace:" + tairManager.getNameSpace());
        Result<DataEntry> dataEntryResult = tairManager.getMultiClusterTairManager().get(tairManager.getNameSpace(), tairKey);
        tacLogger.info("dataEntryResult:" + JSON.toJSONString(dataEntryResult));
        String value = Optional.ofNullable(dataEntryResult).map(Result::getValue).map(DataEntry::getValue).map(Object::toString).orElse("");

        if (StringUtils.isEmpty(value)) {
            return list;
        }

        return JSON.parseArray(value, ItemEntity.class);
    }


//    protected boolean checkSuccess(OriginDataDTO<ItemEntity> originDataDTO) {
//        return originDataDTO != null && CollectionUtils.isNotEmpty(originDataDTO.getResult());
//    }


}
