package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.constant;
import lombok.Getter;

public enum TabTypeEnum {
    OTHER("other","其他Tab"),
    TODAY_CHAO_SHENG("todayChaoSheng","今日超省Tab"),
    ;

    TabTypeEnum(String type, String description) {
        this.type = type;
        this.description = description;
    }

    @Getter
    private final String type;
    @Getter
    private final String description;

}
