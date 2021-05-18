package com.tmall.wireless.tac.biz.processor.todaycrazy.utils;

import java.util.Comparator;
import java.util.Map;

public class MapValueComparatorUtil implements Comparator<Map.Entry<String, Float>> {
    @Override
    public int compare(Map.Entry<String, Float> me1, Map.Entry<String, Float> me2) {

        return me1.getValue().compareTo(me2.getValue());
    }
}
