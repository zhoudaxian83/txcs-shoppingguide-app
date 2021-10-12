package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.util;

import com.tmall.tcls.gs.sdk.framework.model.context.ItemEntity;

public class CommonUtil {

    /**
     * 区分是算法或者其它
     * @param itemEntity
     * @return
     */
    public static String getItemChannel(ItemEntity itemEntity,String tabType){
        //itemEntity.getExtMap().put("todayCrazyChannel",jsonObject.getLong("tairKey"));
        return "";
    }

    public static boolean getLimit(ItemEntity itemEntity,String tabType){
        //itemEntity.getExtMap().put("todayCrazyChannel",jsonObject.getLong("tairKey"));

        return false;
    }


}
