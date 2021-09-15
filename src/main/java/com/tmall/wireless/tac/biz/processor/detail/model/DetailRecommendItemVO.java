package com.tmall.wireless.tac.biz.processor.detail.model;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: guichen
 * @Data: 2020/12/8
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetailRecommendItemVO extends DetailRecommendVO {

    private Long itemId;
    private List<DetailTextComponentVO> price;
}