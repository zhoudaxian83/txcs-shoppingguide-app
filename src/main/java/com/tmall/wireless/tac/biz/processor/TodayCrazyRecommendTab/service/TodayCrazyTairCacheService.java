package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.service;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.taobao.tair.ResultCode;
import com.tmall.aselfmanager.client.columncenter.response.ColumnCenterDataSetItemRuleDTO;
import com.tmall.aselfmanager.client.columncenter.response.ColumnCenterPmtRuleDataSetDTO;
import com.tmall.aselfmanager.client.columncenter.response.PmtRuleDataItemRuleDTO;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.OriginDataProcessRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.EntityDTO;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.model.ErrorCode;
import com.tmall.txcs.gs.spi.recommend.TairFactorySpi;
import com.tmall.txcs.gs.spi.recommend.TairManager;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.constant.CommonConstant;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.constant.TabTypeEnum;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.model.DataSourceRequest;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Component
public class TodayCrazyTairCacheService {
    private static final String[] logicalArr = {"HD", "HB", "HN", "HZ", "XN"};

    public static final int SAMPLING_INTERVAL = 10;

    private static final AtomicLong counter = new AtomicLong(0L);

    private static final BizScenario bizScenario = BizScenario.valueOf(
            ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
            ScenarioConstantApp.LOC_TYPE_B2C,
            ScenarioConstantApp.TODAY_CRAZY_RECOMMEND_TAB
    );

    @Autowired
    TairFactorySpi tairFactorySpi;

    @Autowired
    TacLoggerImpl tacLogger;

    String logKey = "originDataFailProcessor";
    String originDataSuccessKey = "originDataSuccess";

    public OriginDataDTO<ItemEntity> process(OriginDataProcessRequest originDataProcessRequest) {
        int interval = SAMPLING_INTERVAL;
//        int interval = Optional.of(originDataProcessRequest)
//                .map(ItemFailProcessorRequest::getSgFrameworkContextItem)
//                .map(SgFrameworkContextItem::getItemMetaInfo)
//                .map(ItemMetaInfo::getSamplingInterval)
//                .orElse(SAMPLING_INTERVAL);
//        if (interval <= 0) {
//            interval = SAMPLING_INTERVAL;
//        }

        String tairKey = buildTairKey(originDataProcessRequest);
        long currentCount = counter.addAndGet(1);
        boolean success = checkSuccess(originDataProcessRequest.getItemEntityOriginDataDTO());

        TairManager merchantsTair = tairFactorySpi.getOriginDataFailProcessTair();
        if (merchantsTair == null || merchantsTair.getMultiClusterTairManager() == null || merchantsTair.getNameSpace() <= 0) {

            HadesLogUtil.stream(bizScenario.getUniqueIdentity())
                    .kv("step", logKey)
                    .kv("errorCode", ErrorCode.ITEM_FAIL_PROCESSOR_TAIR_MANAGER_NULL)
                    .error();

            return originDataProcessRequest.getItemEntityOriginDataDTO();
        }


        if (success) {
            HadesLogUtil.stream(bizScenario.getUniqueIdentity())
                    .kv("step", logKey)
                    .kv("info", originDataSuccessKey)
                    .info();

            if (currentCount % interval == 1) {
                tacLogger.info("tpp????????????,key???" + tairKey);
                ResultCode put = merchantsTair.getMultiClusterTairManager().put(merchantsTair.getNameSpace(),
                        tairKey,
                        JSON.toJSONString(originDataProcessRequest.getItemEntityOriginDataDTO().getResult()),
                        0);

                if (put == null || put.getCode() != ResultCode.SUCCESS.getCode()) {
                    tacLogger.info("tpp??????????????????");
                    HadesLogUtil.stream(bizScenario.getUniqueIdentity())
                            .kv("step", logKey)
                            .kv("errorCode", ErrorCode.ITEM_FAIL_PROCESSOR_TAIR_PUT_ERROR)
                            .kv("tairKey", tairKey)
                            .error();
                }

            }
            return originDataProcessRequest.getItemEntityOriginDataDTO();

        } else {

            HadesLogUtil.stream(bizScenario.getUniqueIdentity())
                    .kv("step", logKey)
                    .kv("errorCode", ErrorCode.ITEM_FAIL_PROCESSOR_ORIGIN_DATA_FAIL)
                    .error();
            tacLogger.info("tpp????????????,key???" + tairKey);
            List<ItemEntity> itemEntityList = readFromTair(tairKey, merchantsTair);
            if (CollectionUtils.isEmpty(itemEntityList)) {
                tacLogger.info("tpp??????????????????");
                HadesLogUtil.stream(bizScenario.getUniqueIdentity())
                        .kv("step", logKey)
                        .kv("errorCode", ErrorCode.ITEM_FAIL_PROCESSOR_READ_FROM_TARI_FAIL)
                        .error();
                return originDataProcessRequest.getItemEntityOriginDataDTO();
            }
            tacLogger.info("tpp??????????????????");
            HadesLogUtil.stream(bizScenario.getUniqueIdentity())
                    .kv("step", logKey)
                    .kv("errorCode", ErrorCode.ITEM_FAIL_PROCESSOR_READ_FROM_TARI_SUCCESS)
                    .error();

            OriginDataDTO<ItemEntity> originDataDTO = new OriginDataDTO<>();
            originDataDTO.setResult(itemEntityList);
            originDataDTO.setIndex(0);
            originDataDTO.setHasMore(false);
            originDataDTO.setPvid("");
            originDataDTO.setScm("1007.0.0.0");
            return originDataDTO;
        }

    }

    /**
     * ??????????????????
     *
     * @param originDataProcessRequest
     * @return
     */
    private String buildTairKey(OriginDataProcessRequest originDataProcessRequest) {
        String tabType = MapUtil.getStringWithDefault(originDataProcessRequest.getSgFrameworkContextItem().getRequestParams(), "tabType", TabTypeEnum.TODAY_CHAO_SHENG.getType());
//        if (RpmContants.enviroment.isOnline()) {
        if (true) {
            return "TPP_supermarket_b2c_TODAY_CRAZY_RECOMMEND_TAB_" + tabType;
        } else {
            return "TPP_supermarket_b2c_TODAY_CRAZY_RECOMMEND_TAB_" + tabType + "_pre";
        }
    }

    private List<ItemEntity> readFromTair(String tairKey, TairManager tairManager) {

        List<ItemEntity> list = Lists.newArrayList();
        Result<DataEntry> dataEntryResult = tairManager.getMultiClusterTairManager().get(tairManager.getNameSpace(), tairKey);

        String value = Optional.ofNullable(dataEntryResult).map(Result::getValue).map(DataEntry::getValue).map(Object::toString).orElse("");

        if (StringUtils.isEmpty(value)) {
            return list;
        }

        return JSON.parseArray(value, ItemEntity.class);
    }


    /**
     * @param originDataDTO
     * @param <T>
     * @return
     */
    protected <T extends EntityDTO> boolean checkSuccess(OriginDataDTO<T> originDataDTO) {
        return originDataDTO != null && CollectionUtils.isNotEmpty(originDataDTO.getResult());
    }

    public List<ColumnCenterDataSetItemRuleDTO> getTairColumnCenterDataSetItemRuleDTO(String tairKey) {
        HadesLogUtil.stream(ScenarioConstantApp.TODAY_CRAZY_RECOMMEND_TAB)
                .kv("tairKey", tairKey)
                .kv("method", "getTairManager")
                .info();
        List<ColumnCenterDataSetItemRuleDTO> centerDataSetItemRuleDTOS = Lists.newArrayList();
        TairManager tairManager = tairFactorySpi.getOriginDataFailProcessTair();
        if (tairManager == null || tairManager.getMultiClusterTairManager() == null || tairManager.getNameSpace() <= 0) {
            return centerDataSetItemRuleDTOS;
        }
        Result<DataEntry> dataEntryResult = tairManager.getMultiClusterTairManager().get(tairManager.getNameSpace(), tairKey);
        if (!dataEntryResult.isSuccess() || dataEntryResult.getValue() == null || dataEntryResult.getValue().getValue() == null) {
            return centerDataSetItemRuleDTOS;
        }
        List<PmtRuleDataItemRuleDTO> pmtRuleDataItemRuleDTOS = JSON.parseArray(JSON.toJSONString(dataEntryResult.getValue().getValue()), PmtRuleDataItemRuleDTO.class);
        /**
         * ??????????????????????????????????????????
         */
        pmtRuleDataItemRuleDTOS.removeIf(pmtRuleDataItemRuleDTO -> !this.inUse(pmtRuleDataItemRuleDTO.getPmtRuleDataSetDTO()));
        if (CollectionUtils.isNotEmpty(pmtRuleDataItemRuleDTOS)) {
            centerDataSetItemRuleDTOS = pmtRuleDataItemRuleDTOS.get(0).getDataSetItemRuleDTOList();
            HadesLogUtil.stream(ScenarioConstantApp.TODAY_CRAZY_RECOMMEND_TAB)
                    .kv("method:", "getTairColumnCenterDataSetItemRuleDTO")
                    .kv("tairKey", tairKey)
                    .kv("in use pmtRuleDataSetDTO", JSON.toJSONString(pmtRuleDataItemRuleDTOS.get(0).getPmtRuleDataSetDTO()))
                    .info();
        }
        if (CollectionUtils.isNotEmpty(centerDataSetItemRuleDTOS)) {
            return centerDataSetItemRuleDTOS;
        } else {
            return Lists.newArrayList();
        }
    }

    /**
     * ???????????????
     * ???????????????????????????????????????
     *
     * @return
     */
    private boolean inUse(ColumnCenterPmtRuleDataSetDTO columnCenterPmtRuleDataSetDTO) {
        long nowTime = System.currentTimeMillis();
        if (columnCenterPmtRuleDataSetDTO == null) {
            return false;
        }
        if (columnCenterPmtRuleDataSetDTO.getScheduleStartTime() == null || columnCenterPmtRuleDataSetDTO.getScheduleEndTime() == null) {
            return false;
        }
        long scheduleStartTime = columnCenterPmtRuleDataSetDTO.getScheduleStartTime().getTime();
        long scheduleEndTime = columnCenterPmtRuleDataSetDTO.getScheduleEndTime().getTime();
        return nowTime > scheduleStartTime && nowTime < scheduleEndTime;
    }


    /**
     * ??????????????????
     */
    public List<ColumnCenterDataSetItemRuleDTO> getEntryChannelPriceNew(String cacheKey) {
        //String channelPriceKey = getChannelPriceNewKey();
        String channelPriceKey = cacheKey;
        if (CommonConstant.DEBUG) {
            tacLogger.info("??????????????????getEntryChannelPriceNew:");
        }
        return this.getTairColumnCenterDataSetItemRuleDTO(channelPriceKey);
    }


    /**
     * ?????????????????????
     */
    public List<ColumnCenterDataSetItemRuleDTO> getEntryPromotionPrice(String cacheKey) {
        //String promotionPriceKey = getPromotionPriceKey();
        String promotionPriceKey = cacheKey;
        if (CommonConstant.DEBUG) {
            tacLogger.info("promotionPriceKey:" + promotionPriceKey);
        }
        return this.getTairColumnCenterDataSetItemRuleDTO(promotionPriceKey);
    }


    public DataSourceRequest buildDataSourceRequest(int version, String cacheKey, String tab) {
        DataSourceRequest dataSourceRequest = new DataSourceRequest();
        dataSourceRequest.setVersion(version);
        dataSourceRequest.setTab(tab);
        dataSourceRequest.setCacheKey(cacheKey);
        return dataSourceRequest;
    }


    private String getRandomLogical() {
        try {
            int index = new Random().nextInt(logicalArr.length);
            return logicalArr[index];
        } catch (Exception e) {
        }
        return logicalArr[0];
    }

    public String getChannelPriceNewKey() {//channelPrice_XN_pre
        return this.createKey(CommonConstant.channelPriceNewPrefix, getRandomLogical());
    }

    public String getPromotionPriceKey() {//channelPrice_XN_pre
        return this.createKey(CommonConstant.promotionPricePrefix, getRandomLogical());
    }

    public String createKey(String prefix, Object... args) {
        try {
            checkArgument(args.length > 0, "key args.length can not be 0");
            StringBuilder buf = new StringBuilder(prefix);
            for (Object arg : args) {
                buf.append(String.valueOf(checkNotNull(arg, "key arg"))).append('_');
            }
            // ????????????????????????(??????key)
//            if (RpmContants.enviroment.isPreline()) {
//                buf.append("pre_");
//            }
            return buf.substring(0, buf.length() - 1);
        } catch (Exception e) {
            HadesLogUtil.stream(bizScenario.getUniqueIdentity())
                    .kv("step", logKey)
                    .kv("errorCode", "GoCreateKeyExc")
                    .error();
        }
        return "";
    }

    public HashMap<String, String> buildItemIdAndCacheKey(List<com.tmall.tcls.gs.sdk.framework.model.context.ItemEntity> itemEntities) {
        HashMap<String, String> map = Maps.newHashMap();
        itemEntities.forEach(itemEntity -> {
            if (itemEntity.getExtMap() != null && itemEntity.getExtMap().get("todayCrazyChannel") != null) {
                map.put(itemEntity.getItemId().toString(), itemEntity.getExtMap().get("todayCrazyChannel").toString());
            } else {
                HadesLogUtil.stream(ScenarioConstantApp.TODAY_CRAZY_RECOMMEND_TAB)
                        .kv("buildItemIdAndCacheKey", "tairKey is null ")
                        .kv("itemEntity", JSON.toJSONString(itemEntity))
                        .info();
                tacLogger.info("tairKey???????????????itemEntity???" + JSON.toJSONString(itemEntity));
            }
        });
        return map;
    }

    public HashMap<String, String> getItemIdAndCacheKey(Map<String, Object> userParams) {
        return (HashMap<String, String>) userParams.get(CommonConstant.ITEM_ID_AND_CACHE_KEYS);
    }

    /**
     * ??????????????????????????????id
     *
     * @return
     */
    public List<String> getItemIdAndCacheKeyList(String tairKey, String appType) {
        DataSourceRequest dataSourceRequest = buildDataSourceRequest(1, tairKey, appType);
        try {
            return topItemIdsIsChannelPriceNew.get(dataSourceRequest);
        } catch (Exception e) {
            HadesLogUtil.stream(bizScenario.getUniqueIdentity())
                    .kv("method", "getItemIdAndCacheKeyList")
                    .kv("dataSourceRequest", JSON.toJSONString(dataSourceRequest))
                    .kv("Exception", JSON.toJSONString(e))
                    .error();
            return Lists.newArrayList();
        }
    }


    /**
     * 30ms???tair?????????????????????????????????LocalCache??????????????????????????????
     */
    private LoadingCache<DataSourceRequest, List<String>> topItemIdsIsChannelPriceNew = CacheBuilder
            .newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build(new CacheLoader<DataSourceRequest, List<String>>() {
                @Override
                public List<String> load(DataSourceRequest dataSourceRequest) {
                    try {
                        List<String> stringList = doGetItemIdAndCacheKeyList(CommonConstant.CHANNEL_ITEM_IDS);
                        if (CommonConstant.DEBUG) {
                            tacLogger.info("topItemIdsIsChannelPriceNew_dataSourceRequest" + JSON.toJSONString(dataSourceRequest));
                            tacLogger.info("topItemIdsIsChannelPrice" + JSON.toJSONString(stringList));
                        }
                        if (CollectionUtils.isEmpty(stringList)) {
                            return Lists.newArrayList();
                        } else {
                            return stringList;
                        }
                    } catch (Exception e) {
                        HadesLogUtil.stream(bizScenario.getUniqueIdentity())
                                .kv("method", "topItemIdsIsChannelPriceNew")
                                .kv("Exception", JSON.toJSONString(e))
                                .error();
                        return Lists.newArrayList();
                    }
                }
            });

    public List<String> doGetItemIdAndCacheKeyList(String tairKey) {
        List<String> itemIds = Lists.newArrayList();
        TairManager tairManager = tairFactorySpi.getOriginDataFailProcessTair();
        if (tairManager == null || tairManager.getMultiClusterTairManager() == null || tairManager.getNameSpace() <= 0) {
            return itemIds;
        }
        Result<DataEntry> dataEntryResult = tairManager.getMultiClusterTairManager().get(tairManager.getNameSpace(), tairKey);
        if (!dataEntryResult.isSuccess() || dataEntryResult.getValue() == null || dataEntryResult.getValue().getValue() == null) {
            return itemIds;
        }
        try {
            itemIds = JSON.parseArray(String.valueOf(dataEntryResult.getValue().getValue()), String.class);
        } catch (Exception e) {
            tacLogger.info("getItemIdAndCacheKeyList json????????????");
            HadesLogUtil.stream(bizScenario.getUniqueIdentity())
                    .kv("method", "getItemIdAndCacheKeyList")
                    .kv("errorCode", "json error")
                    .kv("Exception", JSON.toJSONString(e))
                    .error();
        }
        if (CollectionUtils.isEmpty(itemIds)) {
            HadesLogUtil.stream(bizScenario.getUniqueIdentity())
                    .kv("method", "getItemIdAndCacheKeyList")
                    .kv("errorCode", "itemIds is null")
                    .error();
        }
        return itemIds;
    }


}
