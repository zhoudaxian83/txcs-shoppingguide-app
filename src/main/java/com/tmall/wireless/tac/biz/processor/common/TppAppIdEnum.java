package com.tmall.wireless.tac.biz.processor.common;

import lombok.Getter;

/**
 * @author guijian
 * @date 2021/05/18
 */
public enum TppAppIdEnum {

    NEW_RECOMMEND(21431L,"推荐工程今日疯抢");

    @Getter
    private final Long appId;
    @Getter
    private final String description;

    TppAppIdEnum(Long appId, String description) {
        this.appId = appId;
        this.description = description;
    }
}
