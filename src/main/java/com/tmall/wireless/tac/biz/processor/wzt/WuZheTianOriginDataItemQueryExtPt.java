package com.tmall.wireless.tac.biz.processor.wzt;

import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import com.ali.com.google.common.base.Joiner;
import com.ali.unit.rule.util.lang.CollectionUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.aself.shoppingguide.client.loc.util.AddressUtil;
import com.tmall.txcs.biz.supermarket.extpt.origindata.ConvertUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.extensions.excutor.SgExtensionExecutor;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataItemQueryExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.model.constant.RpmContants;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.txcs.gs.model.model.dto.RecommendResponseEntity;
import com.tmall.txcs.gs.model.model.dto.tpp.RecommendItemEntityDTO;
import com.tmall.txcs.gs.model.spi.model.RecommendRequest;
import com.tmall.txcs.gs.spi.recommend.RecommendSpi;
import com.tmall.txcs.gs.spi.recommend.TairFactorySpi;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.wzt.enums.LogicalArea;
import com.tmall.wireless.tac.biz.processor.wzt.model.ColumnCenterDataSetItemRuleDTO;
import com.tmall.wireless.tac.biz.processor.wzt.model.DataContext;
import com.tmall.wireless.tac.biz.processor.wzt.model.PmtRuleDataItemRuleDTO;
import com.tmall.wireless.tac.biz.processor.wzt.utils.TairUtil;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author luojunchong
 */
@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.WU_ZHE_TIAN)
@Service
public class WuZheTianOriginDataItemQueryExtPt implements OriginDataItemQueryExtPt {

    @Autowired
    TacLogger tacLogger;

    @Autowired
    TairUtil tairUtil;

    @Autowired
    TairFactorySpi tairFactorySpi;

    /**
     * 分大区个性化排序后商品缓存后缀
     */
    private static final String AREA_SORT_SUFFIX = "_AREA_SORT";

    private static final String LOG_PREFIX = "WuZheTianOriginDataItemQueryExtPt-";

    private static final Long APP_ID = 21431L;

    @Autowired
    RecommendSpi recommendSpi;

    @Autowired
    private SgExtensionExecutor sgExtensionExecutor;

    @Override
    public Flowable<OriginDataDTO<ItemEntity>> process(SgFrameworkContextItem context) {
        DataContext dataContext = new DataContext();
        //csa默认只为了区分大区，如有其它作用请检查
        String csa = MapUtil.getStringWithDefault(context.getRequestParams(), "csa",
                "13278278282_0_38.066124.114.465406_0_0_0_130105_107_0_0_0_130105007_0");
        Long smAreaId = this.getSmAreaId(AddressUtil.parseCSA(csa).getRegionCode());
        Long userId = MapUtil.getLongWithDefault(context.getRequestParams(), "userId", 0L);
        Long index = MapUtil.getLongWithDefault(context.getRequestParams(), "index", 1L);
        Long pageSize = MapUtil.getLongWithDefault(context.getRequestParams(), "pageSize", 20L);
        dataContext.setIndex(index);
        dataContext.setPageSize(pageSize);
//        OriginDataDTO<ItemEntity> cacheOriginDataDTO = getItemToCacheOfArea(smAreaId);
//        if (cacheOriginDataDTO == null) {
        //tair获取推荐商品
        List<Long> tairItems = this.getOriginalRecommend(smAreaId);
        dataContext.setItems(tairItems);
        return recommendSpi.recommendItem(this.buildRecommendRequestParam(userId, tairItems))
                .map(recommendResponseEntityResponse -> {
                    if (!recommendResponseEntityResponse.isSuccess()
                            || recommendResponseEntityResponse.getValue() == null
                            || CollectionUtils.isEmpty(recommendResponseEntityResponse.getValue().getResult())) {
                        tacLogger.info("tpp个性化排序返回异常：" + JSON.toJSONString(recommendResponseEntityResponse));
                        return new OriginDataDTO<>();
                    }
                    OriginDataDTO<ItemEntity> originDataDTO = convert(recommendResponseEntityResponse.getValue());
                    originDataDTO = this.testBuildOriginDataDTO(tairItems);
                    tacLogger.info("原始数据originDataDTO：" + JSON.toJSONString(originDataDTO));
                    //this.setItemToCacheOfArea(originDataDTO, smAreaId);
                    return this.getItemPage(originDataDTO, dataContext);
                });
//        } else {
//            return Flowable.just(this.getItemPage(cacheOriginDataDTO, dataContext));
//        }
    }

    private OriginDataDTO<ItemEntity> testBuildOriginDataDTO(List<Long> itemIds) {
        OriginDataDTO<ItemEntity> originDataDTO = new OriginDataDTO<>();
        List<ItemEntity> itemEntities = itemIds.stream().map(itemId -> {
            ItemEntity itemEntity = new ItemEntity();
            itemEntity.setItemId(itemId);
            return itemEntity;
        }).collect(Collectors.toList());
        originDataDTO.setResult(itemEntities);
        return originDataDTO;
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
        recommendRequest.setAppId(APP_ID);
        Map<String, String> params = Maps.newHashMap();
        params.put("userItemIdList", Joiner.on(",").join(itemIds));
        recommendRequest.setParams(params);
        tacLogger.info("tpp入参recommendRequest：" + JSON.toJSONString(recommendRequest));
        return recommendRequest;
    }

    private List<Long> getOriginalRecommend(Long smAreaId) {
        Long stickMax = 10000L;
        List<Long> items = null;
        List<PmtRuleDataItemRuleDTO> pmtRuleDataItemRuleDTOS = this.getTairItems(smAreaId);
        if (CollectionUtils.isEmpty(pmtRuleDataItemRuleDTOS)) {
            tacLogger.info(LOG_PREFIX + "getOriginalRecommend获取tair原始数据为空，请检查tair数据源配置");
            return Lists.newArrayList();
        } else {
            try {
                PmtRuleDataItemRuleDTO pmtRuleDataItemRuleDTO = JSON.parseObject(
                        JSON.toJSON(pmtRuleDataItemRuleDTOS.get(0)).toString(), PmtRuleDataItemRuleDTO.class);
                List<ColumnCenterDataSetItemRuleDTO> columnCenterDataSetItemRuleDTOS = pmtRuleDataItemRuleDTO.getDataSetItemRuleDTOList();
                columnCenterDataSetItemRuleDTOS.forEach(item -> {
                    if (item.getDataRule().getStick() != null) {
                        item.setIndex(item.getDataRule().getStick());
                    } else {
                        item.setIndex(stickMax);
                    }
                });
                List<ColumnCenterDataSetItemRuleDTO> columnCenterDataSetItemRuleDTOSSort = columnCenterDataSetItemRuleDTOS.stream().sorted(Comparator.comparing(ColumnCenterDataSetItemRuleDTO::getIndex)).collect(
                        Collectors.toList());
                items = columnCenterDataSetItemRuleDTOSSort.stream().map(
                        ColumnCenterDataSetItemRuleDTO::getItemId).collect(Collectors.toList());
                return items;
            } catch (Exception e) {
                tacLogger.error(LOG_PREFIX + "getOriginalRecommend获取tair原始items异常：" + JSON.toJSONString(items), e);
            }
        }
        return items;
    }

    private OriginDataDTO<ItemEntity> getItemPage(OriginDataDTO<ItemEntity> originDataDTO, DataContext dataContext) {
        List<ItemEntity> itemEntities = this.getPage(originDataDTO.getResult(), dataContext.getIndex(),
                dataContext.getPageSize());
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

    private List<PmtRuleDataItemRuleDTO> sortTairItems(List<PmtRuleDataItemRuleDTO> pmtRuleDataItemRuleDTOS) {
        return pmtRuleDataItemRuleDTOS;
    }

    private List<PmtRuleDataItemRuleDTO> getTairItems(Long smAreaId) {
        LogicalArea logicalArea = LogicalArea.ofCoreCityCode(smAreaId);
        if (logicalArea == null) {
            tacLogger.warn(LOG_PREFIX + "getTairItems大区id未匹配：smAreaId：" + smAreaId);
            return Lists.newArrayList();
        }
        String cacheKey = logicalArea.getCacheKey();
        if (!RpmContants.enviroment.isOnline()) {
            cacheKey = cacheKey + "_pre";
        }
        try {
            return (List<PmtRuleDataItemRuleDTO>) tairUtil.getCache(cacheKey);
        } catch (Exception e) {
            tacLogger.error(LOG_PREFIX + "getTairItems数据转换异常：smAreaId：" + smAreaId, e);
        }
        return Lists.newArrayList();
    }

    /**
     * 缓存个性化排序后的商品信息，区分大区
     *
     * @return
     */
    private boolean setItemToCacheOfArea(OriginDataDTO<ItemEntity> originDataDTO, Long smAreaId) {
        LogicalArea logicalArea = LogicalArea.ofCoreCityCode(smAreaId);
        if (logicalArea == null) {
            tacLogger.warn(LOG_PREFIX + "setItemToCacheOfArea大区id未匹配：smAreaId：" + smAreaId);
            return false;
        }
        return tairUtil.setCache(originDataDTO,
                logicalArea.getCacheKey() + AREA_SORT_SUFFIX);
    }

    /**
     * 区分大区缓存获取推荐信息
     *
     * @param smAreaId
     * @return
     */
    private OriginDataDTO<ItemEntity> getItemToCacheOfArea(Long smAreaId) {
        LogicalArea logicalArea = LogicalArea.ofCoreCityCode(smAreaId);
        if (logicalArea == null) {
            tacLogger.warn(LOG_PREFIX + "getItemToCacheOfArea大区id未匹配：smAreaId：" + smAreaId);
            return null;
        }
        Object o = tairUtil.getCache(logicalArea.getCacheKey() + AREA_SORT_SUFFIX);
        if (o == null) {
            return null;
        }
        return JSON.parseObject((String) o, new TypeReference<OriginDataDTO<ItemEntity>>() {
        });
    }

    /**
     * 手动分页
     *
     * @param originalList 分页前数据
     * @param index        页码
     * @param pageSize     每页数量
     * @return 分页后结果
     */
    public <T> List<T> getPage(List<T> originalList, Long index, Long pageSize) {
        if (index < 1) {
            index = 1L;
        }
        //第一页，每页数据大于总数据时全部返回
        if (index == 1 && pageSize > originalList.size()) {
            return originalList;
        }
        if (index * pageSize > originalList.size()) {
            tacLogger.warn("getPage中页数小于获取页数据条数，总页数；" + originalList.size());
            return Lists.newArrayList();
        }
        // 分页后的结果
        List<T> resultList = new ArrayList<>();
        // 如果需要进行分页
        if (pageSize > 0) {
            // 获取起点
            long pageStart = (index - 1) * pageSize;
            // 获取终点
            long pageStop = pageStart + pageSize;
            // 开始遍历
            while (pageStart < pageStop) {
                // 考虑到最后一页可能不够pageSize
                if (pageStart == originalList.size()) {
                    break;
                }
                resultList.add(originalList.get(Math.toIntExact(pageStart++)));
            }
        }
        // 如果不进行分页，显示所有数据
        else {
            resultList = originalList;
        }
        return resultList;
    }

    /**
     * 根据regionCode获取coreCityCode,华东打底
     *
     * @param regionCode
     * @return
     */
    private Long getSmAreaId(String regionCode) {
        LogicalArea logicalArea = LogicalArea.ofCode(regionCode);
        if (logicalArea == null) {
            return LogicalArea.HD.getCoreCityCode();
        } else {
            return logicalArea.getCoreCityCode();
        }
    }

}
