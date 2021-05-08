package com.tmall.wireless.tac.biz.processor.firstScreenMind.model.content;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

public class SubContentModel implements Serializable {

    private static final long serialVersionUID = -1L;

    /**子内容ID*/
    @Getter @Setter
    private String subContentId;

    /**子内容标题*/
    @Getter @Setter
    private String subContentTitle;

    /**子内容正常图片*/
    @Getter @Setter
    private String subContentPic;

    /**子内容类型*/
    @Getter @Setter
    private String subContentType;

    /**子内容跳转链接*/
    @Getter @Setter
    private String subContentJumpUrl;

    /**子内容对应的圈品集ID，如果是多个，则用逗号隔开*/
    @Getter @Setter
    private String itemSetIds;

}
