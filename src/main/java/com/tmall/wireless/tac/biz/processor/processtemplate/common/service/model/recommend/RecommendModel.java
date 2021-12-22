package com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend;

import java.util.List;

public interface RecommendModel {
    /**
     * 获取所有的商品ID，重复的商品ID会进行distinct去重逻辑
     *
     * @return 所有的商品ID
     */
    List<Long> fetchAllItemIds();
}
