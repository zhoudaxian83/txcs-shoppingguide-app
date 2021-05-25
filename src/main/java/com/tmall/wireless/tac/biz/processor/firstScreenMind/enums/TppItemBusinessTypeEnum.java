package com.tmall.wireless.tac.biz.processor.firstScreenMind.enums;
import lombok.Getter;

public enum TppItemBusinessTypeEnum {

    B2C("B2C","TPP商品主站业务类型"),
    OneHour("OneHour","TPP商品一小时达业务类型"),
    HalfDay("HalfDay","TPP商品半日达业务类型"),
    NextDay("NextDay","TPP商品外仓业务类型"),
    ;

    TppItemBusinessTypeEnum(String type, String description) {
        this.type = type;
        this.description = description;
    }

    @Getter
    private final String type;
    @Getter
    private final String description;

}
