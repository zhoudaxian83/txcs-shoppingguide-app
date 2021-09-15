package com.tmall.wireless.tac.biz.processor.detail.model;

import lombok.Data;
import lombok.ToString;

/**
 * @author: guichen
 * @Data: 2021/9/13
 * @Description:
 */
@Data
@ToString(callSuper = true)
public class DetailRecItemResultVO extends DetailRecContentResultVO {

    /**
     * 场景id
     */
    private Long contentId;

    /**
     * 商品集id
     */
    private String itemSetIds;
}
