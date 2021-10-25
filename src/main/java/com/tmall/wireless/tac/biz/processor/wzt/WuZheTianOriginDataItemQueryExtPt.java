package com.tmall.wireless.tac.biz.processor.wzt;

import com.ali.com.google.common.base.Joiner;
import com.ali.unit.rule.util.lang.CollectionUtils;
import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.aselfmanager.client.columncenter.response.ColumnCenterDataRuleDTO;
import com.tmall.aselfmanager.client.columncenter.response.ColumnCenterDataSetItemRuleDTO;
import com.tmall.aselfmanager.client.columncenter.response.PmtRuleDataItemRuleDTO;
import com.tmall.txcs.biz.supermarket.extpt.origindata.ConvertUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataItemQueryExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.txcs.gs.model.model.dto.RecommendResponseEntity;
import com.tmall.txcs.gs.model.model.dto.tpp.RecommendItemEntityDTO;
import com.tmall.txcs.gs.model.spi.model.RecommendRequest;
import com.tmall.txcs.gs.spi.recommend.RecommendSpi;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.wzt.constant.Constant;
import com.tmall.wireless.tac.biz.processor.wzt.model.DataContext;
import com.tmall.wireless.tac.biz.processor.wzt.model.SortItemEntity;
import com.tmall.wireless.tac.biz.processor.wzt.utils.LogicPageUtil;
import com.tmall.wireless.tac.biz.processor.wzt.utils.SmAreaIdUtil;
import com.tmall.wireless.tac.biz.processor.wzt.utils.TairUtil;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import io.reactivex.Flowable;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author luojunchong
 */
@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.WU_ZHE_TIAN)
@Service
public class WuZheTianOriginDataItemQueryExtPt implements OriginDataItemQueryExtPt {

    @Autowired
    TairUtil tairUtil;

    @Autowired
    RecommendSpi recommendSpi;

    @Autowired
    TacLogger tacLogger;

    @Override
    public Flowable<OriginDataDTO<ItemEntity>> process(SgFrameworkContextItem context) {
        DataContext dataContext = new DataContext();
        Long smAreaId = SmAreaIdUtil.getSmAreaId(context);
        Long userId = MapUtil.getLongWithDefault(context.getRequestParams(), "userId", 0L);
        Long index = MapUtil.getLongWithDefault(context.getRequestParams(), "index", 1L);
        Long pageSize = MapUtil.getLongWithDefault(context.getRequestParams(), "pageSize", 20L);
        dataContext.setIndex(index);
        dataContext.setPageSize(pageSize);
        /**
         * tair获取推荐活动
         */
        PmtRuleDataItemRuleDTO pmtRuleDataItemRuleDTO = tairUtil.getPmtRuleDataItemRuleDTO(smAreaId);
        /**
         * 保存channelKey到上下文中，供captain参数构建使用
         */
        context.getUserParams().put(Constant.CHANNEL_KEY, tairUtil.getChannelKeyV2(pmtRuleDataItemRuleDTO));
        /**
         * tair获取推荐商品
         */
        List<ColumnCenterDataSetItemRuleDTO> columnCenterDataSetItemRuleDTOList = tairUtil.getOriginalRecommend(pmtRuleDataItemRuleDTO);
        if (Constant.DEBUG) {
            tacLogger.info("返回原始缓存过滤前(当前时间没有活动或者没有商品信息)：" + JSON.toJSONString(columnCenterDataSetItemRuleDTOList));
        }
        /**
         * 去除掉活动冲不满足当前时间的商品
         */
        columnCenterDataSetItemRuleDTOList.removeIf(columnCenterDataSetItemRuleDTO -> !needData(columnCenterDataSetItemRuleDTO.getDataRule()));
        if (Constant.DEBUG) {
            tacLogger.info("返回原始缓存过滤后：" + JSON.toJSONString(columnCenterDataSetItemRuleDTOList));
        }

        /**
         * 获取id和排序信息
         */
        Map<Long, Long> stringLongMap = new HashMap<>(16);
        List<Long> items = Lists.newArrayList();
        columnCenterDataSetItemRuleDTOList.forEach(columnCenterDataSetItemRuleDTO -> {
            stringLongMap.put(columnCenterDataSetItemRuleDTO.getItemId(), columnCenterDataSetItemRuleDTO.getDataRule().getStick());
            items.add(columnCenterDataSetItemRuleDTO.getItemId());
        });
        return recommendSpi.recommendItem(this.buildRecommendRequestParam(userId, items))
                .map(recommendResponseEntityResponse -> {
                    if (!recommendResponseEntityResponse.isSuccess()
                            || recommendResponseEntityResponse.getValue() == null
                            || CollectionUtils.isEmpty(recommendResponseEntityResponse.getValue().getResult())) {
                        if (Constant.DEBUG) {
                            tacLogger.info("结果集为空，会走默认打底逻辑");
                        }
                        return new OriginDataDTO<>();
                    }
                    if (Constant.DEBUG) {
                        tacLogger.info("tpp返回结果：" + JSON.toJSONString(recommendResponseEntityResponse.getValue()));
                    }

                    OriginDataDTO<ItemEntity> originDataDTO = convert(recommendResponseEntityResponse.getValue());

                    if (Constant.DEBUG) {
                        tacLogger.info("tpp返回结果originDataDTO：" + JSON.toJSONString(originDataDTO));
                    }

                    this.sortItemEntityList(originDataDTO, stringLongMap);
                    return this.getItemPage(originDataDTO, dataContext);
                });
    }

    /**
     * 1，需要再展示时间内，不需要坑位时间
     *
     * @param columnCenterDataRuleDTO
     * @return
     */
    private boolean needData(ColumnCenterDataRuleDTO columnCenterDataRuleDTO) {
        long nowTime = System.currentTimeMillis();
        if (columnCenterDataRuleDTO == null) {
            return false;
        }
        Date itemScheduleStartDate = columnCenterDataRuleDTO.getItemScheduleStartTime();
        Date itemScheduleEndDate = columnCenterDataRuleDTO.getItemScheduleEndTime();
        if (itemScheduleStartDate == null || itemScheduleEndDate == null) {
            return false;
        }
        long itemScheduleStartTime = itemScheduleStartDate.getTime();
        long itemScheduleEndTime = itemScheduleEndDate.getTime();

        boolean needData = itemScheduleStartTime < nowTime && itemScheduleEndTime > nowTime;
        if (Constant.DEBUG) {
            tacLogger.info("时间过滤：nowTime=" + nowTime + "itemScheduleStartDate=" + itemScheduleStartDate + "itemScheduleEndDate" + itemScheduleEndDate);
            tacLogger.info("在排期内的商品：" + needData);
        }
        return needData;
    }


    private void sortItemEntityList(OriginDataDTO<ItemEntity> originDataDTO, Map<Long, Long> stringLongMap) {
        List<SortItemEntity> resultItemEntityList = Lists.newArrayList();
        List<SortItemEntity> sortItemEntityList = Lists.newArrayList();
        originDataDTO.getResult().forEach(itemEntity -> {
            Long index = stringLongMap.get(itemEntity.getItemId());
            SortItemEntity sortItemEntity = new SortItemEntity();
            sortItemEntity.setItemEntity(itemEntity);
            sortItemEntity.setIndex(stringLongMap.get(itemEntity.getItemId()));
            if (index != null && !Constant.INDEX.equals(index)) {
                sortItemEntityList.add(sortItemEntity);
            } else {
                resultItemEntityList.add(sortItemEntity);
            }
        });
        List<SortItemEntity> sortItemEntityList2 = sortItemEntityList.stream().sorted(
                Comparator.comparing(SortItemEntity::getIndex)).collect(
                Collectors.toList());
        if (Constant.DEBUG) {
            tacLogger.info("定坑商品：" + JSON.toJSONString(sortItemEntityList2));
        }
        //如果能按顺序插入按顺序插入，大于总数放最后面
        for (SortItemEntity sortItemEntity : sortItemEntityList2) {
            long index = sortItemEntity.getIndex();
            if (index > resultItemEntityList.size()) {
                resultItemEntityList.add(sortItemEntity);
            } else {
                resultItemEntityList.add((int) index - 1, sortItemEntity);
            }
        }
        List<ItemEntity> itemEntityList = resultItemEntityList.stream().map(
                SortItemEntity::getItemEntity).collect(Collectors.toList());
        originDataDTO.setResult(itemEntityList);
    }

    /**
     * tpp获取个性化排序规则参数构建
     *
     * @param userId
     * @param itemIds
     * @return
     */
    private RecommendRequest buildRecommendRequestParam(Long userId, List<Long> itemIds) {
        RecommendRequest recommendRequest = new RecommendRequest();
        recommendRequest.setLogResult(true);
        recommendRequest.setUserId(userId);
        recommendRequest.setAppId(Constant.APP_ID);
        Map<String, String> params = Maps.newHashMap();
        params.put("userItemIdList", Joiner.on(",").join(itemIds));
        recommendRequest.setParams(params);
        return recommendRequest;
    }

    /**
     * 分页
     *
     * @param originDataDTO
     * @param dataContext
     * @return
     */
    private OriginDataDTO<ItemEntity> getItemPage(OriginDataDTO<ItemEntity> originDataDTO, DataContext dataContext) {
        Pair<Boolean, List<ItemEntity>> pair = LogicPageUtil.getPage(originDataDTO.getResult(), dataContext.getIndex(),
                dataContext.getPageSize());
        List<ItemEntity> itemEntities = pair.getRight();
        originDataDTO.setHasMore(pair.getLeft());
        originDataDTO.setResult(itemEntities);
        return originDataDTO;
    }

    /**
     * 未缓存前
     *
     * @param recommendResponseEntity
     * @return
     */
    private OriginDataDTO<ItemEntity> convert(RecommendResponseEntity<RecommendItemEntityDTO> recommendResponseEntity) {
        OriginDataDTO<ItemEntity> originDataDTO = new OriginDataDTO<>();
        originDataDTO.setHasMore(recommendResponseEntity.isHasMore());
        originDataDTO.setIndex(recommendResponseEntity.getIndex());
        originDataDTO.setPvid(recommendResponseEntity.getPvid());
        originDataDTO.setScm(recommendResponseEntity.getScm());
        originDataDTO.setTppBuckets(recommendResponseEntity.getTppBuckets());
        originDataDTO.setResult(recommendResponseEntity
                .getResult()
                .stream()
                .filter(Objects::nonNull).map(ConvertUtil::convert).collect(Collectors.toList()));
        return originDataDTO;
    }

}
