package com.tmall.wireless.tac.biz.processor.firstScreenMind.model.content;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

public class ContentRuleInfo implements Serializable {

    private static final long serialVersionUID = -1L;

    /**内容定投人群规则*/
    @Getter @Setter
    private String crowdIdRule;

    /**内容定投区域规则*/
    @Getter @Setter
    private String smAreaIdRule;

    /**内容定投门店规则*/
    @Getter @Setter
    private String storeIdRule;

    /**内容排期规则-开始时间*/
    @Getter @Setter
    private String scheduleStartRule;

    /**内容排期规则-结束时间*/
    @Getter @Setter
    private String scheduleEndRule;

    /***内容排期有效起始时间*/
    @Getter @Setter
    private Date scheduleStartDate;

    /***内容排期有效结束时间*/
    @Getter @Setter
    private Date scheduleEndDate;

    /**内容置顶规则*/
    @Getter @Setter
    private String stickRule;

    /**内容渠道规则*/
    @Getter @Setter
    private String channelRule;

}
