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

    /**
     * 轮播文案
     */
    private String itemCarouselDesc;

    public static ItemConfig valueOf(Map<String, Object> stringObjectMap) {
        ItemConfig itemConfig = new ItemConfig();
        itemConfig.setItemId(Long.valueOf(String.valueOf(stringObjectMap.get("contentId"))));
        if(stringObjectMap.get("shortTitle") != null) {
            itemConfig.setItemName(String.valueOf(stringObjectMap.get("shortTitle")));
        }
        if(stringObjectMap.get("groupNo") != null) {
            itemConfig.setGroupNo(Integer.valueOf(String.valueOf(stringObjectMap.get("groupNo"))));
        } else {
            //给一个默认值，避免报错，正常不会走到这里
            itemConfig.setGroupNo(100);
        }
        itemConfig.setForceSort("1".equals(String.valueOf(stringObjectMap.get("forceSort"))));
        if(stringObjectMap.get("sequenceNo") != null) {
            itemConfig.setSequenceNo(Integer.parseInt(String.valueOf(stringObjectMap.get("sequenceNo"))));
        } else {
            itemConfig.setSequenceNo(100);
        }
        if(stringObjectMap.get("exposurePercent") != null) {
            try {
                itemConfig.setExposurePercent(Integer.parseInt(String.valueOf(stringObjectMap.get("exposurePercent"))));
            } catch (Exception e) {
                itemConfig.setExposurePercent(0);
            }
        } else {
            itemConfig.setExposurePercent(0);
        }
        if(stringObjectMap.get("activityId") != null) {
            itemConfig.setActivityId(String.valueOf(stringObjectMap.get("activityId")));
        }
        if(stringObjectMap.get("couponValue") != null) {
            itemConfig.setCouponValue(String.valueOf(stringObjectMap.get("couponValue")));
        }
        if(stringObjectMap.get("itemDescCustom") != null) {
            itemConfig.setItemDescCustom(String.valueOf(stringObjectMap.get("itemDescCustom")));
        }
        if(stringObjectMap.get("itemImg") != null) {
            itemConfig.setItemImg(String.valueOf(stringObjectMap.get("itemImg")));
        }

        if(stringObjectMap.get("itemCarouselDesc") != null) {
            itemConfig.setItemCarouselDesc(String.valueOf(stringObjectMap.get("itemCarouselDesc")));
        }
        return itemConfig;
    }
}
