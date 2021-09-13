package com.tmall.wireless.tac.biz.processor.detail.model;

import java.util.List;

import lombok.Data;

/**
 * @author: guichen
 * @Data: 2021/9/10
 * @Description:
 */
@Data
public class DetailRecommendContentVO extends DetailRecommendVO{

    private Long contentId;

    private String itemSetIds;

    List<DetailRecommendItemVO> recommendItemVOS;

}
