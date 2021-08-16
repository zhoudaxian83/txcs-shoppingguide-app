package com.tmall.wireless.tac.biz.processor.wzt.utils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import com.google.common.collect.Lists;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.taobao.tair.ResultCode;
import com.tmall.aselfmanager.client.columncenter.response.PmtRuleDataItemRuleDTO;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.model.constant.RpmContants;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.txcs.gs.spi.recommend.TairFactorySpi;
import com.tmall.txcs.gs.spi.recommend.TairManager;
import com.tmall.wireless.tac.biz.processor.common.VoKeyConstantApp;
import com.tmall.wireless.tac.biz.processor.todaycrazy.utils.MapUtil;
import com.tmall.wireless.tac.biz.processor.todaycrazy.utils.TodayCrazyUtils;
import com.tmall.wireless.tac.biz.processor.wzt.constant.Constant;
import com.tmall.wireless.tac.biz.processor.wzt.enums.LogicalArea;
import com.tmall.wireless.tac.biz.processor.wzt.model.ColumnCenterDataSetItemRuleDTO;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: luoJunChong
 * @Date: 2021/5/17 15:48
 */
@Component
public class TairUtil {

    @Autowired
    TairFactorySpi tairFactorySpi;

    @Autowired
    TacLogger tacLogger;

    public static int NAME_SPACE = 184;

    private static final String LOG_PREFIX = "TairUtil-";

    /**
     * 分大区个性化排序后商品缓存后缀
     */
    private static final String AREA_SORT_SUFFIX = "_AREA_SORT";

    public Object getCache(String cacheKey) {
        try {
            TairManager defaultTair = tairFactorySpi.getDefaultTair();
            if (defaultTair == null || defaultTair.getMultiClusterTairManager() == null) {
                tacLogger.warn(
                        LOG_PREFIX + "缓存异常，cacheKey: " + cacheKey);
                return null;
            }
            Result<DataEntry> dataEntryResult = defaultTair.getMultiClusterTairManager().get(NAME_SPACE,
                    cacheKey);
            if (dataEntryResult.isSuccess() && dataEntryResult.getValue() != null
                    && dataEntryResult.getValue().getValue() != null) {
                return dataEntryResult.getValue().getValue();
            } else {
                tacLogger.info(LOG_PREFIX + "getCache获取缓存为空，cacheKey: " + cacheKey);
                return null;
            }
        } catch (Exception e) {
            tacLogger.error(LOG_PREFIX + "getCache获取缓存异常,cacheKey:" + cacheKey, e);
        }
        return null;
    }

    public Boolean setCache(Object data, String cacheKey) {
        try {
            TairManager defaultTair = tairFactorySpi.getDefaultTair();
            if (defaultTair == null || defaultTair.getMultiClusterTairManager() == null) {
                tacLogger.warn(LOG_PREFIX + "缓存异常， cacheKey: " + cacheKey);
                return false;
            }
            ResultCode resultCode = defaultTair.getMultiClusterTairManager().put(NAME_SPACE,
                    cacheKey, JSON.toJSONString(data), 0, 60 * 30);
            if (resultCode.isSuccess()) {
                return true;
            } else {
                tacLogger.info(LOG_PREFIX + "setCache缓存失败，cacheKey: " + cacheKey);
            }
            return true;
        } catch (Exception e) {
            tacLogger.error(LOG_PREFIX + "setCache缓存异常,cacheKey:" + cacheKey, e);
        }
        return false;
    }

    /**
     * 获取原始tair并做排序
     *
     * @param smAreaId
     * @return
     */
    public List<ColumnCenterDataSetItemRuleDTO> getOriginalRecommend(Long smAreaId) {
        List<PmtRuleDataItemRuleDTO> pmtRuleDataItemRuleDTOS = getCachePmtRuleDataItemRuleDTOList(smAreaId);
        tacLogger.info("验证定投数据_排序前：" + JSON.toJSONString(pmtRuleDataItemRuleDTOS));
        if (com.ali.unit.rule.util.lang.CollectionUtils.isEmpty(pmtRuleDataItemRuleDTOS)) {
            tacLogger.info(LOG_PREFIX + "getOriginalRecommend获取tair原始数据为空，请检查tair数据源配置");
            return Lists.newArrayList();
        } else {
            try {
                com.tmall.wireless.tac.biz.processor.wzt.model.PmtRuleDataItemRuleDTO pmtRuleDataItemRuleDTO = JSON
                        .parseObject(
                                JSON.toJSON(pmtRuleDataItemRuleDTOS.get(0)).toString(),
                                com.tmall.wireless.tac.biz.processor.wzt.model.PmtRuleDataItemRuleDTO.class);
                List<ColumnCenterDataSetItemRuleDTO> columnCenterDataSetItemRuleDTOS = pmtRuleDataItemRuleDTO
                        .getDataSetItemRuleDTOList();
                columnCenterDataSetItemRuleDTOS.forEach(item -> {
                    if (item.getDataRule().getStick() != null) {
                        item.setIndex(item.getDataRule().getStick());
                    } else {
                        item.setIndex(Constant.INDEX);
                    }
                });
//                return columnCenterDataSetItemRuleDTOS.stream().sorted(
//                    Comparator.comparing(ColumnCenterDataSetItemRuleDTO::getIndex)).collect(
//                    Collectors.toList());
                return columnCenterDataSetItemRuleDTOS;
            } catch (Exception e) {
                tacLogger.error(LOG_PREFIX + "getOriginalRecommend获取tair原始items异常", e);
            }
        }
        return Lists.newArrayList();
    }

    private List<PmtRuleDataItemRuleDTO> getCachePmtRuleDataItemRuleDTOList(Long smAreaId) {
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
            return (List<PmtRuleDataItemRuleDTO>) getCache(cacheKey);
        } catch (Exception e) {
            tacLogger.error(LOG_PREFIX + "getTairItems数据转换异常：smAreaId：" + smAreaId, e);
        }
        return Lists.newArrayList();
    }

    public String getChannelKey(Long smAreaId) {
        List<PmtRuleDataItemRuleDTO> pmtRuleDataItemRuleDTOList = getCachePmtRuleDataItemRuleDTOList(smAreaId);
        if (CollectionUtils.isNotEmpty(pmtRuleDataItemRuleDTOList) && pmtRuleDataItemRuleDTOList.get(0)
                .getPmtRuleDataSetDTO() != null) {
            String promotionExtension = pmtRuleDataItemRuleDTOList.get(0).getPmtRuleDataSetDTO().getExtension();
            Map<String, Object> extensionMap = TodayCrazyUtils.parseExtension(promotionExtension, "\\|", "\\=", true);
            String channelKey = MapUtil.getStringWithDefault(extensionMap, "channelKey", VoKeyConstantApp.CHANNEL_KEY);
            return channelKey;
        }
        return VoKeyConstantApp.CHANNEL_KEY;
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
        Object o = getCache(logicalArea.getCacheKey() + AREA_SORT_SUFFIX);
        if (o == null) {
            return null;
        }
        return JSON.parseObject((String) o, new TypeReference<OriginDataDTO<ItemEntity>>() {
        });
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
        return setCache(originDataDTO,
                logicalArea.getCacheKey() + AREA_SORT_SUFFIX);
    }

}

