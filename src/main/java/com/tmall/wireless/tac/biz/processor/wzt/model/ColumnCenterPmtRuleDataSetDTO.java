package com.tmall.wireless.tac.biz.processor.wzt.model;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * @Author: luoJunChong
 * @Date: 2021/5/17 19:06
 */
@Data
public class ColumnCenterPmtRuleDataSetDTO implements Serializable {
    private static final long serialVersionUID = -1L;
    private Long id;
    private String bizCode;
    private String ruleName;
    private Date scheduleStartTime;
    private Date scheduleEndTime;
    private String ruleLevel;
    private String dataSetType;
    private String ruleType;
    private Long pmtId;
    private String ruleStatus;
    private String extension;
    private Long dataSetId;
    private String dataType;
    private String itemSetId;
    private Long pmtRuleId;
    private Long contentSetId;
    private String dataSetName;
}
