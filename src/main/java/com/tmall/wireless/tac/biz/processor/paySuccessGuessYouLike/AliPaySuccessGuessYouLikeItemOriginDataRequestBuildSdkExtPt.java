package com.tmall.wireless.tac.biz.processor.paySuccessGuessYouLike;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.*;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.todaycrazy.utils.MapUtil;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.apache.commons.lang3.StringUtils;
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
        RecommendRequest tppRequest = new RecommendRequest();
        tppRequest.setLogResult(true);
        tppRequest.setAppId(APP_ID);
        Long userId = Optional.ofNullable(context)
                .map(SgFrameworkContext::getCommonUserParams)
                .map(CommonUserParams::getUserDO).map(UserDO::getUserId)
                .orElse(0L);
        Map<String,Object> contextParamsMap = Optional.of(context).map(SgFrameworkContext::getTacContext)
                .map(Context::getParams).orElse(Maps.newHashMap());

        //pmt参数拼接
        String pmtName = Optional.of(context).map(SgFrameworkContext::getCommonUserParams)
                .map(CommonUserParams::getPmtParams).map(PmtParams::getPmtName).orElse("guessULike");

        String pmtSource = Optional.of(context).map(SgFrameworkContext::getCommonUserParams)
                .map(CommonUserParams::getPmtParams).map(PmtParams::getPmtSource).orElse("sm_manager");

        String pageId = Optional.of(context).map(SgFrameworkContext::getCommonUserParams)
                .map(CommonUserParams::getPmtParams).map(PmtParams::getPageId).orElse("cainixihuan1");

        String moduleId = Optional.of(context).map(SgFrameworkContext::getCommonUserParams)
                .map(CommonUserParams::getPmtParams).map(PmtParams::getModuleId).orElse("153");

        LocParams locParams = Optional.of(context).map(SgFrameworkContext::getCommonUserParams)
                .map(CommonUserParams::getLocParams).orElse(new LocParams());

        Long smAreaId = Optional.of(locParams).map(LocParams::getSmAreaId).get();

        String regionCode = locParams.getRegionCode() != null ? locParams.getRegionCode().toString() : "";

         String logicAreaId = Joiner.on(",").join(Optional.ofNullable(locParams)
                 .map(LocParams::getLogicIdByPriority)
                 .orElse(Lists.newArrayList()));


        String index = "0";
        String pageSize = MapUtil.getStringWithDefault(contextParamsMap, "pageSize", "20");
        String isFirstPage = MapUtil.getStringWithDefault(contextParamsMap, "isFirstPage", "true");
        if (!"true".equals(isFirstPage)) {
            isFirstPage = "false";
        }
        String itemBusinessType = MapUtil.getStringWithDefault(
                contextParamsMap, "itemBusinessType", "B2C,OneHour,HalfDay,NextDay");
        String exposureDataUserId = Optional.of(context).map(SgFrameworkContext::getCommonUserParams)
                .map(CommonUserParams::getUserDO).map(UserDO::getCna).orElse("");
        if (StringUtils.isEmpty(exposureDataUserId)) {
            exposureDataUserId = String.valueOf(userId);
        }
        tppRequest.setUserId(userId);
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
        params.put("regionCode", regionCode);
        params.put("moduleId", moduleId);
        params.put("level1Id", moduleId);
        tacLogger.info("index值：" + index + "pageSize值" + pageSize);
        params.put("frontIndex", index);
        params.put("itemBusinessType", itemBusinessType);
        params.put("honehourStoreId", "0");
        params.put("isFirstPage", isFirstPage);
        params.put("exposureDataUserId", exposureDataUserId);

        tppRequest.setParams(params);

        tppRequest.setParams(params);
        String requestLog = "https://tui.taobao.com/recommend?appid=" + tppRequest.getAppId() + "&" +
                Joiner.on("&").withKeyValueSeparator("=").join(tppRequest.getParams());
        tacLogger.info("TPP_REQUEST: " + requestLog);
        HadesLogUtil.stream(ScenarioConstantApp.PAY_FOR_SUCCESS_GUESS_YOU_LIKE)
                .kv("AliPaySuccessGuessYouLikeItemOriginDataRequestBuildSdkExtPt","tppRequest")
                .kv("tppRequest", JSON.toJSONString(tppRequest))
                .info();
        return tppRequest;
    }
}
