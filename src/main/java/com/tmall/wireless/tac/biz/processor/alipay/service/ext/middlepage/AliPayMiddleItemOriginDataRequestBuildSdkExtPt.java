package com.tmall.wireless.tac.biz.processor.alipay.service.ext.middlepage;


import com.google.common.collect.Maps;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.*;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;

import java.util.Map;
import java.util.Optional;


@SdkExtension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C
        , scenario = ScenarioConstantApp.SCENARIO_ALI_PAY_MIDDLE_PAGE
)
public class AliPayMiddleItemOriginDataRequestBuildSdkExtPt extends Register implements ItemOriginDataRequestBuildSdkExtPt {
    private static final Long APPID = 27244L;

//    https://tui.taobao.com/recommend?appid=27244&regionCode=108&pageSize=5&index=1&itemSets=crm_378428&commerce=B2C&smAreaId=130300

    @Override
    public RecommendRequest process(SgFrameworkContextItem sgFrameworkContextItem) {
        RecommendRequest tppRequest = new RecommendRequest();
        tppRequest.setAppId(APPID);

        Map<String, String> params = Maps.newHashMap();
        params.put("pageSize", "20");
        params.put("itemSets",  "crm_5233");
        params.put("smAreaId", Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams)
                .map(CommonUserParams::getLocParams).map(LocParams::getSmAreaId).orElse(0L).toString());
        params.put("index", "0");
        tppRequest.setUserId(Optional.of(sgFrameworkContextItem).
                map(SgFrameworkContext::getCommonUserParams).
                map(CommonUserParams::getUserDO).map(UserDO::getUserId).orElse(0L));
        params.put("commerce", "B2C");

        params.put("regionCode", "107");

        tppRequest.setParams(params);
        return tppRequest;
    }
}
