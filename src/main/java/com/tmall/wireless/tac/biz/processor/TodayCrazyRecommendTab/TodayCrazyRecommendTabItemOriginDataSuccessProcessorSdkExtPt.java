package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.tmall.aselfmanager.client.columncenter.response.ColumnCenterDataRuleDTO;
import com.tmall.aselfmanager.client.columncenter.response.ColumnCenterDataSetItemRuleDTO;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataSuccessProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.OriginDataProcessRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.constant.CommonConstant;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.constant.TabTypeEnum;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.model.DataSourceRequest;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.model.TodayCrazySortItemEntity;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.service.TodayCrazyTairCacheService;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created from template by 罗俊冲 on 2021-09-23 14:14:31.
 * TPP获取成功后
 */

@SdkExtension(
        bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.TODAY_CRAZY_RECOMMEND_TAB
)
public class TodayCrazyRecommendTabItemOriginDataSuccessProcessorSdkExtPt extends Register implements ItemOriginDataSuccessProcessorSdkExtPt {
    @Autowired
    TacLoggerImpl tacLogger;

    @Autowired
    TodayCrazyTairCacheService todayCrazyTairCacheService;

    private static final BizScenario bizScenario = BizScenario.valueOf(
            ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
            ScenarioConstantApp.LOC_TYPE_B2C,
            ScenarioConstantApp.TODAY_CRAZY_RECOMMEND_TAB
    );

    @Override
    public OriginDataDTO<ItemEntity> process(OriginDataProcessRequest originDataProcessRequest) {
        //tacLogger.info("TPP返回数据条数：" + originDataProcessRequest.getItemEntityOriginDataDTO().getResult().size());
        //tacLogger.info("TPP返回数据结果：" + JSON.toJSONString(originDataProcessRequest.getItemEntityOriginDataDTO().getResult()));

        /**
         * 用户置顶
         */
        String crowdTopStr = MapUtil.getStringWithDefault(originDataProcessRequest.getSgFrameworkContextItem().getRequestParams(), "crowdTop", "");
        List<String> crowdTopList = crowdTopStr.equals("") ? Lists.newArrayList() : Arrays.asList(crowdTopStr.split(","));
        /**
         * 鸿雁置顶itemIds和已曝光置顶itemIds,按照前端入参顺序(前端已做合并，原先是已曝光置顶itemIds在最上面，然后是鸿雁置顶itemIds的)
         */
        String topListStr = MapUtil.getStringWithDefault(originDataProcessRequest.getSgFrameworkContextItem().getRequestParams(), "topList", "");
        List<String> topList = topListStr.equals("") ? Lists.newArrayList() : Arrays.asList(topListStr.split(","));
        boolean isFirstPage = (boolean) originDataProcessRequest.getSgFrameworkContextItem().getUserParams().get("isFirstPage");
        tacLogger.info("isFirstPage：" + isFirstPage);
        String tabType = MapUtil.getStringWithDefault(originDataProcessRequest.getSgFrameworkContextItem().getRequestParams(), "tabType", "");
        String appType = MapUtil.getStringWithDefault(originDataProcessRequest.getSgFrameworkContextItem().getRequestParams(), "appType", "");


        OriginDataDTO<ItemEntity> originDataDTO = originDataProcessRequest.getItemEntityOriginDataDTO();
        List<ItemEntity> itemEntities = originDataDTO.getResult();

        /**
         * tpp请求成功写入缓存，供失败打底使用
         */
        todayCrazyTairCacheService.process(originDataProcessRequest);

        /**
         * 排序优先级：已曝光>鸿雁>坑位排序(保证顺序去重)
         * 去重的同时保证入参的顺序
         */
        List<String> topItemIds = Lists.newArrayList();
        /**
         * 用户特殊置顶在所有置顶之上
         */
        if (CollectionUtils.isNotEmpty(crowdTopList)) {
            topItemIds.addAll(crowdTopList);
        }
        /**
         * 对所有需要置顶的商品去重处理，保证优先出现的顺序不变
         */
        topList.forEach(s -> {
            if (!topItemIds.contains(s)) {
                topItemIds.add(s);
            }
        });
        tacLogger.info("topList去重后" + JSON.toJSONString(topItemIds));

        /**
         * 保存tairKey和item关联关系。供后面逻辑使用查询限购，区分渠道的判断依据
         */
        HashMap<String, String> itemIdAndCacheKey = new HashMap<>(todayCrazyTairCacheService.buildItemIdAndCacheKey(itemEntities));


        /**
         * 只有今日超省才走定坑逻辑
         */
        if (TabTypeEnum.TODAY_CHAO_SHENG.getType().equals(tabType)) {
            this.itemSortV2(originDataDTO, isFirstPage, itemIdAndCacheKey, appType);
        }

        /**
         * 置顶逻辑，根据topItemIds前端传入的顺序置顶操作
         */
        if (CollectionUtils.isNotEmpty(topItemIds)) {
            this.doTopItems(originDataDTO, topItemIds, isFirstPage);
        }

        /**
         * 如果topItemIds在专享价中则优先打专享标
         *
         */
        this.topItemIdsIsChannelPriceNew(topItemIds, itemIdAndCacheKey, appType);

        /**
         * 保存到上下文中，供后面查询限购，区分渠道的判断依据
         */
        originDataProcessRequest.getSgFrameworkContextItem().getUserParams().put(CommonConstant.ITEM_ID_AND_CACHE_KEYS, itemIdAndCacheKey);

        HadesLogUtil.stream(ScenarioConstantApp.TODAY_CRAZY_RECOMMEND_TAB)
                .kv("class", "TodayCrazyRecommendTabItemOriginDataSuccessProcessorSdkExtPt")
                .kv("tpp data size", Integer.toString(originDataDTO.getResult().size()))
                .info();
        tacLogger.info("tpp最终条数：" + originDataDTO.getResult().size());
        return originDataDTO;
    }

    /**
     * 如果前端入参的itemId存在专享价则优先打专享标
     *
     * @param topItemIds
     * @param itemIdAndCacheKey
     */
    private void topItemIdsIsChannelPriceNew(List<String> topItemIds, HashMap<String, String> itemIdAndCacheKey, String appType) {
        List<String> allChannelPriceNewItemIds = todayCrazyTairCacheService.getItemIdAndCacheKeyList(CommonConstant.CHANNEL_ITEM_IDS, appType);
        if (CollectionUtils.isEmpty(allChannelPriceNewItemIds)) {
            return;
        }
        topItemIds.forEach(itemId -> {
            if (allChannelPriceNewItemIds.contains(itemId)) {
                tacLogger.info("专享价打标成功，itemId=" + itemId);
                itemIdAndCacheKey.put(itemId, CommonConstant.TODAY_CHANNEL_NEW);
            }
        });
    }


    /**
     * 置顶排序
     *
     * @param originDataDTO
     * @param topList
     * @param isFirstPage
     */
    public void doTopItems(OriginDataDTO<ItemEntity> originDataDTO, List<String> topList, boolean isFirstPage) {
        /**
         * 如果是第一页去除重复且置顶，非第一页只去重
         */
        List<ItemEntity> itemEntities = originDataDTO.getResult();

        /**
         * 只有今日超省走双置顶逻辑，1，双中判断置顶有效期；2，只有第一页做置顶这个置顶逻辑；3每页走要进行置顶去重
         */
        itemEntities.removeIf(itemEntity -> topList.contains(String.valueOf(itemEntity.getItemId())));
        tacLogger.info("过滤后条数：" + itemEntities.size());
        if (isFirstPage) {
            List<ItemEntity> topResultsItemEntityList = Lists.newArrayList();
            topList.forEach(itemId -> {
                ItemEntity itemEntity = new ItemEntity();
                itemEntity.setO2oType("B2C");
                itemEntity.setBizType("sm");
                itemEntity.setItemId(Long.valueOf(itemId));
                itemEntity.setTop(true);
                topResultsItemEntityList.add(itemEntity);
            });
            topResultsItemEntityList.addAll(itemEntities);
            tacLogger.info("加上topList总条数:" + topResultsItemEntityList.size());
            originDataDTO.setResult(topResultsItemEntityList);
        } else {
            originDataDTO.setResult(itemEntities);
        }
    }

    /**
     * 根据资源位置顶操作
     *
     * @param originDataDTO
     * @param isFirstPage
     */
    private void itemSortV2(OriginDataDTO<ItemEntity> originDataDTO, boolean isFirstPage, HashMap<String, String> itemIdAndCacheKey, String appType) {
        /**
         * tpp返回的全部结果集
         */
        List<ItemEntity> itemEntities = originDataDTO.getResult();

        /**
         * 全部定坑数据集
         */
        List<ColumnCenterDataSetItemRuleDTO> result = Lists.newArrayList();

        //全部定坑数据itemId
        List<Long> resultItemIds = Lists.newArrayList();

        //获取渠道立减商品和对应itemIds
        Pair<List<Long>, List<ColumnCenterDataSetItemRuleDTO>> entryChannelPriceNewPair = this.getEntryChannelPriceNewPair(appType);
        List<ColumnCenterDataSetItemRuleDTO> entryChannelPriceNew = entryChannelPriceNewPair.getRight();
        List<Long> entryChannelPriceNewItemIdList = entryChannelPriceNewPair.getLeft();
        if (CommonConstant.DEBUG) {
            tacLogger.info("entryChannelPriceNewPair结果集：" + JSON.toJSONString(entryChannelPriceNewPair));
        }

        //获取单品折扣价商品和对应itemIds
        Pair<List<Long>, List<ColumnCenterDataSetItemRuleDTO>> entryPromotionPriceNewPair = this.getEntryPromotionPriceNewPair(appType);
        List<ColumnCenterDataSetItemRuleDTO> entryPromotionPrice = entryPromotionPriceNewPair.getRight();
        List<Long> entryPromotionPriceItemIdList = entryPromotionPriceNewPair.getLeft();
        if (CommonConstant.DEBUG) {
            tacLogger.info("entryPromotionPriceItemIdList结果集：" + JSON.toJSONString(entryChannelPriceNewPair));
        }

        //如果渠道立减商品和单品折扣价商品有重复，则渠道立减商品优先，防止重复
        entryPromotionPrice.removeIf(columnCenterDataSetItemRuleDTO -> entryChannelPriceNewItemIdList.contains(columnCenterDataSetItemRuleDTO.getItemId()));
        entryPromotionPriceItemIdList.removeIf(entryChannelPriceNewItemIdList::contains);

        //合并后进行定坑处理
        resultItemIds.addAll(entryChannelPriceNewItemIdList);
        resultItemIds.addAll(entryPromotionPriceItemIdList);
        result.addAll(entryChannelPriceNew);
        result.addAll(entryPromotionPrice);
        tacLogger.info("定坑去重后结果：" + JSON.toJSONString(result));
        HadesLogUtil.stream(ScenarioConstantApp.TODAY_CRAZY_RECOMMEND_TAB)
                .kv("result", JSON.toJSONString(result))
                .kv("method", "itemSortV2")
                .info();
        //根据定坑数据对原tpp返回结果集进行去重处理
        itemEntities.removeIf(itemEntity -> resultItemIds.contains(itemEntity.getItemId()));
        //只有首页才进行定坑处理，且要有坑位数据
        if (isFirstPage && CollectionUtils.isNotEmpty(result)) {
            //保存tairKey和item关联关系。供后面逻辑使用查询限购，区分渠道的判断依据(因为定坑商品也要展示渠道)
            entryPromotionPriceItemIdList.forEach(itemId -> itemIdAndCacheKey.put(Long.toString(itemId), CommonConstant.TODAY_PROMOTION));
            entryChannelPriceNewItemIdList.forEach(itemId -> itemIdAndCacheKey.put(Long.toString(itemId), CommonConstant.TODAY_CHANNEL_NEW));
            //坑位排序
            originDataDTO.setResult(this.doItemSort(itemEntities, result));
        } else {
            originDataDTO.setResult(itemEntities);
        }
    }

    /**
     * 本地缓存模式
     *
     * @param appType
     * @return
     */
    public Pair<List<Long>, List<ColumnCenterDataSetItemRuleDTO>> getEntryChannelPriceNewPair(String appType) {
        String channelPriceKey = todayCrazyTairCacheService.getChannelPriceNewKey();
        DataSourceRequest dataSourceRequest = todayCrazyTairCacheService.buildDataSourceRequest(1, channelPriceKey, appType);
        try {
            return entryChannelPriceNewPair.get(dataSourceRequest);
        } catch (Exception e) {
            HadesLogUtil.stream(bizScenario.getUniqueIdentity())
                    .kv("method", "getEntryChannelPriceNewPair")
                    .kv("dataSourceRequest", JSON.toJSONString(dataSourceRequest))
                    .kv("Exception", JSON.toJSONString(e))
                    .error();
            return Pair.of(Lists.newArrayList(), Lists.newArrayList());
        }
    }

    /**
     * 本地缓存模式
     *
     * @param appType
     * @return
     */
    public Pair<List<Long>, List<ColumnCenterDataSetItemRuleDTO>> getEntryPromotionPriceNewPair(String appType) {
        String channelPriceKey = todayCrazyTairCacheService.getPromotionPriceKey();
        DataSourceRequest dataSourceRequest = todayCrazyTairCacheService.buildDataSourceRequest(1, channelPriceKey, appType);
        try {
            return entryPromotionPriceNewPair.get(dataSourceRequest);
        } catch (Exception e) {
            HadesLogUtil.stream(bizScenario.getUniqueIdentity())
                    .kv("method", "getEntryPromotionPriceNewPair")
                    .kv("dataSourceRequest", JSON.toJSONString(dataSourceRequest))
                    .kv("Exception", JSON.toJSONString(e))
                    .error();
            return Pair.of(Lists.newArrayList(), Lists.newArrayList());
        }
    }


    /**
     * 30ms从tair加载最新的值，此处使用LocalCache的目的是为了避免热点 todo 极值验证10000
     */
    private LoadingCache<DataSourceRequest, Pair<List<Long>, List<ColumnCenterDataSetItemRuleDTO>>> entryChannelPriceNewPair = CacheBuilder
            .newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build(new CacheLoader<DataSourceRequest, Pair<List<Long>, List<ColumnCenterDataSetItemRuleDTO>>>() {
                @Override
                public Pair<List<Long>, List<ColumnCenterDataSetItemRuleDTO>> load(DataSourceRequest dataSourceRequest) {
                    try {
                        Pair<List<Long>, List<ColumnCenterDataSetItemRuleDTO>> pair = getNeedEnterDataSetItemRuleDTOS(todayCrazyTairCacheService.getEntryChannelPriceNew(dataSourceRequest.getCacheKey()));
                        if (CommonConstant.DEBUG) {
                            tacLogger.info("entryChannelPriceNewPair_dataSourceRequest" + JSON.toJSONString(dataSourceRequest));
                            tacLogger.info("entryChannelPriceNewPair_pair" + JSON.toJSONString(pair));
                        }
                        return pair;
                    } catch (Exception e) {
                        HadesLogUtil.stream(bizScenario.getUniqueIdentity())
                                .kv("method", "entryChannelPriceNewPair")
                                .kv("Exception", JSON.toJSONString(e))
                                .error();
                        return Pair.of(Lists.newArrayList(), Lists.newArrayList());
                    }
                }
            });

    /**
     * 30ms从tair加载最新的值，此处使用LocalCache的目的是为了避免热点
     */
    private LoadingCache<DataSourceRequest, Pair<List<Long>, List<ColumnCenterDataSetItemRuleDTO>>> entryPromotionPriceNewPair = CacheBuilder
            .newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build(new CacheLoader<DataSourceRequest, Pair<List<Long>, List<ColumnCenterDataSetItemRuleDTO>>>() {
                @Override
                public Pair<List<Long>, List<ColumnCenterDataSetItemRuleDTO>> load(DataSourceRequest dataSourceRequest) {
                    try {
                        Pair<List<Long>, List<ColumnCenterDataSetItemRuleDTO>> pair = getNeedEnterDataSetItemRuleDTOS(todayCrazyTairCacheService.getEntryPromotionPrice(dataSourceRequest.getCacheKey()));
                        if (CommonConstant.DEBUG) {
                            tacLogger.info("entryPromotionPriceNewPair_dataSourceRequest" + JSON.toJSONString(dataSourceRequest));
                            tacLogger.info("entryPromotionPriceNewPair_pair" + JSON.toJSONString(pair));
                        }
                        return pair;
                    } catch (Exception e) {
                        HadesLogUtil.stream(bizScenario.getUniqueIdentity())
                                .kv("method", "entryPromotionPriceNewPair")
                                .kv("Exception", JSON.toJSONString(e))
                                .error();
                        return Pair.of(Lists.newArrayList(), Lists.newArrayList());
                    }
                }
            });


    private List<ItemEntity> doItemSort(List<ItemEntity> itemEntities, List<ColumnCenterDataSetItemRuleDTO> needEnterDataSetItemRuleDTOS) {
        if (CollectionUtils.isEmpty(needEnterDataSetItemRuleDTOS)) {
            return itemEntities;
        }

        /**
         * 组装坑位数据为TPP需要的标准入参数据，做排序准备
         */
        List<TodayCrazySortItemEntity> todayCrazySortItemEntities = Lists.newArrayList();
        needEnterDataSetItemRuleDTOS.forEach(columnCenterDataSetItemRuleDTO -> {
            ColumnCenterDataRuleDTO columnCenterDataRuleDTO = columnCenterDataSetItemRuleDTO.getDataRule();
            TodayCrazySortItemEntity todayCrazySortItemEntity = new TodayCrazySortItemEntity();
            ItemEntity itemEntity = new ItemEntity();
            itemEntity.setO2oType("B2C");
            itemEntity.setBizType("sm");
            itemEntity.setItemId(columnCenterDataSetItemRuleDTO.getItemId());
            itemEntity.setTop(true);
            todayCrazySortItemEntity.setItemEntity(itemEntity);
            todayCrazySortItemEntity.setIndex(columnCenterDataRuleDTO.getStick());
            todayCrazySortItemEntities.add(todayCrazySortItemEntity);
        });

        /**
         * 对坑位做排序处理
         */
        List<TodayCrazySortItemEntity> todayCrazySortItemEntities2 = todayCrazySortItemEntities.stream().sorted(
                Comparator.comparing(TodayCrazySortItemEntity::getIndex)).collect(
                Collectors.toList());

        /**
         * 根据坑位先后顺序向原有tpp结果数据中进行插入,当坑位值大于总数据条数的时候就放在末尾（正常情况下每页返回20条不会出现）
         */
        for (TodayCrazySortItemEntity todayCrazySortItemEntity : todayCrazySortItemEntities2) {
            long index = todayCrazySortItemEntity.getIndex();
            if (todayCrazySortItemEntity.getIndex() > itemEntities.size()) {
                itemEntities.add(todayCrazySortItemEntity.getItemEntity());
            } else {
                itemEntities.add((int) index - 1, todayCrazySortItemEntity.getItemEntity());
            }
        }
        return itemEntities;
    }

    /**
     * 过滤出需要坑位信息,且去重处理（一般不会重复的）
     *
     * @param columnCenterDataSetItemRuleDTOS
     * @return
     */
    private Pair<List<Long>, List<ColumnCenterDataSetItemRuleDTO>> getNeedEnterDataSetItemRuleDTOS(List<ColumnCenterDataSetItemRuleDTO> columnCenterDataSetItemRuleDTOS) {
        List<ColumnCenterDataSetItemRuleDTO> needEnterDataSetItemRuleDTOS = Lists.newArrayList();
        List<Long> itemList = Lists.newArrayList();
        columnCenterDataSetItemRuleDTOS.forEach(columnCenterDataSetItemRuleDTO -> {
            ColumnCenterDataRuleDTO columnCenterDataRuleDTO = columnCenterDataSetItemRuleDTO.getDataRule();
            if (this.isNeedSort(columnCenterDataRuleDTO) && !itemList.contains(columnCenterDataSetItemRuleDTO.getItemId())) {
                //tacLogger.info("去重保留itemId：：" + columnCenterDataSetItemRuleDTO.getItemId());
                itemList.add(columnCenterDataSetItemRuleDTO.getItemId());
                needEnterDataSetItemRuleDTOS.add(columnCenterDataSetItemRuleDTO);
            }
        });
        return Pair.of(itemList, needEnterDataSetItemRuleDTOS);
    }

    /**
     * 1，必须同时有排序起止时间和置顶起止时间
     * 2，当前时间必须同时在排期和置顶的起止时间段内
     *
     * @param columnCenterDataRuleDTO
     * @return
     */
    private boolean isNeedSort(ColumnCenterDataRuleDTO columnCenterDataRuleDTO) {
        long nowDate = System.currentTimeMillis();
        if (columnCenterDataRuleDTO == null) {
            return false;
        }
        Date itemScheduleStartTime = columnCenterDataRuleDTO.getItemScheduleStartTime();
        Date itemScheduleEndTime = columnCenterDataRuleDTO.getItemScheduleEndTime();
        Date itemStickStartTime = columnCenterDataRuleDTO.getItemStickStartTime();
        Date itemStickEndTime = columnCenterDataRuleDTO.getItemStickEndTime();
        Long stick = columnCenterDataRuleDTO.getStick();
        if (itemScheduleStartTime == null || itemScheduleEndTime == null || itemStickStartTime == null || itemStickEndTime == null || stick == null) {
            return false;
        }
        long scheduleStartTime = itemScheduleStartTime.getTime();
        long scheduleEndTime = itemScheduleEndTime.getTime();
        long stickStartTime = itemStickStartTime.getTime();
        long stickEndTime = itemStickEndTime.getTime();
        //tacLogger.info("去重判断：nowDate=" + nowDate + "itemScheduleStartTime=" + itemScheduleStartTime + "itemScheduleEndTime=" + itemScheduleEndTime + "itemStickStartTime=" + itemStickStartTime + "itemStickEndTime=" + itemStickEndTime);
        return nowDate > scheduleStartTime && nowDate < scheduleEndTime && nowDate > stickStartTime && nowDate < stickEndTime;
    }


    private List<ColumnCenterDataSetItemRuleDTO> merge(List<ColumnCenterDataSetItemRuleDTO> entryChannelPriceNew, List<ColumnCenterDataSetItemRuleDTO> entryPromotionPrice) {
        List<ColumnCenterDataSetItemRuleDTO> centerDataSetItemRuleDTOS = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(entryChannelPriceNew)) {
            centerDataSetItemRuleDTOS.addAll(entryChannelPriceNew);
        }
        if (CollectionUtils.isNotEmpty(entryPromotionPrice)) {
            centerDataSetItemRuleDTOS.addAll(entryPromotionPrice);
        }
        return centerDataSetItemRuleDTOS;
    }

}
