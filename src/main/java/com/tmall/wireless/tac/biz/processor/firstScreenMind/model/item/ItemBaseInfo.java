package com.tmall.wireless.tac.biz.processor.firstScreenMind.model.item;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class ItemBaseInfo implements Serializable {

    private static final long serialVersionUID = -1L;

    /**商品skuID*/
    @Getter @Setter
    private Long skuId;
    /**商品标题*/
    @Getter @Setter
    private String title;
    /**商品主图*/
    @Getter @Setter
    private String mainPic;
    /**商品白底图*/
    @Getter @Setter
    private String whiteImgUrl;
    /**商品详情URL*/
    @Getter @Setter
    private String itemUrl;

    /**商品短标题*/
    @Getter @Setter
    private String shortTitle;
    /**商品规格*/
    @Getter @Setter
    private String specifications;
    /**商品属性*/
    @Getter @Setter
    private List<String> selfSupportProperties;
    /*** 商品标签*/
    @Getter @Setter
    private Set<Integer> itemTags;

    /**商品分层*/
    @Getter @Setter
    private String itemLevelTag;
    /**商品类型*/
    @Getter @Setter
    private String itemType;
    /**商品来源*/
    @Getter @Setter
    private String itemSource;
    /**商品门店类型*/
    @Getter @Setter
    private String itemLocType;

}
