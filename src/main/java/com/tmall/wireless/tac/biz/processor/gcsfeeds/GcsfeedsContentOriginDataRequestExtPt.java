package com.tmall.wireless.tac.biz.processor.gcsfeeds;

import com.alibaba.cola.extension.Extension;
import com.google.common.collect.Maps;
import com.tmall.txcs.gs.framework.extensions.origindata.request.ContentOriginDataRequestExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextContent;
import com.tmall.txcs.gs.framework.model.constant.ScenarioConstant;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.txcs.gs.model.spi.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by yangqing.byq on 2021/4/18.
 */
@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.SCENARIO_GCS_FEEDS)
public class GcsfeedsContentOriginDataRequestExtPt implements ContentOriginDataRequestExtPt {
    @Override
    public RecommendRequest process(SgFrameworkContextContent sgFrameworkContextContent) {

        Map<String, String> params = Maps.newHashMap();
        RecommendRequest recommendRequest = new RecommendRequest();
        params.put("contentSetIdList", "7002");
        params.put("pageSize", "4");
        params.put("itemCountPerContent", "10");
        params.put("isFirstPage",String.valueOf(true));
        params.put("contentType", "7");
        params.put("contentSetSource", "intelligentCombinationItems");
//        params.put("exposureDataUserId", renderQuery.getUserInfo().getCna());
        recommendRequest.setParams(params);
        recommendRequest.setAppId(23198L);
        recommendRequest.setUserId(Optional.ofNullable(sgFrameworkContextContent).map(SgFrameworkContext::getUserDO).map(UserDO::getUserId).orElse(0L));
        return recommendRequest;
    }
}
