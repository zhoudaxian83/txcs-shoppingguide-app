package com.tmall.wireless.tac.biz.processor.alipay.service.ext.middlepage;


import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.*;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.AldService;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.AliPayServiceImpl;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@SdkExtension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C
        , scenario = ScenarioConstantApp.SCENARIO_ALI_PAY_MIDDLE_PAGE
)
public class AliPayMiddleItemOriginDataRequestBuildSdkExtPt extends Register implements ItemOriginDataRequestBuildSdkExtPt {
    private static final Long APPID = 27244L;

//    https://tui.taobao.com/recommend?appid=27244&regionCode=108&pageSize=5&index=1&itemSets=crm_378428&commerce=B2C&smAreaId=130300

    @Autowired
    AldService aldService;


    @Override
    public RecommendRequest process(SgFrameworkContextItem sgFrameworkContextItem) {
        RecommendRequest tppRequest = new RecommendRequest();
        tppRequest.setAppId(APPID);

        GeneralItem aldData = aldService.getAldData(
                Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getUserDO).map(UserDO::getUserId).orElse(0L),
                Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getLocParams).map(LocParams::getSmAreaId).map(String::valueOf).orElse("0")
        );
        aldData.getLong(AliPayServiceImpl.hookItemSetAldKey);
        aldData.getLong(AliPayServiceImpl.itemSetAldKey);
        List<String> itemSetIds = Lists.newArrayList(
                aldData.getLong(AliPayServiceImpl.hookItemSetAldKey),
                aldData.getLong(AliPayServiceImpl.itemSetAldKey))
                .stream().map(i -> "crm_" + i).collect(Collectors.toList());

        Map<String, String> params = Maps.newHashMap();
        params.put("pageSize", "20");
        params.put("itemSets", Joiner.on(",").join(itemSetIds));
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
