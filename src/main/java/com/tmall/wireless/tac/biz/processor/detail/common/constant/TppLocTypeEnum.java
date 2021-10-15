package com.tmall.wireless.tac.biz.processor.detail.common.constant;

import lombok.Getter;

/**
 * @author: guichen
 * @Data: 2021/9/18
 * @Description:
 */
public enum TppLocTypeEnum {
    one_hour("one_hour", "一小时达"),
    half_day("half_day","半日达"),
    next_day("next_day", "次日达"),
    B2C("B2C","B2C商品")
    ;

    TppLocTypeEnum(String locType, String desc) {
        this.locType = locType;
        this.desc = desc;
    }

    @Getter
    private String locType;
    @Getter
    private String desc;
}
