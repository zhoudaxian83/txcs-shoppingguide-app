package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class DataSourceRequest implements Serializable {

    private static final long serialVersionUID = -1L;

    private String cacheKey;

    private int version;

    private String tab;

}
