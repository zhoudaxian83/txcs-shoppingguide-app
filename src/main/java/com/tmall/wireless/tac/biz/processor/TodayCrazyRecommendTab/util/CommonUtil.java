package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.util;

import com.google.common.collect.Maps;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemEntity;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.constant.CommonConstant;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.util.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommonUtil {

    private static Logger tacLogger;


    public static HashMap<String, String> buildItemIdAndCacheKey(List<ItemEntity> itemEntities) {
        HashMap<String, String> map = Maps.newHashMap();
        itemEntities.forEach(itemEntity -> {
            if (itemEntity.getExtMap().get("todayCrazyChannel") != null) {
                map.put(itemEntity.getItemId().toString(), itemEntity.getExtMap().get("todayCrazyChannel").toString());
            } else {
                tacLogger.info("tairKey为空itemId");
            }

        });
        return map;
    }

    public static HashMap<String, String> getItemIdAndCacheKey(Map<String, Object> userParams) {
        return (HashMap<String, String>) userParams.get(CommonConstant.ITEM_ID_AND_CACHE_KEYS);
    }


    public static void setTacLogger(Logger tacLogger) {
        CommonUtil.tacLogger = tacLogger;
    }
}
