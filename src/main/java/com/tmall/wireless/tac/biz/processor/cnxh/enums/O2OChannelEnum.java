package com.tmall.wireless.tac.biz.processor.cnxh.enums;

/**
 * @Author: luoJunChong
 * @Date: 2021/5/27 14:08
 * description:
 */
public enum O2OChannelEnum {
    ONE_HOUR("oneHour", "小时达", 21895L),
    HALF_DAY("halfDay", "半日达", 21896L),
    NEXT_DAY("nextDay", "次日达", 21896L),
    ALL_FRESH("allFresh", "全域生鲜", 21896L);

    private final String code;
    private final String name;
    private final long appId;

    private O2OChannelEnum(String code, String name, long appId) {
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

    public static O2OChannelEnum ofCode(String code) {
        for (O2OChannelEnum area : O2OChannelEnum.values()) {
            if (area.getCode().equals(code)) {
                return area;
            }
        }
        return null;
    }

}