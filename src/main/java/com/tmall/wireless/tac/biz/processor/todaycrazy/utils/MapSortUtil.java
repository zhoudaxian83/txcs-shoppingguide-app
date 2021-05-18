package com.tmall.wireless.tac.biz.processor.todaycrazy.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MapSortUtil {
    /**
     * 使用 Map按value进行排序
     * @param oriMap
     * @return
     */
    public static Map<String, Float> sortMapByValue(Map<String, Float> oriMap) {
        if (oriMap == null || oriMap.isEmpty()) {
            return null;
        }
        Map<String, Float> sortedMap = new LinkedHashMap<String, Float>();
        List<Map.Entry<String, Float>> entryList = new ArrayList<Map.Entry<String, Float>>(
                oriMap.entrySet());
        Collections.sort(entryList, new MapValueComparatorUtil());

        Iterator<Map.Entry<String, Float>> iter = entryList.iterator();
        Map.Entry<String, Float> tmpEntry = null;
        while (iter.hasNext()) {
            tmpEntry = iter.next();
            sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
        }
        return sortedMap;
    }
}
