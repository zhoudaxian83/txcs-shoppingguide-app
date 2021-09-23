package com.tmall.wireless.tac.biz.processor.paySuccessGuessYouLike;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataResponseConvertSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ResponseConvertRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContext;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.todaycrazy.utils.MapUtil;
import com.tmall.wireless.tac.biz.productpackage.recommendold.TppConvertUtil;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Optional;

@SdkExtension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C
        , scenario = ScenarioConstantApp.PAY_FOR_SUCCESS_GUESS_YOU_LIKE
)
public class AliPaySuccessGuessYouLikeItemOriginDataResponseConvertSdkExtPt extends Register implements ItemOriginDataResponseConvertSdkExtPt {

    @Autowired
    TacLoggerImpl tacLogger;
    public static final String INDEX_KEY = "index";

    @Override
    public OriginDataDTO<ItemEntity> process(ResponseConvertRequest responseConvertRequest) {
        Map<String,Object> requestParamsMap = Optional.of(responseConvertRequest).map(ResponseConvertRequest::getSgFrameworkContextItem)
                .map(SgFrameworkContext::getRequestParams).orElse(Maps.newHashMap());
        JSONObject jsonObject = JSON.parseObject(responseConvertRequest.getResponse());

        if (jsonObject.containsKey(INDEX_KEY)) {
            String requestIndex = MapUtil.getStringWithDefault(requestParamsMap, INDEX_KEY, "1");
            String responseIndex =  String.valueOf(jsonObject.get(INDEX_KEY));
            if (!requestIndex.equals(responseIndex)) {
                jsonObject.put(INDEX_KEY, requestIndex);
            }
            responseConvertRequest.setResponse(JSON.toJSONString(jsonObject));
        }
        tacLogger.info("responseConvertRequest信息：" + JSON.toJSONString(responseConvertRequest));
        return TppConvertUtil.processItemEntity(responseConvertRequest.getResponse());
    }
}
