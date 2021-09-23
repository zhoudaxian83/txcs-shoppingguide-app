package com.tmall.wireless.tac.biz.processor.paySuccessGuessYouLike;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.tmall.tcls.gs.sdk.biz.extensions.item.filter.DefaultItemFilterSdkExtPt;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.framework.extensions.item.filter.ItemFilterRequest;
import com.tmall.tcls.gs.sdk.framework.extensions.item.filter.ItemFilterSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ResponseConvertRequest;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.model.context.CommonUserParams;
import com.tmall.tcls.gs.sdk.framework.model.context.LocParams;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContext;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextItem;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.todaycrazy.utils.MapUtil;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Optional;

@SdkExtension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C
        , scenario = ScenarioConstantApp.PAY_FOR_SUCCESS_GUESS_YOU_LIKE
)
public class AliPaySuccessGuessYouLikeItemFilterSdkExtPt extends DefaultItemFilterSdkExtPt implements ItemFilterSdkExtPt {

    @Autowired
    TacLoggerImpl tacLogger;

    public static final String INDEX_KEY = "index";
    @Override
    public SgFrameworkResponse<ItemEntityVO> process(ItemFilterRequest itemFilterRequest) {

        Map<String,Object> requestParamsMap = Optional.of(itemFilterRequest).map(ItemFilterRequest::getSgFrameworkContextItem)
                .map(SgFrameworkContext::getRequestParams).orElse(Maps.newHashMap());
        int requestIndex = MapUtil.getIntWithDefault(requestParamsMap, INDEX_KEY, 1);
        int responseIndex = Optional.of(itemFilterRequest)
                .map(ItemFilterRequest::getEntityVOSgFrameworkResponse)
                .map(SgFrameworkResponse::getIndex)
                .orElse(1);

        tacLogger.info("requestIndex：" + requestIndex + "responseIndex:" + responseIndex);
        if (requestIndex != responseIndex) {
            itemFilterRequest.getEntityVOSgFrameworkResponse().setIndex(requestIndex);
            tacLogger.info("index不一致修改index为：" + requestIndex);
        }
        SgFrameworkResponse<ItemEntityVO> process = super.process(itemFilterRequest);

        return process;
    }
}
