package com.tmall.wireless.tac.biz.processor.firstScreenMind.enums;

import lombok.Getter;

public enum RenderDataSourceEnum {

    sceneLabelBottomRecallSource("sceneLabelBottomRecallSource","场景中心打底召回来源"),
    tppRecallSource("tppRecallSource","tpp召回数据源"),
    tppPageCacheRecallSource("tppPageCacheRecallSource","tpp分页缓存召回数据源"),
    localCacheBottomRecallSource("localCacheRecallSource","本地缓存打底召回数据源"),


    other("other","unknown");

    @Getter
    private String code ;

    @Getter
    private String message ;

    private RenderDataSourceEnum(String code, String message) {
        this.message = message;
        this.code = code;
    }

    public static String getMessageDescByCode(String code){
        for(RenderDataSourceEnum dataSourceEnum : RenderDataSourceEnum.values()){
            if(dataSourceEnum.code.equals(code)){
                return dataSourceEnum.message;
            }
        }
        return RenderDataSourceEnum.other.message;
    }

    public static RenderDataSourceEnum getRenderDataSourceEnum(String code){
        for(RenderDataSourceEnum dataSourceEnum : RenderDataSourceEnum.values()){
            if(dataSourceEnum.code.equals(code)){
                return dataSourceEnum;
            }
        }
        return RenderDataSourceEnum.other;
    }
}
