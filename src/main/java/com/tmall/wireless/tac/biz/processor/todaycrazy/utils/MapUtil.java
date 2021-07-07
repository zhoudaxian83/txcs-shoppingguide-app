package com.tmall.wireless.tac.biz.processor.todaycrazy.utils;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author guijian
 */
public class MapUtil {
    public MapUtil() {
    }

    public static Long getLongWithDefault(Map<String, Object> map, String key, Long defaultValue) {
        if (!MapUtils.isEmpty(map) && map.get(key) != null) {
            return !StringUtils.isNumeric(map.get(key).toString()) ? defaultValue : Long.valueOf(map.get(key).toString());
        } else {
            return defaultValue;
        }
    }

    public static String getStringWithDefault(Map<String, Object> map, String key, String defaultValue) {
        return !MapUtils.isEmpty(map) && map.get(key) != null ? map.get(key).toString() : defaultValue;
    }

    public static Integer getIntWithDefault(Map<String, Object> map, String key, Integer defaultValue) {
        return !MapUtils.isEmpty(map) && map.get(key) != null ? Integer.valueOf(map.get(key).toString()) : defaultValue;
    }
}
