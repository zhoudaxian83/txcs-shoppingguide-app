package com.tmall.wireless.tac.biz.processor.wzt.enums;

import java.io.Serializable;

import com.ali.com.google.common.base.Strings;

/**
 * @Author: luoJunChong
 * @Date: 2021/5/17 16:49
 */
public enum LogicalArea implements Serializable {
    HD("107", "HD", "华东", 310100L,"wuZheTian_HD_pre"),
    HB("108", "HB", "华北", 110100L,"wuZheTian_HB_pre"),
    HN("109", "HN", "华南", 440100L,"wuZheTian_HN_pre"),
    HZ("111", "HZ", "华中", 420100L,"wuZheTian_HZ_pre"),
    XN("112", "XN", "西南/西北", 510100L,"wuZheTian_XN_pre");

    private final String code;
    private final String shorthand;
    private final String name;
    private final long coreCityCode;
    private final String cacheKey;

    private LogicalArea(String code, String shorthand, String name, long coreCityCode,String cacheKey) {
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

    public static LogicalArea parseByCode(String code) {
        if (Strings.isNullOrEmpty(code)) {
            return null;
        } else {
            LogicalArea[] var1 = values();
            int var2 = var1.length;

            for (int var3 = 0; var3 < var2; ++var3) {
                LogicalArea area = var1[var3];
                if (area.getCode().equals(code)) {
                    return area;
                }
            }

            return null;
        }
    }

    public static LogicalArea parseByShorthand(String shorthand) {
        if (Strings.isNullOrEmpty(shorthand)) {
            return null;
        } else {
            LogicalArea[] var1 = values();
            int var2 = var1.length;

            for (int var3 = 0; var3 < var2; ++var3) {
                LogicalArea area = var1[var3];
                if (area.getShorthand().equals(shorthand)) {
                    return area;
                }
            }

            return null;
        }
    }

    public static LogicalArea ofCoreCityCode(Long coreCityCode) {
        for (LogicalArea s : LogicalArea.values()) {
            if (s.getCoreCityCode() == coreCityCode) {
                return s;
            }
        }
        return null;
    }

}