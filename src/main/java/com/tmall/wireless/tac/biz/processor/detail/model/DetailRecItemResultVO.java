package com.tmall.wireless.tac.biz.processor.detail.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: guichen
 * @Data: 2021/9/13
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
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
