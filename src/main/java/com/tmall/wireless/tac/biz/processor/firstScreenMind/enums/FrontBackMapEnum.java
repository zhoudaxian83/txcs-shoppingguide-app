package com.tmall.wireless.tac.biz.processor.firstScreenMind.enums;

import lombok.Getter;

/**
 * 前端key	后台数据property key	字段说明
 * contentPic	avatarUrl	外部小图
 * contentBackgroundPic	bannerUrl	内部大图/视频场景封面图
 * contentVideoUrl	mediaUrl	视频地址
 * contentAuthor	author	作者(菜谱场景作者)
 * contentSeeCount	seeCount	查看人数(菜谱场景查看人数)
 * contentCustomLink	linkUrl	场景自定义承接页链接
 * contentTitle	title	场景主标题 (注意 在Tair DTO中)
 * contentSubtitle	subtitle	场景副标题 (注意 在Tair DTO中)
 */
public enum FrontBackMapEnum {
    contentPic("contentPic","avatarUrl","外部小图"),
    contentBackgroundPic("contentBackgroundPic","bannerUrl","内部大图/视频场景封面图"),
    contentVideoUrl("contentVideoUrl","mediaUrl","视频地址"),
    contentAuthor("contentAuthor","author","作者菜谱场景作者"),
    contentSeeCount("contentSeeCount","seeCount","查看人数菜谱场景查看人数"),
    contentCustomLink("contentCustomLink","linkUrl","场景自定义承接页链接"),
    contentShortTitle("contentShortTitle", "iconShortTitle", "icon加购后推荐场景短标题")
//    contentTitle("contentTitle","title","场景主标题tair提供"),
//    contentSubtitle("contentSubtitle","subtitle","场景副标题tair提供")
    ;

    @Getter
    private String front ;

    @Getter
    private String back ;

    @Getter
    private String message ;

    private FrontBackMapEnum(String front,String back,String message){
        this.front = front;
        this.back = back;
        this.message = message;
    }
/*    public String getBackbyFront(String front){
        for(FrontBackMapEnum frontBackMapEnum :FrontBackMapEnum.values()){
            if(frontBackMapEnum.front.equals(front)){
                return frontBackMapEnum.back;
            }
        }
    }*/
}
