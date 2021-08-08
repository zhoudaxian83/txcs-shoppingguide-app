package com.tmall.wireless.tac.biz.processor.newTemplate;

import com.google.common.collect.Maps;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkPackage;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.*;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;

import java.util.Map;
import java.util.Optional;

/**
 * Created by yangqing.byq on 2021/8/8.
 */

@SdkExtension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
useCase = ScenarioConstantApp.LOC_TYPE_B2C
//        , scenario = "test"
)
@SdkPackage(packageName = "test_package")
public class TestItemOriginDataRequestBuildExtPt extends Register implements ItemOriginDataRequestBuildSdkExtPt {
    private static final Long APPID = 25385L;

    @Override
    public RecommendRequest process(SgFrameworkContextItem sgFrameworkContextItem) {
        RecommendRequest tppRequest = new RecommendRequest();
        tppRequest.setAppId(APPID);

        Map<String, String> params = Maps.newHashMap();
        params.put("pageSize", "20");
        params.put("itemSets",  "crm_5233");
        params.put("smAreaId", Optional.ofNullable(sgFrameworkContextItem).map(com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContext::getCommonUserParams)
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
