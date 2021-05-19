package com.tmall.wireless.tac.biz.processor.wzt.utils;

import java.util.List;

import javax.annotation.Resource;

import com.alibaba.fastjson.JSON;

import com.google.common.collect.Lists;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.taobao.tair.ResultCode;
import com.taobao.tair.impl.mc.MultiClusterTairManager;
import com.tmall.txcs.gs.spi.recommend.TairFactorySpi;
import com.tmall.txcs.gs.spi.recommend.TairManager;
import com.tmall.wireless.tac.biz.processor.wzt.model.PmtRuleDataItemRuleDTO;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: luoJunChong
 * @Date: 2021/5/17 15:48
 */
@Component
public class TairUtil {
    @Resource
    private MultiClusterTairManager multiClusterTairManager;

    @Autowired
    TairFactorySpi tairFactorySpi;

    @Autowired
    TacLogger tacLogger;

    public static int NAME_SPACE = 184;

    private static final String LOG_PREFIX = "TairUtil-";

    /**
     * 通过get方法获取key值对应的缓存
     */
    public Object queryPromotionFromCache(String cacheKey) {
        try {
            //调用get方法获取key值对应的缓存
            Result<DataEntry> result = multiClusterTairManager.get(NAME_SPACE, cacheKey);
            tacLogger.info("缓存原始数据" + cacheKey + JSON.toJSONString(result));
            if (null == result || !result.isSuccess()
                || ResultCode.DATANOTEXSITS.equals(result.getRc())
                || null == result.getValue()
                || null == result.getValue().getValue()) {
                tacLogger.info("缓存数据为空");
                return null;
            }
            tacLogger.info("tair缓存取出" + cacheKey + String.valueOf(result.getValue().getValue()));
            return result.getValue().getValue();
        } catch (Exception e) {
            tacLogger.error(
                LOG_PREFIX + "queryPromotionFromCache 缓存获取失败，cacheKey：" + cacheKey, e);
            return null;
        }
    }

    //public Boolean updateItemDetailPromotionCache(Object data,
    //    String cacheKey) {
    //    ResultCode resultCode = multiClusterTairManager.put(NAME_SPACE, cacheKey,
    //        JSON.toJSONString(data), 0, 60 * 30);
    //    if (resultCode == null || !resultCode.isSuccess()) {
    //        tacLogger.info(LOG_PREFIX + "updateItemDetailPromotionCache 缓存失败，cacheKey：" + cacheKey);
    //        return false;
    //    }
    //    return true;
    //}

    public List<PmtRuleDataItemRuleDTO> getCache(String cacheKey) {
        List<PmtRuleDataItemRuleDTO> pmtRuleDataItemRuleDTOS = Lists.newArrayList();
        try {
            TairManager defaultTair = tairFactorySpi.getDefaultTair();
            if (defaultTair == null || defaultTair.getMultiClusterTairManager() == null) {
                tacLogger.warn(
                    LOG_PREFIX + "缓存异常，cacheKey: " + cacheKey);
                return null;
            }
            Result<DataEntry> dataEntryResult = defaultTair.getMultiClusterTairManager().get(NAME_SPACE,
                cacheKey);
            if (dataEntryResult.isSuccess()) {
                pmtRuleDataItemRuleDTOS = (List<PmtRuleDataItemRuleDTO>)dataEntryResult.getValue().getValue();
            } else {
                tacLogger.info(LOG_PREFIX + "获取缓存失败，cacheKey: " + cacheKey);
            }
            return pmtRuleDataItemRuleDTOS;
        } catch (Exception e) {
            tacLogger.error(LOG_PREFIX + "getCache获取缓存失败,cacheKey:" + cacheKey, e);
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
}

