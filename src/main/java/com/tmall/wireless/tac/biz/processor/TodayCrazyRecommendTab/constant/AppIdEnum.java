package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.constant;

import lombok.Getter;

public enum AppIdEnum {
    INDEX_APP_ID(22519L, "首页appId"),
    TAB_APP_ID(21431L, "承接页appId"),
    ;

    AppIdEnum(long code, String description) {
        this.code = code;
        this.description = description;
    }

    @Getter
    private final long code;
    @Getter
    private final String description;

}
