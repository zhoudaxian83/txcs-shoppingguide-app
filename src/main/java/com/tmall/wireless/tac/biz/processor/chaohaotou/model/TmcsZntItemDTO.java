package com.tmall.wireless.tac.biz.processor.chaohaotou.model;

import lombok.Data;

import java.util.List;

@Data
public class TmcsZntItemDTO {

    /**
     * 资源位id
     */
    private Long itemId;

    /**
     * 品牌Id
     */
    private String brandId;

    /**
     * 叶子类目id
     */
    private String cateId;

    /**
     * 选品集列表
     */
    private List<String> itemSets;

    /**
     * 推荐类型 ITEM，固定值
     */
    private String recommendType;

    /**
     * 商业模式
     */
    private String commerceModel;

    /**
     * 算法埋点
     */
    private String trackPoint;
}
