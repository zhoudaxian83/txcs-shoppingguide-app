package com.tmall.wireless.tac.biz.processor.processtemplate.common.service;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.alibaba.fastjson.JSON;
import com.taobao.eagleeye.EagleEye;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.taobao.tair.ResultCode;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.txcs.gs.spi.factory.CommonFactoryAbs;
import com.tmall.txcs.gs.spi.recommend.TairFactorySpi;
import com.tmall.txcs.gs.spi.recommend.TairManager;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.config.SupermarketHallSwitch;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.util.Logger;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.util.LoggerProxy;
import com.tmall.wireless.tac.client.domain.Enviroment;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.tmall.wireless.tac.biz.processor.extremeItem.common.config.SupermarketHallSwitch.openSampleBottom;

@Service
public class ProcessTemplateBottomService {
    private static Logger logger = LoggerProxy.getLogger(ProcessTemplateBottomService.class);

    @Autowired
    TairFactorySpi tairFactorySpi;
    @Autowired
    CommonFactoryAbs commonFactoryAbs;
    @Autowired
    Enviroment enviroment;

    public void writeBottomData(String resourceId, String scheduleId, List<GeneralItem> generalItemList) {
        if(!satisfyBottom(resourceId, scheduleId)) {
            return;
        }
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
                logger.error( "readBottomData失败,traceId:{},cacheKey:{}", EagleEye.getTraceId(), cacheKey);
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
        if(enviroment != null && enviroment.isOnline()) {
            return "hall_bottom_" + resourceId + "_" + scheduleId;
        }
        return "pre_hall_bottom_" + resourceId + "_" + scheduleId;
    }

    private String getCacheKey(String resourceId) {
        if(enviroment != null && enviroment.isOnline()) {
            return "hall_bottom_" + resourceId;
        }
        return "pre_hall_bottom_" + resourceId;
    }

    private String getCounterKey(String resourceId, String scheduleId) {
        if(enviroment != null && enviroment.isOnline()) {
            return "hall_counter_" + resourceId + "_" + scheduleId;
        }
        return "pre_hall_counter_" + resourceId + "_" + scheduleId;
    }

    private String getCounterKey(String resourceId) {
        if(enviroment != null && enviroment.isOnline()) {
            return "hall_counter_" + resourceId;
        }
        return "pre_hall_counter_" + resourceId;
    }

    public boolean satisfyBottom(String resourceId, String scheduleId) {
        if(!openSampleBottom) {
            return false;
        }

        TairManager defaultTair = tairFactorySpi.getDefaultTair();
        if (defaultTair == null || defaultTair.getMultiClusterTairManager() == null) {
            logger.error("tairFactorySpi.getDefaultTair fail, traceId:{}", EagleEye.getTraceId());
            return false;
        }
        Long bottomCountStart = System.currentTimeMillis();
        String counterKey = getCounterKey(resourceId, scheduleId);
        try {
            Result<Integer> counterResult = defaultTair.getMultiClusterTairManager().incr(defaultTair.getNameSpace(), counterKey, 1, -1, 60 * 60 * 24);
            if (counterResult.isSuccess() && counterResult.getValue() != null) {
                Long bottomCountEnd = System.currentTimeMillis();
                HadesLogUtil.stream("ExtremeItemSdkItemHandler|bottomCount|" + Logger.isEagleEyeTest() + "|success|" + (bottomCountEnd - bottomCountStart))
                        .kv("counterKey", counterKey)
                        .kv("counterValue", String.valueOf(counterResult.getValue()))
                        .info();
                boolean satisfy = counterResult.getValue() % SupermarketHallSwitch.bottomCounterCycle == 0;
                if (satisfy) {
                    HadesLogUtil.stream("ExtremeItemSdkItemHandler|bottomCount.hit|" + Logger.isEagleEyeTest() + "|success|" + (bottomCountEnd - bottomCountStart))
                            .kv("counterKey", counterKey)
                            .kv("counterValue", String.valueOf(counterResult.getValue()))
                            .info();
                }
                return satisfy;
            } else {
                HadesLogUtil.stream("ExtremeItemSdkItemHandler|bottomCount|" + Logger.isEagleEyeTest() + "|error")
                        .kv("counterKey", counterKey)
                        .error();
            }
        } catch (Exception e) {
            HadesLogUtil.stream("ExtremeItemSdkItemHandler|bottomCount|" + Logger.isEagleEyeTest() + "|exception")
                    .kv("counterKey", counterKey)
                    .error();
        }
        return false;
    }

}
