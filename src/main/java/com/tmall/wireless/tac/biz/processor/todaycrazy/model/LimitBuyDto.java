package com.tmall.wireless.tac.biz.processor.todaycrazy.model;

import java.util.List;

import com.tmall.aselfmanager.client.columncenter.response.ColumnCenterDataSetItemRuleDTO;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * @author guijian
 */
@Data
public class LimitBuyDto {
    @Getter @Setter
    private Long startTime;
    @Getter @Setter
    private Long endTime;
    @Getter @Setter
    private List<ColumnCenterDataSetItemRuleDTO>  columnCenterDataSetItemRuleDTOS;
    @Getter @Setter
    private Boolean isHit;


}
