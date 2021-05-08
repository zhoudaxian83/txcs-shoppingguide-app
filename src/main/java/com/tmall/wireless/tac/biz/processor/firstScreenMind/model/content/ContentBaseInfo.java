package com.tmall.wireless.tac.biz.processor.firstScreenMind.model.content;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

public class ContentBaseInfo implements Serializable {

    private static final long serialVersionUID = -1L;

    /**内容ID*/
    @Getter @Setter
    private String contentId;

    /**内容集ID*/
    @Getter @Setter
    private String contentSetId;

    /**内容标题*/
    @Getter @Setter
    private String contentTitle;

    /**内容图片-小*/
    @Getter @Setter
    private String contentPic;

    /**内容背景图片-大*/
    @Getter @Setter
    private String contentBackgroundPic;

    /**内容跳转链接*/
    @Getter @Setter
    private String contentJumpUrl;

    /**内容类型*/
    @Getter @Setter
    private String contentType;

    /**内容来源*/
    @Getter @Setter
    private String contentSource;

    /**内容对应的圈品集ID，如果是多个，则用逗号隔开*/
    @Getter @Setter
    private String itemSetIds;

    /**内容埋点*/
    @Getter @Setter
    private String scm;

    /**内容描述*/
    @Getter @Setter
    private String contentDesc;

    /**判断内容类型是否是O2O内容*/
    /*public boolean O2OContentType(){
        return RenderContentTypeEnum.checkO2OContentType(contentType);
    }*/
}
