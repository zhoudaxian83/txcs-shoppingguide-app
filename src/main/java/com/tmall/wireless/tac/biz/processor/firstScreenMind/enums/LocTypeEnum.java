package com.tmall.wireless.tac.biz.processor.firstScreenMind.enums;

import lombok.Getter;

/**
 * @author guijian
 */

public enum LocTypeEnum {
    B2C("B2C","TPP商品主站业务类型"),
    O2OOneHour("O2OOneHour","商品一小时达业务类型"),
    O2OHalfDay("O2OHalfDay","商品半日达业务类型"),
    O2ONextDay("O2ONextDay","商品外仓业务类型"),
    ;

    LocTypeEnum(String type, String description) {
        this.type = type;
        this.description = description;
    }

    @Getter
    private final String type;
    @Getter
    private final String description;
}
