package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.*;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Created from template by 罗俊冲 on 2021-09-15 17:54:58.
 */

@SdkExtension(
        bizId = "supermarket",
        useCase = "b2c",
        scenario = "TodayCrazyRecommendTab"
)
public class TodayCrazyRecommendTabItemOriginDataRequestBuildSdkExtPt extends Register implements ItemOriginDataRequestBuildSdkExtPt {
    @Autowired
    TacLogger tacLogger;

    @Override
    public RecommendRequest process(SgFrameworkContextItem sgFrameworkContextItem) {
        return this.buildTppParam(sgFrameworkContextItem);
    }

    private RecommendRequest buildTppParam(SgFrameworkContextItem sgFrameworkContextItem) {
        RecommendRequest recommendRequest = new RecommendRequest();
        Map<String, String> params = Maps.newHashMap();
        params.put("pageSize", Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getUserPageInfo).map(PageInfoDO::getPageSize).map(Objects::toString).orElse("20"));
        params.put("index", Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getUserPageInfo).map(PageInfoDO::getIndex).map(Objects::toString).orElse("0"));
        params.put("smAreaId", Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getLocParams).map(LocParams::getSmAreaId).map(Objects::toString).orElse("330100"));
        recommendRequest.setAppId(21895L);
        recommendRequest.setUserId(Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getUserDO).map(UserDO::getUserId).orElse(0L));
        recommendRequest.setParams(params);
        recommendRequest.setLogResult(true);
        tacLogger.info("recommendRequest_:" + JSON.toJSONString(recommendRequest));
        return recommendRequest;
    }
}
