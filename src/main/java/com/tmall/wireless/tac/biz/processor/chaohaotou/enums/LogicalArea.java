package com.tmall.wireless.tac.biz.processor.chaohaotou.enums;

import java.io.Serializable;

/**
 * @Author: luoJunChong
 * @Date: 2021/5/17 16:49
 */
public enum LogicalArea implements Serializable {
    HD("107", "HD", "华东", 310100L, "wuZheTian_HD"),
    HB("108", "HB", "华北", 110100L, "wuZheTian_HB"),
    HN("109", "HN", "华南", 440100L, "wuZheTian_HN"),
    HZ("111", "HZ", "华中", 420100L, "wuZheTian_HZ"),
    XN("112", "XN", "西南/西北", 510100L, "wuZheTian_XN");

    private final String code;
    private final String shorthand;
    private final String name;
    private final long coreCityCode;
    private final String cacheKey;

    private LogicalArea(String code, String shorthand, String name, long coreCityCode, String cacheKey) {
        this.code = code;
        this.shorthand = shorthand;
        this.name = name;
        this.coreCityCode = coreCityCode;
        this.cacheKey = cacheKey;
    }

    public String getCode() {
        return this.code;
    }

    public String getShorthand() {
        return this.shorthand;
    }

    public String getName() {
        return this.name;
    }

    public long getCoreCityCode() {
        return this.coreCityCode;
    }

    public String getCacheKey() {
        return this.cacheKey;
    }

    public static LogicalArea ofCoreCityCode(Long coreCityCode) {
        for (LogicalArea area : LogicalArea
            .values()) {
            if (area.getCoreCityCode() == coreCityCode) {
                return area;
            }
        }
        return null;
    }

    public static LogicalArea ofCode(String code) {
        for (LogicalArea area :
            LogicalArea
            .values()) {
            if (area.getCode().equals(code)) {
                return area;
            }
        }
        return null;
    }

}