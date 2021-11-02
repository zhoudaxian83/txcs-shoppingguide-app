package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public class DataSourceRequest implements Serializable {

    private static final long serialVersionUID = -1L;

    @Getter
    @Setter
    private String cacheKey;
    @Getter
    @Setter
    private int version;

    @Getter
    @Setter
    private String tab;

}
