package com.tmall.wireless.tac.biz.processor.detail.common.constant;

import lombok.Getter;

/**
 * @author: guichen
 * @Data: 2021/9/10
 * @Description:
 */
public enum RecTypeEnum {

    RECIPE("recipe","菜谱推荐"),

    SIMILAR_ITEM("similarItem","相似商品推荐");
    @Getter
    private final String type;
    @Getter
    private final String description;

    RecTypeEnum(String type, String description) {
        this.type = type;
        this.description = description;
    }


}
