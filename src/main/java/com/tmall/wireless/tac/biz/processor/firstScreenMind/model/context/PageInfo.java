package com.tmall.wireless.tac.biz.processor.firstScreenMind.model.context;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

public class PageInfo implements Serializable {

    private static final long serialVersionUID = -1L;

    /**当前请求是否是第一页*/
    @Getter@Setter
    private boolean firstPage = false;

    /**分页数据开始位置，默认从0开始*/
    @Getter @Setter
    private int pageStartPosition = 0;

    /**每页数据量大小，默认20*/
    @Getter @Setter
    private int pageSize = 20;

    /**数据分页缓存tair版本，默认-1*/
    @Getter @Setter
    private int version = -1;

    /**是否还有下一页请求，默认没有*/
    @Getter@Setter
    private boolean hasMore = false;

    /**总数量*/
    @Getter@Setter
    private int totalCount = 0;

}
