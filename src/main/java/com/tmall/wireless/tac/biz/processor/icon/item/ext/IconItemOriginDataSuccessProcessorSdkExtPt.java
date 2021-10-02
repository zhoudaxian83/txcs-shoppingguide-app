package com.tmall.wireless.tac.biz.processor.icon.item.ext;

import com.alibaba.fastjson.JSON;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataSuccessProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.OriginDataProcessRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContext;
import com.tmall.wireless.store.spi.tair.TairSpi;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.icon.item.ItemRecommendService;
import com.tmall.wireless.tac.biz.processor.icon.item.ItemRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@SdkExtension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C
        , scenario = ScenarioConstantApp.ICON_ITEM
)
public class IconItemOriginDataSuccessProcessorSdkExtPt extends Register implements ItemOriginDataSuccessProcessorSdkExtPt {
    @Autowired
    TairSpi tairSpi;

    Logger LOGGER = LoggerFactory.getLogger(IconItemOriginDataFailProcessorSdkExtPt.class);


    @Override
    public OriginDataDTO<ItemEntity> process(OriginDataProcessRequest originDataProcessRequest) {

        try {
            ItemRequest itemRequest = (ItemRequest) Optional.of(originDataProcessRequest)
                    .map(OriginDataProcessRequest::getSgFrameworkContextItem)
                    .map(SgFrameworkContext::getTacContext)
                    .map(c -> c.get(ItemRecommendService.ITEM_REQUEST_KEY))
                    .orElse(null);
            if (itemRequest == null) {
                return originDataProcessRequest.getItemEntityOriginDataDTO();
            }


            String tairKey = IconItemOriginDataFailProcessorSdkExtPt.buildTairKey(itemRequest);

            LOGGER.info("IconItemOriginDataSuccessProcessorSdkExtPt_tairKey:{}", tairKey);

            tairSpi.put(
                    IconItemOriginDataFailProcessorSdkExtPt.SHOPPING_GUIDE_TAIR_USER_NAME,
                    IconItemOriginDataFailProcessorSdkExtPt.SHOPPING_GUIDE_NAME_SPACE,
                    tairKey,
                    JSON.toJSONString(originDataProcessRequest.getItemEntityOriginDataDTO().getResult()));


            return originDataProcessRequest.getItemEntityOriginDataDTO();
        } catch (Exception e) {
            LOGGER.error("IconItemOriginDataSuccessProcessorSdkExtPt error", e);
            return originDataProcessRequest.getItemEntityOriginDataDTO();
        }

    }
}
