package com.tmall.wireless.tac.biz.processor.detail.model;

import java.util.Map;
import java.util.Objects;

import com.tmall.wireless.tac.biz.processor.detail.common.constant.DetailConstant;
import com.tmall.wireless.tac.client.domain.Context;
import lombok.Data;

/**
 * @author: guichen
 * @Data: 2021/9/10
 * @Description:
 */
@Data
public class DetailRecommendRequest {
    private Integer index;
    private Integer pageSize;
    private Long detailItemId;
    private String locType;
    private Long contentId;
    private String itemSetsId;

    /**
     * 推荐类型，如菜谱或者普通类型
     */
    private String recType;
    /**
     * 叶子类目id，由于加权
     */
    private Long cateId;

    public DetailRecommendRequest(){ }

    public DetailRecommendRequest(Map<String,Object> params){
        DetailRecommendRequest recommendRequest=new DetailRecommendRequest();
        recommendRequest.setDetailItemId((Long)params.get("detailItemId"));
        recommendRequest.setLocType((String) params.get("locType"));
        recommendRequest.setIndex((Integer)params.get("index"));
        recommendRequest.setPageSize((Integer)params.get("pageSize"));
        recommendRequest.setRecType((String)params.get("recType"));
    }

    public static DetailRecommendRequest getDetailRequest(Context tacContext){
        return (DetailRecommendRequest)tacContext.getParams()
            .get(DetailConstant.REQUEST);
    }
}
