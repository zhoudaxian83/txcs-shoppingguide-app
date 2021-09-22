package com.tmall.wireless.tac.biz.processor.todayCrazyTab;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.*;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallCommonAldConstant;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Created from template by 进舟 on 2021-09-22 16:01:32.
 */

@SdkExtension(
        bizId = "supermarket",
        useCase = "b2c",
        scenario = "todayCrazyTab"
)
public class TodayCrazyTabContentOriginDataRequestBuildSdkExtPt extends Register implements ContentOriginDataRequestBuildSdkExtPt {

    //    https://tui.taobao.com/recommend?
//    appid=18697
//    &itemBusinessType=HalfDay
//    &itemSetIdList=61872,62352,62353,62354,61889,61890,62372,62339,62340,61894,62357,62344,62345,61883,61884,62348,62349
//    &logicAreaId=107
//    &pageSize=36
//    &rtHalfDayStoreId=236635411
//    &itemCountPerContent=1
//    &userId=2207255620071
//    &smAreaId=330110
//    &contentType=3

    @Override
    public RecommendRequest process(SgFrameworkContextContent sgFrameworkContextContent) {
        RecommendRequest recommendRequest = new RecommendRequest();
        recommendRequest.setAppId(18697L);
        recommendRequest.setUserId(Optional.of(sgFrameworkContextContent).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getUserDO).map(UserDO::getUserId).orElse(0L));

        Map<String, String> params = Maps.newHashMap();
        params.put("itemBusinessType", "B2C");
        List<Long> logicAreaId = Optional.of(sgFrameworkContextContent).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getLocParams).map(LocParams::getLogicIdByPriority).orElse(Lists.newArrayList());

        params.put("logicAreaId", Joiner.on(",").join(logicAreaId));
        params.put("smAreaId", Optional.of(sgFrameworkContextContent).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getLocParams).map(LocParams::getSmAreaId).map(Objects::toString).orElse("0"));
        params.put("contentType", "3");
        params.put("itemCountPerContent", "3");
        params.put("itemSetIdList", getItemSetIds(sgFrameworkContextContent));
        recommendRequest.setParams(params);
        return recommendRequest;
    }

    private String getItemSetIds(SgFrameworkContextContent sgFrameworkContextContent) {
        Context tacContext = sgFrameworkContextContent.getTacContext();
        RequestContext4Ald requestContext4Ald = (RequestContext4Ald) tacContext;

        Map<String, Object> aldContext = requestContext4Ald.getAldContext();
        Object staticScheduleData = aldContext.get(HallCommonAldConstant.STATIC_SCHEDULE_DATA);
        List<Map<String, Object>> staticScheduleDataList = (List<Map<String, Object>>)staticScheduleData;
        List<Long> longList = Lists.newArrayList();
        for (Map<String, Object> stringObjectMap : staticScheduleDataList) {
            Long itemSetId = (Long) stringObjectMap.get("itemSetId");
            longList.add(itemSetId);
        }

        return Joiner.on(",").join(longList);
    }
}
