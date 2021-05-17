package com.tmall.wireless.tac.biz.processor.wzt.utils;

import javax.annotation.Resource;

import com.alibaba.fastjson.JSON;

import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.taobao.tair.ResultCode;
import com.taobao.tair.impl.mc.MultiClusterTairManager;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
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
    public static String ACHE_NAME_PREFIX = "WUZHETIAN_";

    /**
     * 通过get方法获取key值对应的缓存
     */
    public String queryPromotionFromCache(String cacheKey) {
        try {
            ////约定好的key值
            //String cacheKey = StringUtils.join(
            //    Lists.newArrayList("ITEM", "PROM", "TEST", itemId),
            //    "_");
            //调用get方法获取key值对应的缓存
            Result<DataEntry> result = multiClusterTairManager.get(NAME_SPACE, ACHE_NAME_PREFIX+cacheKey);
            if (null == result || !result.isSuccess()
                || ResultCode.DATANOTEXSITS.equals(result.getRc())
                || null == result.getValue()
                || null == result.getValue().getValue()) {
                return null;
            }
            return String.valueOf(result.getValue().getValue());
        } catch (Exception e) {
            tacLogger.error("[CzmfInfoManage] get item promotion from cache failed, itemId: " + ACHE_NAME_PREFIX+cacheKey, e);
            return null;
        }
    }

    /**
     * 调用put方法将ItemPromotionDTO对象存到NAME_SPACE下，key值为cacheKey
     */
    public Boolean updateItemDetailPromotionCache(OriginDataDTO<ItemEntity> czmfItem,
        String cacheKey) {
        ////约定好的key值
        //String cacheKey = StringUtils.join(
        //    Lists.newArrayList("ITEM", "PROM", "TEST", itemId),
        //    "_");
        //更新超返数据到tair，过期时间2分钟
        ResultCode resultCode = multiClusterTairManager.put(NAME_SPACE, cacheKey,
            JSON.toJSONString(czmfItem), 0, 120);
        if (resultCode == null || !resultCode.isSuccess()) {
            tacLogger.info("[CzmfInfoManage]Failed to update item detail promotion cache, cacheKey: " + ACHE_NAME_PREFIX+cacheKey);
            return false;
        }
        return true;
    }
}
