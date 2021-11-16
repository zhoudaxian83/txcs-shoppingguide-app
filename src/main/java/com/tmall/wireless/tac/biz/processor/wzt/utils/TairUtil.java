package com.tmall.wireless.tac.biz.processor.wzt.utils;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.taobao.tair.ResultCode;
import com.tmall.aselfmanager.client.columncenter.response.ColumnCenterDataRuleDTO;
import com.tmall.aselfmanager.client.columncenter.response.ColumnCenterDataSetItemRuleDTO;
import com.tmall.aselfmanager.client.columncenter.response.ColumnCenterPmtRuleDataSetDTO;
import com.tmall.aselfmanager.client.columncenter.response.PmtRuleDataItemRuleDTO;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.txcs.gs.spi.recommend.TairFactorySpi;
import com.tmall.txcs.gs.spi.recommend.TairManager;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.common.VoKeyConstantApp;
import com.tmall.wireless.tac.biz.processor.todaycrazy.utils.MapUtil;
import com.tmall.wireless.tac.biz.processor.todaycrazy.utils.TodayCrazyUtils;
import com.tmall.wireless.tac.biz.processor.wzt.constant.Constant;
import com.tmall.wireless.tac.biz.processor.wzt.enums.LogicalArea;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
        //cacheKey = "channelPriceNew_XN";
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
            if (Constant.DEBUG) {
                tacLogger.info("Tair原始数据key：" + cacheKey + ";JSON:" + JSON.toJSONString(dataEntryResult.getValue().getValue()));
            }
            return dataEntryResult.getValue().getValue();
        } else {
            tacLogger.info(LOG_PREFIX + "getCache获取缓存为空，cacheKey: " + cacheKey);
            return null;
        }
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
     * 获取原始tair
     *
     * @param pmtRuleDataItemRuleDTO
     * @return
     */
    public List<ColumnCenterDataSetItemRuleDTO> getOriginalRecommend(PmtRuleDataItemRuleDTO pmtRuleDataItemRuleDTO) {
        if (pmtRuleDataItemRuleDTO == null) {
            return Lists.newArrayList();
        }
        List<ColumnCenterDataSetItemRuleDTO> dataSetItemRuleDTOList = pmtRuleDataItemRuleDTO.getDataSetItemRuleDTOList();
        if (CollectionUtils.isEmpty(dataSetItemRuleDTOList)) {
            return Lists.newArrayList();
        }
        /**
         * 定坑排序打标转换
         */
        dataSetItemRuleDTOList.forEach(columnCenterDataSetItemRuleDTO -> {
            ColumnCenterDataRuleDTO columnCenterDataRuleDTO = columnCenterDataSetItemRuleDTO.getDataRule();
            Long stick = columnCenterDataRuleDTO.getStick();
            Date itemStickStartDate = columnCenterDataRuleDTO.getItemStickStartTime();
            Date itemStickEndDate = columnCenterDataRuleDTO.getItemStickEndTime();
            /**
             * 定坑商品如果定坑时间不能满足当前时间，则置为非定坑商品
             */
            if (stick != null && itemStickStartDate != null && itemStickEndDate != null) {
                long nowTime = System.currentTimeMillis();
                long itemStickStartTime = itemStickStartDate.getTime();
                long itemStickEndTime = itemStickEndDate.getTime();
                boolean stickTime = itemStickStartTime < nowTime && itemStickEndTime > nowTime;
                if (stickTime) {
                    columnCenterDataSetItemRuleDTO.getDataRule().setStick(stick);
                } else {
                    columnCenterDataSetItemRuleDTO.getDataRule().setStick(Constant.INDEX);
                }
            } else {
                columnCenterDataSetItemRuleDTO.getDataRule().setStick(Constant.INDEX);
            }
        });
        return dataSetItemRuleDTOList;
    }


    public List<PmtRuleDataItemRuleDTO> getCachePmtRuleDataItemRuleDTOList(Long smAreaId) {
        LogicalArea logicalArea = LogicalArea.ofCoreCityCode(smAreaId);
        if (logicalArea == null) {
            HadesLogUtil.stream(ScenarioConstantApp.WU_ZHE_TIAN)
                    .kv("method:", "getCachePmtRuleDataItemRuleDTOList")
                    .kv("errorMessage", "smAreaId is null")
                    .info();
            return Lists.newArrayList();
        }
        String cacheKey = logicalArea.getCacheKey();
//        if (!RpmContants.enviroment.isOnline()) {
//            cacheKey = cacheKey + "_pre";
//        }
        try {
            return (List<PmtRuleDataItemRuleDTO>) getCache(cacheKey);
        } catch (Exception e) {
            tacLogger.error(LOG_PREFIX + "getTairItems数据转换异常：smAreaId：" + smAreaId, e);
        }
        return Lists.newArrayList();
    }

    /**
     * 获取指定的活动
     *
     * @param smAreaId
     * @return
     */
    public PmtRuleDataItemRuleDTO getPmtRuleDataItemRuleDTO(Long smAreaId) {
        /**
         * 获取全部活动商品
         */
        List<PmtRuleDataItemRuleDTO> pmtRuleDataItemRuleDTOList = this.getCachePmtRuleDataItemRuleDTOList(smAreaId);
        if (Constant.DEBUG) {
            tacLogger.info("不同活动过滤前：" + JSON.toJSONString(pmtRuleDataItemRuleDTOList));
        }
        if (CollectionUtils.isEmpty(pmtRuleDataItemRuleDTOList)) {
            HadesLogUtil.stream(ScenarioConstantApp.WU_ZHE_TIAN)
                    .kv("method:", "getPmtRuleDataItemRuleDTO")
                    .kv("errorMessage1", "pmtRuleDataItemRuleDTOList is null")
                    .info();
            return null;
        }
        /**
         * 去除当前时间不在排期时间内的
         */
        pmtRuleDataItemRuleDTOList.removeIf(pmtRuleDataItemRuleDTO -> !this.inUse(pmtRuleDataItemRuleDTO.getPmtRuleDataSetDTO()));
        if (CollectionUtils.isEmpty(pmtRuleDataItemRuleDTOList)) {
            HadesLogUtil.stream(ScenarioConstantApp.WU_ZHE_TIAN)
                    .kv("method:", "getPmtRuleDataItemRuleDTO")
                    .kv("errorMessage2", "pmtRuleDataItemRuleDTOList is null")
                    .info();
            return null;
        }
        return pmtRuleDataItemRuleDTOList.get(0);
    }

    /**
     * 是否在用，
     * 当前时间必须在排期时间段内
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

    public String getChannelKeyV2(PmtRuleDataItemRuleDTO pmtRuleDataItemRuleDTO) {
        if (pmtRuleDataItemRuleDTO != null && pmtRuleDataItemRuleDTO.getPmtRuleDataSetDTO() != null) {
            String promotionExtension = pmtRuleDataItemRuleDTO.getPmtRuleDataSetDTO().getExtension();
            Map<String, Object> extensionMap = TodayCrazyUtils.parseExtension(promotionExtension, "\\|", "\\=", true);
            return MapUtil.getStringWithDefault(extensionMap, Constant.CHANNEL_KEY, VoKeyConstantApp.CHANNEL_KEY);
        }
        return VoKeyConstantApp.CHANNEL_KEY;
    }

}

