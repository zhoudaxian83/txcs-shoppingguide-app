package com.tmall.wireless.tac.biz.processor.detail.model;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

import lombok.Data;
import lombok.ToString;

/**
 * @author: guichen
 * @Data: 2021/9/13
 * @Description:
 */
@Data
public class DetailRecItemResultVO{

    /**
     * 类型
     */
    private String type="simple";


    /**
     * 标题
     */
    private List<DetailTextComponentVO> title;

    /**
     * 埋点
     */
    private JSONObject exposureExtraParam;

    /**
     * 是否允许滑动
     */
    private boolean enableScroll;

    /**
     * 是否展示角标
     */
    private boolean showArrow;

    /**
     * 场景id
     */
    private Long contentId;

    /**
     * 商品集id
     */
    private String itemSetIds;

    /**
     * 结果
     */
    List<DetailRecommendItemVO> result;
}
