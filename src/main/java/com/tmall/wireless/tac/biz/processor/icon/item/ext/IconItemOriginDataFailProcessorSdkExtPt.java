package com.tmall.wireless.tac.biz.processor.icon.item.ext;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataFailProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.OriginDataProcessRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContext;
import com.tmall.wireless.store.spi.tair.TairSpi;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.icon.item.ItemRecommendService;
import com.tmall.wireless.tac.biz.processor.icon.item.ItemRequest;
import com.tmall.wireless.tac.biz.processor.mmc.handler.MmcItemQueryHandler;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@SdkExtension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C
        , scenario = ScenarioConstantApp.ICON_ITEM
)
public class IconItemOriginDataFailProcessorSdkExtPt extends Register implements ItemOriginDataFailProcessorSdkExtPt {

    public static final String SHOPPING_GUIDE_TAIR_USER_NAME = "b6241830ca7f4b9d";
    public static final int SHOPPING_GUIDE_NAME_SPACE = 184;

    Logger LOGGER = LoggerFactory.getLogger(IconItemOriginDataFailProcessorSdkExtPt.class);


    @Autowired
    TairSpi tairSpi;
    @Override
    public OriginDataDTO<ItemEntity> process(OriginDataProcessRequest originDataProcessRequest) {
        String tairKey = "";
        boolean success = false;
        try {
            ItemRequest itemRequest = (ItemRequest) Optional.of(originDataProcessRequest)
                    .map(OriginDataProcessRequest::getSgFrameworkContextItem)
                    .map(SgFrameworkContext::getTacContext)
                    .map(c -> c.get(ItemRecommendService.ITEM_REQUEST_KEY))
                    .orElse(null);
            Preconditions.checkArgument(itemRequest != null);

            tairKey = buildTairKey(itemRequest);
            Result<DataEntry> dataEntryResult = tairSpi.get(SHOPPING_GUIDE_TAIR_USER_NAME, SHOPPING_GUIDE_NAME_SPACE, tairKey);

            Object o = Optional.ofNullable(dataEntryResult).map(Result::getValue).map(DataEntry::getValue).orElse(null);
            if (o == null) {
                return originDataProcessRequest.getItemEntityOriginDataDTO();
            }

            List<ItemEntity> itemEntityList = JSON.parseArray(o.toString(), ItemEntity.class);
            OriginDataDTO<ItemEntity> result = new OriginDataDTO<>();
            result.setResult(itemEntityList);
            success = CollectionUtils.isNotEmpty(itemEntityList);
            result.setHasMore(false);
            result.setSuccess(true);
            result.setScm("tpp.error");
            return result;
        } catch (Exception e) {
            LOGGER.error("IconItemOriginDataFailProcessorSdkExtPt error", e);
            return originDataProcessRequest.getItemEntityOriginDataDTO();
        } finally {
            LOGGER.warn("IconItemOriginDataFailProcessorSdkExtPt_result:{}", success);
        }

    }

    public static String buildTairKey(ItemRequest itemRequest) {
        return "iconItemTairKey_" + itemRequest.getLevel1Id() + "_" + itemRequest.getLevel2Id() + "_" + itemRequest.getLevel3Id();
    }


}
