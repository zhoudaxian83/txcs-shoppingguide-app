package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.constant;
import lombok.Getter;

public enum AppTypeEnum {
    INDEX_PAGE("indexPage","首页"),
    TAB_PAGE("tabPage","承接页"),
    ;

    AppTypeEnum(String type, String description) {
        this.type = type;
        this.description = description;
    }

    @Getter
    private final String type;
    @Getter
    private final String description;

}
