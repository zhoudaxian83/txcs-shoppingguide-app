package com.tmall.wireless.tac.biz.processor.guessYourLikeShopCart4;

import com.alibaba.fastjson.JSON;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.taobao.tair.ResultCode;
import com.taobao.tair.impl.mc.MultiClusterTairManager;
import com.tmall.tcls.gs.sdk.biz.extensions.item.origindata.DefaultItemOriginDataSuccessProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataSuccessProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.OriginDataProcessRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.*;
import com.tmall.txcs.gs.spi.recommend.TairFactorySpi;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SdkExtension(
        bizId = "supermarket",
        useCase = "b2c",
        scenario = "guessYourLikeShopCart4"
)
public class GuessYourLikeShopCart4ItemOriginDataSuccessProcessorSdkExtPt
        extends DefaultItemOriginDataSuccessProcessorSdkExtPt implements ItemOriginDataSuccessProcessorSdkExtPt {


    @Autowired
    TairFactorySpi tairFactorySpi;

    private static final int nameSpace = 184;
    @Autowired
    TacLoggerImpl tacLogger;

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
                    ScenarioConstantApp.GUESS_YOU_LIKE_SHOP_CART, sKey,
                    JSON.toJSONString(collect), 0, 0);

            if (labelSceneResult.isSuccess()) {
                tacLogger.info("插入tair数据成功");
            } else {
                tacLogger.info("插入tair数据失败");
            }
        }

        return itemEntityOriginDataDTO;
    }
}
