package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

public class DataSourceRequest implements Serializable {

    private static final long serialVersionUID = -1L;

    @Getter@Setter
    private Long userId;
    @Getter@Setter
    private Long smAreaId;
    @Getter@Setter
    private String bizCode;
    @Getter@Setter
    private String dateStr;//时间
    @Getter@Setter
    private int version;//缓存的版本号
    @Getter@Setter
    private String tab; //模块类型

    @Setter@Getter
    private List<Long> itemIdList; //当前页的数据ID

    public DataSourceRequest(){
    }
}
