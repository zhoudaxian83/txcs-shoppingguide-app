package com.tmall.wireless.tac.biz.processor.firstScreenMind.model.item;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public class ItemRuleInfo implements Serializable {

    private static final long serialVersionUID = -1L;

    /**商品定投人群规则*/
    @Getter @Setter
    private String crowdIdRule;

    /**商品定投区域规则*/
    @Getter @Setter
    private String smAreaIdRule;

    /**商品定投门店规则*/
    @Getter @Setter
    private String storeIdRule;

    /**商品排期规则-开始时间*/
    @Getter @Setter
    private String scheduleStartRule;

    /**商品排期规则-结束时间*/
    @Getter @Setter
    private String scheduleEndRule;

    /**商品置顶规则*/
    @Getter @Setter
    private String stickRule;

    /**商品渠道规则*/
    @Getter @Setter
    private String channelRule;

}
