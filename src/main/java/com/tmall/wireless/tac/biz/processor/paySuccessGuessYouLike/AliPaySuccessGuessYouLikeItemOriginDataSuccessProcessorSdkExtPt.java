package com.tmall.wireless.tac.biz.processor.paySuccessGuessYouLike;

import com.alibaba.fastjson.JSON;
import com.taobao.tair.ResultCode;
import com.taobao.tair.impl.mc.MultiClusterTairManager;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.biz.extensions.item.origindata.DefaultItemOriginDataSuccessProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataSuccessProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.OriginDataProcessRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.*;
import com.tmall.txcs.gs.spi.recommend.TairFactorySpi;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SdkExtension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C
        , scenario = ScenarioConstantApp.PAY_FOR_SUCCESS_GUESS_YOU_LIKE
)
public class AliPaySuccessGuessYouLikeItemOriginDataSuccessProcessorSdkExtPt
        extends DefaultItemOriginDataSuccessProcessorSdkExtPt implements ItemOriginDataSuccessProcessorSdkExtPt {


    @Autowired
    TairFactorySpi tairFactorySpi;

    private static final int nameSpace = 184;

    @Override
    public OriginDataDTO<ItemEntity> process(OriginDataProcessRequest originDataProcessRequest) {

        OriginDataDTO<ItemEntity> itemEntityOriginDataDTO = super.process(originDataProcessRequest);

        //在解析成功扩展点将数据存入tair
        if (itemEntityOriginDataDTO != null &&  itemEntityOriginDataDTO.getResult() != null) {

            String sKey = Optional.of(originDataProcessRequest).map(OriginDataProcessRequest::getSgFrameworkContextItem)
                    .map(SgFrameworkContext::getCommonUserParams)
                    .map(CommonUserParams::getPmtParams).map(PmtParams::getModuleId).orElse("153");

            MultiClusterTairManager multiClusterTairManager = tairFactorySpi.getOriginDataFailProcessTair()
                    .getMultiClusterTairManager();

            List<Long> collect = itemEntityOriginDataDTO.getResult().stream()
                    .map(e -> e.getItemId()).distinct().collect(Collectors.toList());

            ResultCode labelSceneResult = multiClusterTairManager.prefixPut(nameSpace,
                    ScenarioConstantApp.PAY_FOR_SUCCESS_GUESS_YOU_LIKE, sKey,
                    JSON.toJSONString(collect), 0, 0);

            HadesLogUtil.stream(ScenarioConstantApp.PAY_FOR_SUCCESS_GUESS_YOU_LIKE)
                    .kv("AliPaySuccessGuessYouLikeItemOriginDataSuccessProcessorSdkExtPt", "process")
                    .kv("isSuccess", "true")
                    .kv("addTairSuccess", String.valueOf(labelSceneResult.isSuccess()))
                    .info();
        }

        return itemEntityOriginDataDTO;
    }
}
