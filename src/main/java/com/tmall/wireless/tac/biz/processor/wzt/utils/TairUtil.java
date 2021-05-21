package com.tmall.wireless.tac.biz.processor.wzt.utils;

import javax.annotation.Resource;

import com.alibaba.fastjson.JSON;

import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.taobao.tair.ResultCode;
import com.taobao.tair.impl.mc.MultiClusterTairManager;
import com.tmall.txcs.gs.spi.recommend.TairFactorySpi;
import com.tmall.txcs.gs.spi.recommend.TairManager;
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
                tacLogger.warn(
                    LOG_PREFIX + "取缓存key打印，cacheKey: " + cacheKey);
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
}

