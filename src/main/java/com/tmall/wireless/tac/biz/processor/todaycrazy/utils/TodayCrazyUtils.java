package com.tmall.wireless.tac.biz.processor.todaycrazy.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class TodayCrazyUtils {

    /**
     * 解析extension
     *
     * @param extension
     * @param itemSepRegex
     * @param kvSepRegex
     * @param trim
     * @return
     */
    public static Map<String, Object> parseExtension(String extension,
                                                     String itemSepRegex,
                                                     String kvSepRegex,
                                                     boolean trim) {
        if (StringUtils.isEmpty(extension)) {
            return Collections.emptyMap();
        }
        String[] items = trim ? extension.trim().split(itemSepRegex) : extension.split(itemSepRegex);
        if (ArrayUtils.isEmpty(items)) {
            return Collections.emptyMap();
        }
        Map<String, Object> resultMap = new HashMap<>();
        for (String item : items) {
            String[] kvs = trim ? item.trim().split(kvSepRegex) : item.split(kvSepRegex);
            if (ArrayUtils.isEmpty(kvs)
                || kvs.length < 2) {
                continue;
            }
            resultMap.put(kvs[0], kvs[1]);
        }
        return resultMap;
    }
}
