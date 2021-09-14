package com.tmall.wireless.tac.biz.processor.detail.model;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import lombok.Data;

/**
 * @author: guichen
 * @Data: 2021/9/10
 * @Description:
 */
@Data
public class DetailRecContentResultVO {


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
     * 结果
     */
    List<DetailRecommendVO> result;


}
