package com.tmall.wireless.tac.biz.processor.todaycrazy.utils;

import java.util.Comparator;
import java.util.Map;

public class MapValueComparatorUtil implements Comparator<Map.Entry<String, String>> {
    @Override
    public int compare(Map.Entry<String, String> me1, Map.Entry<String, String> me2) {

        return me1.getValue().compareTo(me2.getValue());
    }
}
