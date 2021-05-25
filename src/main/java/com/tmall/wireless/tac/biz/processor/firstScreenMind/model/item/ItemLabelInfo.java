package com.tmall.wireless.tac.biz.processor.firstScreenMind.model.item;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ItemLabelInfo implements Serializable {

    private static final long serialVersionUID = -1L;

    /**商品大促标*/
    @Getter @Setter
    private String bigActivitySaleBiu;

    /**商品日常标*/
    @Getter @Setter
    private String dayActivitySaleBiu;

    /**商品通用卖点,直接透传captain返回结果*/
    @Getter @Setter
    private Map<String, Object> attachments = new ConcurrentHashMap<>();

    /**商品满返猫超卡标*/
    @Getter @Setter
    private String supermarketCardInfo;

    /**利益点标签列表(满减+满折+猫超卡)*/
    @Getter @Setter
    private List<Map<String,String>> benefitLabelList = new ArrayList<>();

    /**商品标签描述，兼容前端组件*/
    @Getter @Setter
    private String itemDesc;

    /**O2O商品描述*/
    @Getter @Setter
    private String o2oDesc;
}
