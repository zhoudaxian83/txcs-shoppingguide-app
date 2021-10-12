package com.tmall.wireless.tac.biz.processor.extremeItem.common.service;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.alibaba.fastjson.JSON;
import com.taobao.eagleeye.EagleEye;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.taobao.tair.ResultCode;
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
import java.util.concurrent.atomic.AtomicLong;

@Service
public class SupermarketHallBottomService {
    private static Logger logger = LoggerProxy.getLogger(SupermarketHallIGraphSearchServiceImpl.class);

    private static final AtomicLong bottomCounter = new AtomicLong(0);

    @Autowired
    TairFactorySpi tairFactorySpi;

    public void writeBottomData(String resourceId, List<GeneralItem> generalItemList) {
        if(!satisfyBottom()) {
            return;
        }
        String cacheKey = getCacheKey(resourceId);
        try {
            //defaultTair的namespace是184
            TairManager defaultTair = tairFactorySpi.getDefaultTair();
            if (defaultTair == null || defaultTair.getMultiClusterTairManager() == null) {
                logger.error("tairFactorySpi.getDefaultTair fail, traceId:" + EagleEye.getTraceId());
                return;
            }
            ResultCode resultCode = defaultTair.getMultiClusterTairManager().put(defaultTair.getNameSpace(), cacheKey, JSON.toJSONString(generalItemList), 0, 60 * 60 * 24);
            if (!resultCode.isSuccess()) {
                logger.error("writeBottomData失败，traceId:" + EagleEye.getTraceId() + "," + "cacheKey: " + cacheKey);
            }
        } catch (Exception e) {
            logger.error("writeBottomData异常，traceId:" + EagleEye.getTraceId() + "," + "cacheKey: " + cacheKey, e);
        }
    }

    public List<GeneralItem> readBottomData(String resourceId) {
        if(StringUtils.isBlank(resourceId)) {
            return new ArrayList<>();
        }
        String cacheKey = getCacheKey(resourceId);
        try {
            TairManager defaultTair = tairFactorySpi.getDefaultTair();
            if (defaultTair == null || defaultTair.getMultiClusterTairManager() == null) {
                logger.error("tairFactorySpi.getDefaultTair fail, traceId:" + EagleEye.getTraceId());
                return new ArrayList<>();
            }
            Result<DataEntry> dataEntryResult = defaultTair.getMultiClusterTairManager().get(defaultTair.getNameSpace(), cacheKey);
            if (dataEntryResult.isSuccess() && dataEntryResult.getValue() != null
                    && dataEntryResult.getValue().getValue() != null) {
                return JSON.parseArray(String.valueOf(dataEntryResult.getValue().getValue()), GeneralItem.class);
            } else {
                logger.error( "readBottomData失败，traceId:" + EagleEye.getTraceId() + "," + "cacheKey: " + cacheKey);
                return new ArrayList<>();
            }
        } catch (Exception e) {
            logger.error("readBottomData异常，traceId:" + EagleEye.getTraceId() + "," + "cacheKey: " + cacheKey, e);
        }
        return new ArrayList<>();
    }

    private String getCacheKey(String resourceId, String areaId) {
        if(!RpmContants.enviroment.isOnline()) {
            return "pre_hall_bottom_" + resourceId + "_" + areaId;
        }
        return "hall_bottom_" + resourceId + "_" + areaId;
    }

    private String getCacheKey(String resourceId) {
        if(!RpmContants.enviroment.isOnline()) {
            return "pre_hall_bottom_" + resourceId;
        }
        return "hall_bottom_" + resourceId;
    }

    public boolean satisfyBottom() {
        long count = bottomCounter.getAndAdd(1);
        return count % SupermarketHallSwitch.bottomCounterCycle == 0;
    }

}
