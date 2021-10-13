package com.tmall.wireless.tac.biz.processor.extremeItem.common.service;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.alibaba.fastjson.JSON;
import com.taobao.eagleeye.EagleEye;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.taobao.tair.ResultCode;
import com.tmall.aselfcaptain.util.StackTraceUtil;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.txcs.gs.model.constant.RpmContants;
import com.tmall.txcs.gs.spi.recommend.TairFactorySpi;
import com.tmall.txcs.gs.spi.recommend.TairManager;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.config.SupermarketHallSwitch;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.service.impl.SupermarketHallIGraphSearchServiceImpl;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.util.Logger;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.util.LoggerProxy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class SupermarketHallBottomService {
    private static Logger logger = LoggerProxy.getLogger(SupermarketHallIGraphSearchServiceImpl.class);

    private static final Map<String, AtomicLong> bottomCounterMap = new ConcurrentHashMap<>();

    @Autowired
    TairFactorySpi tairFactorySpi;

    public void writeBottomData(String resourceId, String scheduleId, List<GeneralItem> generalItemList) {
        if(!satisfyBottom(resourceId, scheduleId)) {
            return;
        }
        logger.info("traceId:" + EagleEye.getTraceId() + ", bottomCounterMap:" + JSON.toJSONString(bottomCounterMap));
        Long writeBottomDataStart = System.currentTimeMillis();
        String cacheKey = getCacheKey(resourceId, scheduleId);
        try {
            //defaultTair的namespace是184
            TairManager defaultTair = tairFactorySpi.getDefaultTair();
            if (defaultTair == null || defaultTair.getMultiClusterTairManager() == null) {
                logger.error("tairFactorySpi.getDefaultTair fail, traceId:" + EagleEye.getTraceId());
                return;
            }
            ResultCode resultCode = defaultTair.getMultiClusterTairManager().put(defaultTair.getNameSpace(), cacheKey, JSON.toJSONString(generalItemList), 0, 60 * 60 * 24);
            if (!resultCode.isSuccess()) {
                HadesLogUtil.stream("ExtremeItemSdkItemHandler|writeBottomData|" + Logger.isEagleEyeTest() + "|error")
                        .kv("cacheKey", cacheKey)
                        .error();
                logger.error("writeBottomData失败，traceId:" + EagleEye.getTraceId() + "," + "cacheKey: " + cacheKey);
            } else {
                Long writeBottomDataEnd = System.currentTimeMillis();
                HadesLogUtil.stream("ExtremeItemSdkItemHandler|writeBottomData|" + Logger.isEagleEyeTest() + "|success|" + (writeBottomDataEnd - writeBottomDataStart))
                        .kv("cacheKey", cacheKey)
                        .error();
            }
        } catch (Exception e) {
            HadesLogUtil.stream("ExtremeItemSdkItemHandler|writeBottomData|" + Logger.isEagleEyeTest() + "|exception")
                    .kv("cacheKey", cacheKey)
                    .error();
            logger.error("writeBottomData异常，traceId:" + EagleEye.getTraceId() + "," + "cacheKey: " + cacheKey, e);
        }
    }

    public List<GeneralItem> readBottomData(String resourceId, String scheduleId) {
        if(StringUtils.isBlank(resourceId) || StringUtils.isBlank(scheduleId)) {
            return new ArrayList<>();
        }
        Long readBottomDataStart = System.currentTimeMillis();
        String cacheKey = getCacheKey(resourceId, scheduleId);
        try {
            TairManager defaultTair = tairFactorySpi.getDefaultTair();
            if (defaultTair == null || defaultTair.getMultiClusterTairManager() == null) {
                HadesLogUtil.stream("ExtremeItemSdkItemHandler|readBottomData|" + Logger.isEagleEyeTest() + "|error")
                        .kv("cacheKey", cacheKey)
                        .error();
                logger.error("tairFactorySpi.getDefaultTair fail, traceId:" + EagleEye.getTraceId());
                return new ArrayList<>();
            }
            Result<DataEntry> dataEntryResult = defaultTair.getMultiClusterTairManager().get(defaultTair.getNameSpace(), cacheKey);
            if (dataEntryResult.isSuccess() && dataEntryResult.getValue() != null
                    && dataEntryResult.getValue().getValue() != null) {
                Long readBottomDataEnd = System.currentTimeMillis();
                HadesLogUtil.stream("ExtremeItemSdkItemHandler|readBottomData|" + Logger.isEagleEyeTest() + "|success|" + (readBottomDataEnd - readBottomDataStart))
                        .kv("cacheKey", cacheKey)
                        .error();
                return JSON.parseArray(String.valueOf(dataEntryResult.getValue().getValue()), GeneralItem.class);
            } else {
                HadesLogUtil.stream("ExtremeItemSdkItemHandler|readBottomData|" + Logger.isEagleEyeTest() + "|error")
                        .kv("cacheKey", cacheKey)
                        .error();
                logger.error( "readBottomData失败，traceId:" + EagleEye.getTraceId() + "," + "cacheKey: " + cacheKey);
                return new ArrayList<>();
            }
        } catch (Exception e) {
            HadesLogUtil.stream("ExtremeItemSdkItemHandler|readBottomData|" + Logger.isEagleEyeTest() + "|exception")
                    .kv("cacheKey", cacheKey)
                    .error();
            logger.error("readBottomData异常，traceId:" + EagleEye.getTraceId() + "," + "cacheKey: " + cacheKey, e);
        }
        return new ArrayList<>();
    }

    private String getCacheKey(String resourceId, String scheduleId) {
        if(!RpmContants.enviroment.isOnline()) {
            return "pre_hall_bottom_" + resourceId + "_" + scheduleId;
        }
        return "hall_bottom_" + resourceId + "_" + scheduleId;
    }

    private String getCacheKey(String resourceId) {
        if(!RpmContants.enviroment.isOnline()) {
            return "pre_hall_bottom_" + resourceId;
        }
        return "hall_bottom_" + resourceId;
    }

    public boolean satisfyBottom(String resourceId, String scheduleId) {
        AtomicLong counter = bottomCounterMap.computeIfAbsent(resourceId + "_" + scheduleId, v -> new AtomicLong(0));
        return counter.getAndAdd(1) % SupermarketHallSwitch.bottomCounterCycle == 0;
    }

}
