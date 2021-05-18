package com.tmall.wireless.tac.biz.processor.wzt.utils;

import javax.annotation.Resource;

import com.alibaba.fastjson.JSON;

import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.taobao.tair.ResultCode;
import com.taobao.tair.impl.mc.MultiClusterTairManager;
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
    TacLogger tacLogger;

    public static int NAME_SPACE = 184;

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
                "[queryPromotionFromCache] get item promotion from cache failed, itemId: " + cacheKey, e);
            return null;
        }
    }

    /**
     * 调用put方法将ItemPromotionDTO对象存到NAME_SPACE下，key值为cacheKey
     */
    public Boolean updateItemDetailPromotionCache(Object data,
        String cacheKey) {
        ResultCode resultCode = multiClusterTairManager.put(NAME_SPACE, cacheKey,
            JSON.toJSONString(data), 0, 60 * 30);
        if (resultCode == null || !resultCode.isSuccess()) {
            tacLogger.info("[updateItemDetailPromotionCache]Failed to update item detail promotion cache, cacheKey: "
                + cacheKey);
            return false;
        }
        return true;
    }
}
