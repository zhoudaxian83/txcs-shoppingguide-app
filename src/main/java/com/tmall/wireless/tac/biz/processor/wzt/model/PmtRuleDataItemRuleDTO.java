package com.tmall.wireless.tac.biz.processor.wzt.model;

import java.util.List;

import lombok.Data;

/**
 * @Author: luoJunChong
 * @Date: 2021/5/17 19:05
 */
@Data
public class PmtRuleDataItemRuleDTO {
    private static final long serialVersionUID = -1L;
    private ColumnCenterPmtRuleDataSetDTO pmtRuleDataSetDTO;
    private List<ColumnCenterDataSetItemRuleDTO> dataSetItemRuleDTOList;
}
