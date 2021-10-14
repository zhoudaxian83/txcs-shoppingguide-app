package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.tmall.aselfmanager.client.columncenter.response.ColumnCenterDataRuleDTO;
import com.tmall.aselfmanager.client.columncenter.response.ColumnCenterDataSetItemRuleDTO;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataSuccessProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.OriginDataProcessRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.extensions.failprocessor.ItemFailProcessorRequest;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.constant.CommonConstant;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.constant.TabTypeEnum;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.model.TodayCrazySortItemEntity;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.service.TodayCrazyTairCacheService;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.util.CommonUtil;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
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


    @Override
    public OriginDataDTO<ItemEntity> process(OriginDataProcessRequest originDataProcessRequest) {
        //已曝光置顶itemIds
        String entryItemIdsStr = MapUtil.getStringWithDefault(originDataProcessRequest.getSgFrameworkContextItem().getRequestParams(), "entryItemIds", "");
        List<String> entryItemIds = entryItemIdsStr.equals("") ? Lists.newArrayList() : Arrays.asList(entryItemIdsStr.split(","));
        tacLogger.info("entryItemIds:" + JSON.toJSONString(entryItemIds));
        //鸿雁置顶itemIds
        String topListStr = MapUtil.getStringWithDefault(originDataProcessRequest.getSgFrameworkContextItem().getRequestParams(), "topList", "");
        List<String> topList = topListStr.equals("") ? Lists.newArrayList() : Arrays.asList(topListStr.split(","));
        tacLogger.info("topList:" + JSON.toJSONString(topList));
        boolean isFirstPage = (boolean) originDataProcessRequest.getSgFrameworkContextItem().getUserParams().get("isFirstPage");
        String tabType = MapUtil.getStringWithDefault(originDataProcessRequest.getSgFrameworkContextItem().getRequestParams(), "tabType", "");
        // 1,融合置顶商品；2，商品去重处理  直接把入参中的置顶商品置顶，每次查询进行去重处理
        OriginDataDTO<ItemEntity> originDataDTO = originDataProcessRequest.getItemEntityOriginDataDTO();
        List<ItemEntity> itemEntities = originDataDTO.getResult();
        tacLogger.info("debug-a");
        //保存tairKey和item关联供后面逻辑使用
        originDataProcessRequest.getSgFrameworkContextItem().getUserParams().put(CommonConstant.ITEM_ID_AND_CACHE_KEYS, CommonUtil.buildItemIdAndCacheKey(itemEntities));
        ItemFailProcessorRequest itemFailProcessorRequest = JSON.parseObject(JSON.toJSONString(originDataProcessRequest), ItemFailProcessorRequest.class);
        //tpp请求成功写入缓存，供失败打底使用
        todayCrazyTairCacheService.process(itemFailProcessorRequest);
        tacLogger.info("debug-b");
        //排序优先级：已曝光>鸿雁>坑位排序
        try {
            entryItemIds.addAll(topList);
        }catch (Exception e){
            tacLogger.info("这不该报错吧。。");
        }
        tacLogger.info("debug-c");
        tacLogger.info("合并结果：" + JSON.toJSONString(entryItemIds));
        if (TabTypeEnum.TODAY_CHAO_SHENG.getType().equals(tabType)) {
            tacLogger.info("debug-1");
            this.itemSort(originDataDTO, isFirstPage);
        }
        if (CollectionUtils.isNotEmpty(entryItemIds)) {
            tacLogger.info("debug-2");
            this.doTopItems(originDataDTO, entryItemIds, isFirstPage);
        }
        return originDataDTO;
    }

    /**
     * 置顶排序
     *
     * @param originDataDTO
     * @param topList
     * @param isFirstPage
     */
    public void doTopItems(OriginDataDTO<ItemEntity> originDataDTO, List<String> topList, boolean isFirstPage) {
        //如果是第一页去除重复且置顶，非第一页只去重
        List<ItemEntity> itemEntities = originDataDTO.getResult();
        // 只有今日超省走双置顶逻辑，1，双中判断置顶有效期；2，只有第一页做置顶这个置顶逻辑；3每页走要进行置顶去重
        tacLogger.info("topList：" + JSON.toJSONString(topList));
        tacLogger.info("TPP返回数据条数：" + itemEntities.size());
        tacLogger.info("TPP返回数据itemEntities：" + JSON.toJSONString(itemEntities));
        itemEntities.removeIf(itemEntity -> topList.contains(String.valueOf(itemEntity.getItemId())));
        tacLogger.info("isFirstPage：" + isFirstPage);
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
            tacLogger.info("过滤后条数：" + itemEntities.size());
            topResultsItemEntityList.addAll(itemEntities);
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
    private void itemSort(OriginDataDTO<ItemEntity> originDataDTO, boolean isFirstPage) {
        tacLogger.info("debug-3");
        List<ItemEntity> itemEntities = originDataDTO.getResult();
        List<ColumnCenterDataSetItemRuleDTO> sortItems = this.getSortItems();
        tacLogger.info("debug-4");
        Pair<List<Long>, List<ColumnCenterDataSetItemRuleDTO>> pair = this.getNeedEnterDataSetItemRuleDTOS(sortItems);
        List<ColumnCenterDataSetItemRuleDTO> needEnterDataSetItemRuleDTOS = pair.getRight();
        List<Long> itemIdList = pair.getLeft();
        tacLogger.info("debug-5");
        //去重原有的
        itemEntities.removeIf(itemEntity -> itemIdList.contains(itemEntity.getItemId()));
        tacLogger.info("过滤后效果itemStickEndTime：" + JSON.toJSONString(needEnterDataSetItemRuleDTOS));
        //只有首页进行置顶操作，但每一页需要去重操作
        if (isFirstPage && CollectionUtils.isNotEmpty(needEnterDataSetItemRuleDTOS)) {
            //资源位操作
            originDataDTO.setResult(this.doItemSort(itemEntities, needEnterDataSetItemRuleDTOS));
        }
    }

    private List<ItemEntity> doItemSort(List<ItemEntity> itemEntities, List<ColumnCenterDataSetItemRuleDTO> needEnterDataSetItemRuleDTOS) {
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

        //定坑排序
        List<TodayCrazySortItemEntity> todayCrazySortItemEntities2 = todayCrazySortItemEntities.stream().sorted(
                Comparator.comparing(TodayCrazySortItemEntity::getIndex)).collect(
                Collectors.toList());
        for (TodayCrazySortItemEntity todayCrazySortItemEntity : todayCrazySortItemEntities2) {
            long index = todayCrazySortItemEntity.getIndex();
            if (todayCrazySortItemEntity.getIndex() > itemEntities.size()) {
                //坑位大于总条数放末尾
                itemEntities.add(todayCrazySortItemEntity.getItemEntity());
            } else {
                itemEntities.add((int) index - 1, todayCrazySortItemEntity.getItemEntity());
            }
        }
        return itemEntities;
    }

    /**
     * 过滤出需要坑位信息
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
                itemList.add(columnCenterDataSetItemRuleDTO.getItemId());
                needEnterDataSetItemRuleDTOS.add(columnCenterDataSetItemRuleDTO);
            }
        });
        return Pair.of(itemList, needEnterDataSetItemRuleDTOS);
    }

    private boolean isNeedSort(ColumnCenterDataRuleDTO columnCenterDataRuleDTO) {
        Date nowDate = new Date();
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
        return nowDate.after(itemScheduleStartTime) && nowDate.before(itemScheduleEndTime) && nowDate.after(itemStickStartTime) && nowDate.before(itemStickEndTime);
    }

    private String transform(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  // 设置日期格式
        return simpleDateFormat.format(date);  // 格式转换
    }


    private List<ColumnCenterDataSetItemRuleDTO> getSortItems() {
        List<ColumnCenterDataSetItemRuleDTO> centerDataSetItemRuleDTOS = Lists.newArrayList();
        List<ColumnCenterDataSetItemRuleDTO> entryChannelPriceNew = todayCrazyTairCacheService.getEntryChannelPriceNew();
        List<ColumnCenterDataSetItemRuleDTO> entryPromotionPrice = todayCrazyTairCacheService.getEntryPromotionPrice();
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(entryChannelPriceNew)) {
            centerDataSetItemRuleDTOS.addAll(entryChannelPriceNew);
        }
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(entryPromotionPrice)) {
            centerDataSetItemRuleDTOS.addAll(entryPromotionPrice);
        }
        return centerDataSetItemRuleDTOS;
    }

}
