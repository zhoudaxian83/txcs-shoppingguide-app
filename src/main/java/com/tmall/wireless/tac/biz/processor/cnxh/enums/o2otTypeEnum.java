package com.tmall.wireless.tac.biz.processor.cnxh.enums;

/**
 * @Author: luoJunChong
 * @Date: 2021/5/27 14:08
 * description:
 */
public enum o2otTypeEnum {
    ONE_HOUR("one_hour", "小时达", 20298L),
    HALF_DAY("half_day", "半日达", 20528L),
    NEXT_DAY("next_day", "次日达", 110100L),
    ALL_FRESH("all_fresh", "全域生鲜", 25624L);

    private final String code;
    private final String name;
    private final long appId;

    private o2otTypeEnum(String code, String name, long appId) {
        this.code = code;
        this.name = name;
        this.appId = appId;
    }

    public String getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }

    public long getAppId() {
        return this.appId;
    }

    public static o2otTypeEnum ofCode(String code) {
        for (o2otTypeEnum area : o2otTypeEnum.values()) {
            if (area.getCode().equals(code)) {
                return area;
            }
        }
        return null;
    }

}