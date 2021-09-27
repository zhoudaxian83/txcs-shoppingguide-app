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
    private String itemDescCustom;

    /**
     * 商品图片
     */
    private String itemImg;

    public static ItemConfig valueOf(Map<String, Object> stringObjectMap) {
        ItemConfig itemConfig = new ItemConfig();
        itemConfig.setItemId(Long.valueOf(String.valueOf(stringObjectMap.get("contentId"))));
        itemConfig.setItemName(String.valueOf(stringObjectMap.get("shortTitle")));
        itemConfig.setGroupNo(Integer.valueOf(String.valueOf(stringObjectMap.get("groupNo"))));
        itemConfig.setForceSort(Integer.parseInt(String.valueOf(stringObjectMap.get("forceSort"))) == 1);
        itemConfig.setSequenceNo(Integer.parseInt(String.valueOf(stringObjectMap.get("sequenceNo"))));
        itemConfig.setExposurePercent(Integer.parseInt(String.valueOf(stringObjectMap.get("exposurePercent"))));
        itemConfig.setActivityId(String.valueOf(stringObjectMap.get("activityId")));
        itemConfig.setCouponValue(String.valueOf(stringObjectMap.get("couponValue")));
        itemConfig.setItemDescCustom(String.valueOf(stringObjectMap.get("itemDescCustom")));
        itemConfig.setItemImg(String.valueOf(stringObjectMap.get("itemImg")));
        return itemConfig;
    }
}
