package com.tmall.wireless.tac.biz.processor.paySuccessGuessYouLike;

import com.alibaba.common.lang.StringUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.tmall.aself.shoppingguide.client.loc.util.AddressUtil;
import com.tmall.aselfcommon.model.oc.domain.LogicalArea;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.*;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.todaycrazy.utils.MapUtil;
import com.tmall.wireless.tac.client.domain.UserInfo;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.tmall.wireless.tac.client.domain.Context;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@SdkExtension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C
        , scenario = ScenarioConstantApp.PAY_FOR_SUCCESS_GUESS_YOU_LIKE
)
public class AliPaySuccessGuessYouLikeItemOriginDataRequestBuildSdkExtPt extends Register
        implements ItemOriginDataRequestBuildSdkExtPt {

    private static final Long APP_ID = 27989L;
    @Autowired
    TacLoggerImpl tacLogger;

    //https://tuipre.taobao.com/recommend?appid=27989&pmtName=guessULike&pageSize=1&pmtSource=sm_manager
    // &type=cainixihuan1&smAreaId=411002&logicAreaId=111&index=10&pageId=cainixihuan1&enlargeCainixihuanToHigher=500
    // &regionCode=111&level1Id=153&moduleId=153&frontIndex=10&itemBusinessType=B2C,OneHour,HalfDay,NextDay
    // &honehourStoreId=0&isFirstPage=true&exposureDataUserId=2443459148

    //参数文档：https://yuque.antfin.com/docs/share/4187d39f-b7d7-4650-8455-3df0426e3c30?#
    @Override
    public RecommendRequest process(SgFrameworkContextItem context) {
        tacLogger.info("=================tacLogger+ 已进入tpp参数组装==================");
        tacLogger.info("sgFrameworkContextItem信息" + context);
        RecommendRequest tppRequest = new RecommendRequest();
        tppRequest.setAppId(APP_ID);
        Long userId = Optional.of(context).
                map(SgFrameworkContext::getCommonUserParams).
                map(CommonUserParams::getUserDO).map(UserDO::getUserId).orElse(0L);
        tacLogger.info("userId：" + userId);
        tacLogger.info("context内容:" + context.toString());
        tacLogger.info("context中requestParam内容" + context.getRequestParams());
        tacLogger.info("tacContext内容：" + context.getTacContext());
        tppRequest.setUserId(userId);
        Map<String, String> params = Maps.newHashMap();
        params.put("pageSize", Optional.of(context).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getUserPageInfo).map(PageInfoDO::getPageSize).map(Objects::toString).orElse("20"));
        params.put("index", Optional.of(context).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getUserPageInfo).map(PageInfoDO::getIndex).map(Objects::toString).orElse("0"));
        params.put("type", "cartsRecommend");
        params.put("smAreaId", "440106");
        params.put("userid", userId.toString());
        params.put("closeSls", "0");
        params.put("logicAreaId", "109");
        params.put("ayStoreId", "236839048");
        params.put("detailItemIdList", "528348289267,565270259153,565032189700,599138529883,610201548194,20739895092,606876101370,15024857415,559321202351,&580864498884,643424111236");
        params.put("itemBusinessType", "B2C,OneHour,HalfDay");
        params.put("itemBusinessType", "B2C,OneHour,HalfDay");
        params.put("isFirstPage", "true");
        params.put("appid", APP_ID.toString());
        tppRequest.setParams(params);
        tacLogger.info("=================tacLogger+ 已完成tpp参数组装==================");
        return tppRequest;
    }



}
