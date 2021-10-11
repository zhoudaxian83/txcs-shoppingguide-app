package com.tmall.wireless.tac.biz.processor.detail.model;

import java.util.List;

import lombok.Data;
import lombok.ToString;

/**
 * @author: guichen
 * @Data: 2021/9/10
 * @Description:
 */
@Data
@ToString(callSuper = true)
public class DetailRecommendContentVO extends DetailRecommendVO{

    private Long contentId;

    private String itemSetIds;

    private List<DetailRecommendItemVO> recommendItemVOS;

}
