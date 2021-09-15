package com.tmall.wireless.tac.biz.processor.detail.model;

import java.util.Map;
import java.util.Objects;

import com.alibaba.fastjson.util.TypeUtils;

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
    private String itemSetIds;

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
        this.setDetailItemId(TypeUtils.castToLong(params.get("detailItemId")));
        this.setLocType(TypeUtils.castToString(params.get("locType")));
        this.setIndex(TypeUtils.castToInt(params.get("index")));
        this.setPageSize(TypeUtils.castToInt(params.get("pageSize")));
        this.setRecType(TypeUtils.castToString(params.get("recType")));
        this.setContentId(TypeUtils.castToLong(params.get("contentId")));
        this.setItemSetIds(TypeUtils.castToString(params.get("itemSetIds")));
    }

    public static DetailRecommendRequest getDetailRequest(Context tacContext){
        return new DetailRecommendRequest(tacContext.getParams());
    }
}
