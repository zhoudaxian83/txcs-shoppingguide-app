package com.tmall.wireless.tac.biz.processor.alipay.service.ext.middlepage;


import com.alipay.tradecsa.common.service.spi.request.MiddlePageClientRequestDTO;
import com.alipay.tradecsa.common.service.spi.request.MiddlePageSPIRequest;
import com.google.common.collect.Lists;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataPostProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.OriginDataProcessRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.*;
import com.tmall.txcs.gs.model.item.O2oType;
import com.tmall.wireless.tac.biz.processor.alipay.AlipayMiddlePageHandler;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.domain.Context;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.Op;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.tmall.wireless.tac.biz.processor.alipay.service.impl.AliPayServiceImpl.TOP_ITEM_ID_KEY;


@SdkExtension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C
        , scenario = ScenarioConstantApp.SCENARIO_ALI_PAY_MIDDLE_PAGE
)
public class AlipayMiddleItemOriginDataPostProcessorSdkExtPt extends Register implements ItemOriginDataPostProcessorSdkExtPt {

    @Override
    public OriginDataDTO<ItemEntity> process(OriginDataProcessRequest originDataProcessRequest) {

        Long topItemId = getTopItemId(originDataProcessRequest);

        if (topItemId <= 0) {
            return originDataProcessRequest.getItemEntityOriginDataDTO();
        }

        List<ItemEntity> originList = Optional.of(originDataProcessRequest).map(OriginDataProcessRequest::getItemEntityOriginDataDTO).map(OriginDataDTO::getResult).orElse(Lists.newArrayList());

        if (CollectionUtils.isEmpty(originList)) {
            return originDataProcessRequest.getItemEntityOriginDataDTO();
        }
        Integer index = Optional.of(originDataProcessRequest)
                .map(OriginDataProcessRequest::getSgFrameworkContextItem)
                .map(SgFrameworkContext::getCommonUserParams)
                .map(CommonUserParams::getUserPageInfo)
                .map(PageInfoDO::getIndex).orElse(0);

        List<ItemEntity> result = originList.stream().filter(itemEntity -> !itemEntity.getItemId().equals(topItemId)).collect(Collectors.toList());

        if (index == 0) {
            ItemEntity itemEntity = getItemEntity(topItemId);
            result.add(0, itemEntity);
        }

        OriginDataDTO<ItemEntity> itemEntityOriginDataDTO = originDataProcessRequest.getItemEntityOriginDataDTO();
        itemEntityOriginDataDTO.setResult(result);
        return itemEntityOriginDataDTO;

    }

    private ItemEntity getItemEntity(Long topItemId) {
        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setItemId(topItemId);
        itemEntity.setBizType(com.tmall.txcs.gs.model.item.BizType.SM.getCode());
        itemEntity.setO2oType(com.tmall.txcs.gs.model.item.O2oType.B2C.name());
        itemEntity.setBusinessType(O2oType.B2C.name());
        return itemEntity;
    }

    private Long getTopItemId(OriginDataProcessRequest originDataProcessRequest) {

        Object req = Optional.of(originDataProcessRequest)
                .map(OriginDataProcessRequest::getSgFrameworkContextItem)
                .map(SgFrameworkContext::getTacContext)
                .map(Context::getParams)
                .map(m -> m.get(AlipayMiddlePageHandler.PARAM_KEY)).orElse(null);
        if (!(req instanceof MiddlePageSPIRequest)) {
            return 0L;
        }

        return queryTopItemId((MiddlePageSPIRequest) req);
    }

    private Long queryTopItemId(MiddlePageSPIRequest req) {
        return Optional.of(req).map(MiddlePageSPIRequest::getMiddlePageClientRequestDTO).map(MiddlePageClientRequestDTO::getItemParams)
                .map(m -> m.get(TOP_ITEM_ID_KEY)).filter(StringUtils::isNumeric).map(Long::valueOf).orElse(0L);
    }
}
