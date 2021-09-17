package com.tmall.wireless.tac.biz.processor.paySuccessGuessYouLike;

import com.alibaba.common.lang.StringUtil;
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
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import com.tmall.wireless.tac.client.domain.Context;
import java.util.Map;
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
        tacLogger.info("context：" + context.toString());
        RecommendRequest tppRequest = new RecommendRequest();
        tppRequest.setAppId(APP_ID);

        Map<String,Object> contextParamsMap = Optional.of(context).map(SgFrameworkContext::getTacContext).map(Context::getParams)
                .orElse(Maps.newHashMap());
        String csa = MapUtil.getStringWithDefault(contextParamsMap, "csa", "");

        String pmtName = MapUtil.getStringWithDefault(contextParamsMap, "pmtName", "guessULike");
        String pmtSource = MapUtil.getStringWithDefault(contextParamsMap, "pmtSource", "sm_manager");
        Long smAreaId = MapUtil.getLongWithDefault(contextParamsMap, "smAreaId", 0L);
        if (smAreaId == 0L && StringUtil.isNotEmpty(csa)) {
            smAreaId = LogicalArea.parseByCode(AddressUtil.parseCSA(csa).getRegionCode()).getCoreCityCode();
        }
        String logicAreaId = AddressUtil.parseCSA(csa).getRegionCode();
        String index = MapUtil.getStringWithDefault(contextParamsMap, "index", "0");
        String pageSize = MapUtil.getStringWithDefault(contextParamsMap, "pageSize", "20");
        String pageId = MapUtil.getStringWithDefault(contextParamsMap, "pageId", "cainixihuan1");
        String level1Id = MapUtil.getStringWithDefault(contextParamsMap, "level1Id", "");
        String itemBusinessType = MapUtil.getStringWithDefault(contextParamsMap, "itemBusinessType", "B2C,OneHour,HalfDay,NextDay");
        String isFirstPage = MapUtil.getStringWithDefault(contextParamsMap, "isFirstPage", "true");
        String exposureDataUserId = MapUtil.getStringWithDefault(contextParamsMap, "exposureDataUserId", "");

        Map<String, String> params = Maps.newHashMap();
        params.put("appid", String.valueOf(APP_ID));
        params.put("pmtName", pmtName);
        params.put("pageSize", pageSize);
        params.put("pmtSource", pmtSource);
        params.put("type", "cainixihuan1");
        params.put("smAreaId", String.valueOf(smAreaId));
        params.put("logicAreaId", logicAreaId);
        params.put("index", index);
        params.put("pageId", pageId);
        params.put("enlargeCainixihuanToHigher", "500");
        params.put("regionCode", logicAreaId);
        params.put("level1Id", level1Id);
        params.put("moduleId", level1Id);
        params.put("frontIndex", index);
        params.put("itemBusinessType", itemBusinessType);
        params.put("honehourStoreId", "0");
        params.put("isFirstPage", isFirstPage);
        params.put("exposureDataUserId", exposureDataUserId);

        tacLogger.info("tpp请求参数：" + params.toString());
        tppRequest.setParams(params);
        tacLogger.info("=================tacLogger+ 已完成tpp参数组装==================");
        return tppRequest;
    }



}
