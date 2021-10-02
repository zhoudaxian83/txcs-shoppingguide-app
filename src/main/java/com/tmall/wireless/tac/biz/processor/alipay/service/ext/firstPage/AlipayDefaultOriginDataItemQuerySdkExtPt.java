package com.tmall.wireless.tac.biz.processor.alipay.service.ext.firstPage;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.excute.ExtensionPointExecutor;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataResponseConvertSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.OriginDataItemQuerySdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ResponseConvertRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.*;
import com.tmall.tmallwireless.tac.spi.context.SPIResult;
import com.tmall.wireless.store.spi.recommend.RecommendSpi;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.AldService;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.AliPayServiceImpl;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.event.ItemListener;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@SdkExtension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C
        , scenario = ScenarioConstantApp.SCENARIO_ALI_PAY_FIRST_PAGE
)
public class AlipayDefaultOriginDataItemQuerySdkExtPt extends Register implements OriginDataItemQuerySdkExtPt {
    @Autowired
    RecommendSpi recommendSpi;
    private static final Long APPID = 27244L;

    @Autowired
    ExtensionPointExecutor extensionPointExecutor;
    @Autowired
    AldService aldService;

    public static final int ITEM_SIZE = 20;
    public static final int ITEM_SIZE_HOOK = 10;

    @Override
    public Flowable<OriginDataDTO<ItemEntity>> process(SgFrameworkContextItem sgFrameworkContextItem) {


        GeneralItem aldData = aldService.getAldData(
                Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getUserDO).map(UserDO::getUserId).orElse(0L),
                Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getLocParams).map(LocParams::getSmAreaId).map(String::valueOf).orElse("0")
        );

        RecommendRequest recommendRequest = buildRequest(sgFrameworkContextItem, aldData.getLong(AliPayServiceImpl.itemSetAldKey), ITEM_SIZE);
        RecommendRequest recommendRequestHook = buildRequest(sgFrameworkContextItem, aldData.getLong(AliPayServiceImpl.hookItemSetAldKey), ITEM_SIZE_HOOK);

        OriginDataDTO<ItemEntity> itemEntityOriginDataDTOError = new OriginDataDTO<>();

        Flowable<OriginDataDTO<ItemEntity>> originDataDTOFlowable = recommendSpi.recommendAsync(recommendRequest)
                .map(stringSPIResult -> convertResult(stringSPIResult, sgFrameworkContextItem))
                .onErrorReturn(throwable -> itemEntityOriginDataDTOError);
        Flowable<OriginDataDTO<ItemEntity>> originDataDTOFlowableHook = recommendSpi.recommendAsync(recommendRequestHook)
                .map(stringSPIResult -> convertResult(stringSPIResult, sgFrameworkContextItem))
                .onErrorReturn(throwable -> itemEntityOriginDataDTOError);

        return Flowable.zip(originDataDTOFlowable, originDataDTOFlowableHook, this::merge);

    }

    private OriginDataDTO<ItemEntity> merge(OriginDataDTO<ItemEntity> itemEntityOriginDataDTO, OriginDataDTO<ItemEntity> itemEntityOriginDataDTOHook) {
        if (itemEntityOriginDataDTO == null || CollectionUtils.isEmpty(itemEntityOriginDataDTO.getResult())) {
            return itemEntityOriginDataDTOHook;
        }
        if (itemEntityOriginDataDTOHook == null || CollectionUtils.isEmpty(itemEntityOriginDataDTOHook.getResult())) {
            return itemEntityOriginDataDTO;
        }
        List<ItemEntity> itemEntityList = Lists.newArrayList();
        itemEntityList.addAll(random(itemEntityOriginDataDTOHook.getResult(), 1));
        itemEntityList.addAll(random(itemEntityOriginDataDTO.getResult(), 4));

        itemEntityOriginDataDTOHook.setResult(itemEntityList);
        return itemEntityOriginDataDTOHook;
    }

    private List<ItemEntity> random(List<ItemEntity> result, int size) {
        if (result.size() <= size) {
            return result;
        }
        Random random = new Random();
        int i = random.nextInt(result.size() - size);
        if (i + size > result.size()) {
            return result.subList(0, size);
        }
        return result.subList(i, i + size);
    }

    private OriginDataDTO<ItemEntity> convertResult(SPIResult<String> stringSPIResult, SgFrameworkContextItem sgFrameworkContextItem) {
        OriginDataDTO<ItemEntity> originDataDTOError = new OriginDataDTO<ItemEntity>();
        originDataDTOError.setSuccess(false);
        if (!stringSPIResult.isSuccess()) {
            originDataDTOError.setErrorCode(stringSPIResult.getMsgCode());
            originDataDTOError.setErrorMsg(stringSPIResult.getMsgInfo());
            return originDataDTOError;
        } else if (StringUtils.isEmpty(stringSPIResult.getData())) {
            originDataDTOError.setErrorCode("RECOMMEND_SPI_RESPONSE_IS_EMPTY");
            originDataDTOError.setErrorMsg("RECOMMEND_SPI_RESPONSE_IS_EMPTY");
            return originDataDTOError;
        } else {
            ResponseConvertRequest responseConvertRequest = new ResponseConvertRequest();
            responseConvertRequest.setResponse(stringSPIResult.getData());
            responseConvertRequest.setSgFrameworkContextItem(sgFrameworkContextItem);
            return extensionPointExecutor.execute(ItemOriginDataResponseConvertSdkExtPt.class, sgFrameworkContextItem.getBizScenario(), (pt) -> pt.process0(responseConvertRequest));
        }
    }


    private RecommendRequest buildRequest(SgFrameworkContextItem sgFrameworkContextItem, Long itemSerId, Integer pageSize) {

        RecommendRequest tppRequest = new RecommendRequest();
        tppRequest.setAppId(APPID);

        Map<String, String> params = Maps.newHashMap();
        params.put("pageSize", String.valueOf(pageSize));
        params.put("itemSets",  "crm_" + itemSerId);
        params.put("smAreaId", Optional.ofNullable(sgFrameworkContextItem).map(com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContext::getCommonUserParams)
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
