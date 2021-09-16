package com.tmall.wireless.tac.biz.processor.extremeItem.domain;

import lombok.Data;

import java.util.Map;

@Data
public class ItemConfig {
    /**
     * 商品Id
     */
    private Long itemId;

    /**
     * 商品名称
     */
    private String itemName;

    /**
     * 分组编号
     */
    private Integer groupNo;

    /**
     * 是否强制排序，如果强制排序，则不走赛马逻辑
     */
    private boolean forceSort;

    /**
     * 分组顺序，如果强制排序，则各分组间按照该字段从小到达排序，每个分组的所有商品该字段必须保持一致
     */
    private Integer sequenceNo;

    /**
     * 曝光比例（0，100）
     */
    private Integer exposurePercent;

    /**
     * 优惠券UUID
     */
    private String activityId;

    /**
     * 优惠券面额
     */
    private String couponValue;

    /**
     * 利益点
     */
    private String itemDesc;

    /**
     * 一排一布局主图
     */
    private String wideImg;

    /**
     * 一排二布局主图
     */
    private String normalImg;

    public static ItemConfig valueOf(Map<String, Object> stringObjectMap) {
        ItemConfig itemConfig = new ItemConfig();
        itemConfig.setItemId((Long)stringObjectMap.get("contentId"));
        itemConfig.setItemName((String)stringObjectMap.get("shortTitle"));
        itemConfig.setGroupNo((Integer)stringObjectMap.get("groupNo"));
        itemConfig.setForceSort((Integer)stringObjectMap.get("forceSort") == 1);
        itemConfig.setSequenceNo((Integer) stringObjectMap.get("sequenceNo"));
        itemConfig.setExposurePercent((Integer) stringObjectMap.get("exposurePercent"));
        itemConfig.setActivityId((String)stringObjectMap.get("activityId"));
        itemConfig.setCouponValue((String)stringObjectMap.get("couponValue"));
        itemConfig.setItemDesc((String)stringObjectMap.get("itemDesc"));
        itemConfig.setWideImg((String)stringObjectMap.get("wideImg"));
        itemConfig.setNormalImg((String)stringObjectMap.get("normalImg"));
        return itemConfig;
    }
}
