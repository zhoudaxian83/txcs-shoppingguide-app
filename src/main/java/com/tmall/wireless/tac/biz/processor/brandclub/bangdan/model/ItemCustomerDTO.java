package com.tmall.wireless.tac.biz.processor.brandclub.bangdan.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class ItemCustomerDTO implements Serializable {
    private Long itemId;
    private int goodCommentCnt;
    private int monthSalesCnt;
    private int repurchaseCnt;

    private int itemRankValue;

}
