package com.tmall.wireless.tac.biz.processor.wzt.model;

import lombok.Data;

/**
 * @Author: luoJunChong
 * @Date: 2021/5/17 19:06
 */
@Data
public class ColumnCenterDataSetItemRuleDTO {
    private static final long serialVersionUID = -1L;
    private Long id;
    private Long itemId;
    private String itemTitle;
    private Long dataSetId;
    private Long pmtRuleId;
    private String itemType;
    private Long sellInventory;
    private String supplyPrice;
    private Long totalPurchase;
    private Long individualPurchase;
    private String upSelfRation;
    private String itemExtension;
    private ColumnCenterDataRuleDTO dataRule;
}
