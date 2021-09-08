package com.tmall.wireless.tac.biz.processor.icon.level2.ext;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.*;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.icon.level2.Level2RecommendService;
import com.tmall.wireless.tac.biz.processor.icon.level2.Level2Request;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@SdkExtension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C
        , scenario = ScenarioConstantApp.ICON_CONTENT_LEVEL2
)
public class IconLevel2ContentOriginDataRequestBuildSdkExtPt extends Register implements ContentOriginDataRequestBuildSdkExtPt {

    @Override
    public RecommendRequest process(SgFrameworkContextContent sgFrameworkContextContent) {
        Level2Request level2Request =(Level2Request) Optional.of(sgFrameworkContextContent).map(SgFrameworkContext::getTacContext).map(c -> c.get(Level2RecommendService.level2RequestKey)).orElse(null);
        Preconditions.checkArgument(level2Request != null);
        RecommendRequest recommendRequest = new RecommendRequest();
        recommendRequest.setAppId(18697L);
        recommendRequest.setUserId(Optional.of(sgFrameworkContextContent).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getUserDO).map(UserDO::getUserId).orElse(0L));
        Map<String, String> params = Maps.newHashMap();
        recommendRequest.setParams(params);

        params.put("pmtName", "icon");
        params.put("pmtSource", "sm_manager");
        params.put("pageId", level2Request.getLevel1Id() + "asa");
        params.put("itemBusinessType", "B2C,OneHour,HalfDay,NextDay");
        params.put("contentType", "1");
        params.put("rtNextDayStoreId", Optional.of(sgFrameworkContextContent).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getLocParams).map(LocParams::getRtNextDayStoreId).map(Object::toString).orElse("0"));
        params.put("rtHalfDayStoreId", Optional.of(sgFrameworkContextContent).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getLocParams).map(LocParams::getRtHalfDayStoreId).map(Object::toString).orElse("0"));
        params.put("rtOneHourStoreId",  Optional.of(sgFrameworkContextContent).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getLocParams).map(LocParams::getRt1HourStoreId).map(Object::toString).orElse("0"));

        return recommendRequest;
    }
}
