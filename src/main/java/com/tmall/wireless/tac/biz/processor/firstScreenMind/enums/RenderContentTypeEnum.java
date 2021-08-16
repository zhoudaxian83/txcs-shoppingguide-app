package com.tmall.wireless.tac.biz.processor.firstScreenMind.enums;

import com.google.common.collect.Sets;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.utils.RenderCheckUtil;
import lombok.Getter;

import java.util.Set;

public enum RenderContentTypeEnum {

    o2oNormalContent("o2oNormalContent","o2o普通场景","o2oNormalContent"),
    o2oCombineContent("o2oCombineContent","o2o组合场景","o2oCombineContent"),
    o2oBrandContent("o2oBrandContent","o2o品牌场景","o2oBrandContent"),

    b2cNormalContent("b2cNormalContent","b2c普通场景","b2cNormalContent"),
    b2cCombineContent("b2cCombineContent","b2c组合场景","b2cCombineContent"),
    b2cBrandContent("b2cBrandContent","b2c品牌场景","b2cBrandContent"),

    recipeContent("recipeContent","b2c品牌场景","recipeContent"),
    mediaContent("mediaContent","b2c品牌场景","mediaContent"),
    bangdanContent("bangdanContent","榜单场景","bangdanContent"),
    bangdanO2OContent("bangdanO2OContent","O2O榜单场景","bangdanO2OContent"),

    ;

    RenderContentTypeEnum(String type, String description, String frontDisplayType) {
        this.type = type;
        this.description = description;
        this.frontDisplayType = frontDisplayType;
    }

    @Getter
    private final String type;
    @Getter
    private final String description;
    @Getter
    private final String frontDisplayType;

    /**获取当前默认打底内容类型*/
    public static String getBottomContentType() {
        return b2cNormalContent.getType();
    }

    /**判断当前内容是否是O2O内容类型*/
    private static final Set<String> o2o_contentType_collection =
        Sets.newHashSet(o2oNormalContent.getType(),
                o2oCombineContent.getType(),
                o2oBrandContent.getType(),
                bangdanO2OContent.getType(),
                recipeContent.getType());

    public static boolean checkO2OContentType(String contentType) {
        if(RenderCheckUtil.StringEmpty(contentType)){
            return false;
        }
        return RenderContentTypeEnum.o2o_contentType_collection.contains(contentType);
    }

    /**判断当前内容是否是brand内容类型*/
    private static final Set<String> brand_contentType_collection =
        Sets.newHashSet(b2cBrandContent.getType(),o2oBrandContent.getType());

    public static boolean checkBrandContentType(String contentType) {
        if(RenderCheckUtil.StringEmpty(contentType)){
            return false;
        }
        return RenderContentTypeEnum.brand_contentType_collection.contains(contentType);
    }

    /**判断当前内容是否是normal内容类型*/
    private static final Set<String> normal_contentType_collection =
        Sets.newHashSet(o2oNormalContent.getType(),b2cNormalContent.getType());

    public static boolean checkNormalContentType(String contentType) {
        if(RenderCheckUtil.StringEmpty(contentType)){
            return false;
        }
        return RenderContentTypeEnum.normal_contentType_collection.contains(contentType);
    }

    /**判断当前内容是否是combine内容类型*/
    private static final Set<String> combine_contentType_collection =
        Sets.newHashSet(o2oCombineContent.getType(),b2cCombineContent.getType());

    public static boolean checkCombineContentType(String contentType) {
        if(RenderCheckUtil.StringEmpty(contentType)){
            return false;
        }
        return RenderContentTypeEnum.combine_contentType_collection.contains(contentType);
    }
}
