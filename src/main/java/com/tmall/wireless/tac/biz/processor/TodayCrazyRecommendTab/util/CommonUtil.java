package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.util;

import com.google.common.collect.Maps;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemEntity;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.constant.CommonConstant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommonUtil {

    public static HashMap<String, String> buildItemIdAndCacheKey(List<ItemEntity> itemEntities) {
        HashMap<String, String> map = Maps.newHashMap();
        itemEntities.forEach(itemEntity -> {
            map.put(itemEntity.getItemId().toString(), itemEntity.getExtMap().get("todayCrazyChannel").toString());
        });
        return map;
    }

    public static HashMap<String, String> getItemIdAndCacheKey(Map<String, Object> userParams) {
        return (HashMap<String, String>) userParams.get(CommonConstant.ITEM_ID_AND_CACHE_KEYS);
    }


}
