package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.model;

import lombok.Data;

@Data
public class DataSourceRequest {

    //private static final long serialVersionUID = -1L;

    private String cacheKey;

    private int version;

    private String tab;

}
