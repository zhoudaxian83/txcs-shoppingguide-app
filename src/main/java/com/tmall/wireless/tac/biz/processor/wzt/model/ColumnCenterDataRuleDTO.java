package com.tmall.wireless.tac.biz.processor.wzt.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: luoJunChong
 * @Date: 2021/5/17 19:07
 */
@Data
public class ColumnCenterDataRuleDTO implements Serializable {
    private static final long serialVersionUID = -1L;
    private Long dataRuleId;
    private Long stick;
    private Long hook;
    private Date itemScheduleStartTime;
    private Date itemScheduleEndTime;
    private Date itemStickStartTime;
    private Date itemStickEndTime;
    private String crowdId;
    private Long exposureFilter;
    private Long trafficAllocation;
    private String dataRuleLevel;
    private Long dataSourceId;
    private Long dataSetId;
    private Long itemScheduleStatus;
    private Long itemPromotionStatus;
    private String ruleExtension;
}
