package com.tmall.wireless.tac.biz.processor.firstScreenMind.enums;
import lombok.Getter;

/**门店类型，以判店结果为导向，主要用在tpp、captain的商品门店类型中*/
public enum RenderStoreTypeEnum {

    oneHour("oneHour","一小时达门店类型"),
    halfDay("halfDay","半日达门店类型"),
    nextDay("nextDay","外仓门店类型"),
    ;

    RenderStoreTypeEnum(String type, String description) {
        this.type = type;
        this.description = description;
    }

    @Getter
    private final String type;
    @Getter
    private final String description;

}
