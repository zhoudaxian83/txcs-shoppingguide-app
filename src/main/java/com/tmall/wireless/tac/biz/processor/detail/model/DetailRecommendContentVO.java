package com.tmall.wireless.tac.biz.processor.detail.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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

    List<DetailRecommendItemVO> recommendItemVOS;

}
