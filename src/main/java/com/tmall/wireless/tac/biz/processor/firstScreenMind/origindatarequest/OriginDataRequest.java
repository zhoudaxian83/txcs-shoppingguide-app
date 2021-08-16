package com.tmall.wireless.tac.biz.processor.firstScreenMind.origindatarequest;

import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.txcs.gs.model.spi.model.RecommendRequest;

/**
 * @author guijian
 */
public interface OriginDataRequest {
    public RecommendRequest buildRecommendRequest(SgFrameworkContext sgFrameworkContext);
}
