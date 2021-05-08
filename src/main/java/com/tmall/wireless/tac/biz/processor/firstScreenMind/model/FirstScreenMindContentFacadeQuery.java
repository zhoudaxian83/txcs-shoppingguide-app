package com.tmall.wireless.tac.biz.processor.firstScreenMind.model;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

public class FirstScreenMindContentFacadeQuery extends FacadeQuery implements Serializable  {

    private static final long serialVersionUID = -1L;

    /********************逛超市特殊入参****************************/
    /**普通B2C内容集ID*/
    @Getter @Setter
    private String normalB2CContentSetIds;

    /**品牌内容集ID*/
    @Getter @Setter
    private String brandContentSetIds;

    /**O2O内容集ID*/
    @Getter @Setter
    private String normalO2OContentSetIds;

    /**内容类型*/
    @Getter @Setter
    private String contentType;

    /**是否是为你推荐tab*/
    @Getter @Setter
    private boolean recommendForUTab = false;

}
