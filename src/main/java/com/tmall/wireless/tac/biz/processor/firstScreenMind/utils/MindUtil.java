package com.tmall.wireless.tac.biz.processor.firstScreenMind.utils;

import java.util.Map;

import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.wireless.tac.biz.processor.common.RequestKeyConstantApp;

public class MindUtil {
    public static boolean isMind(Map<String, Object> requestParams) {

        Long mindContentCode = MapUtil.getLongWithDefault(requestParams,
            RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_MIND, 0L);

        if (mindContentCode <= 0) {
            return false;
        }

        Object isFixPositionBanner = requestParams.get("isFixPositionBanner");

        Boolean isMind = false;
        if (isFixPositionBanner == null || "".equals(isFixPositionBanner)) {
            isMind = true;
        } else if (isFixPositionBanner instanceof Boolean) {
            isMind = (Boolean)isFixPositionBanner;
        } else if (isFixPositionBanner instanceof String && "true".equals(isFixPositionBanner)) {
            isMind = true;
        }
        if(isItemFeeds(requestParams)){
            isMind = false;
        }
        return isMind;
    }
    public static boolean isItemFeeds(Map<String, Object> requestParams){
        Boolean isItemFeeds = false;
        String requestFrom = MapUtil.getStringWithDefault(requestParams,"requestFrom","");
        if("itemFeeds".equals(requestFrom)){
            isItemFeeds = true;
        }
        return isItemFeeds;
    }
}
