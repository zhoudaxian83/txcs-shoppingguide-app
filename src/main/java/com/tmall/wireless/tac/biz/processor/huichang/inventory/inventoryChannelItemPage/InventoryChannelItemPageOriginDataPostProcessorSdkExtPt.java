package com.tmall.wireless.tac.biz.processor.huichang.inventory.inventoryChannelItemPage;

import com.google.common.collect.Lists;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataPostProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.OriginDataProcessRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.*;
import com.tmall.txcs.gs.model.item.BizType;
import com.tmall.txcs.gs.model.item.O2oType;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.utils.PageUrlUtil;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@SdkExtension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
        useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
        scenario = HallScenarioConstant.HALL_SCENARIO_SCENARIO_INVENTORY_CHANNEL_ITEM_PAGE)
public class InventoryChannelItemPageOriginDataPostProcessorSdkExtPt extends Register implements ItemOriginDataPostProcessorSdkExtPt {

    @Override
    public OriginDataDTO<ItemEntity> process(OriginDataProcessRequest originDataProcessRequest) {
        SgFrameworkContextItem sgFrameworkContextItem = Optional.of(originDataProcessRequest.getSgFrameworkContextItem()).orElse(new SgFrameworkContextItem());
        OriginDataDTO<ItemEntity> itemEntityOriginDataDTO = Optional.of(originDataProcessRequest.getItemEntityOriginDataDTO()).orElse(new OriginDataDTO<ItemEntity>());
        List<ItemEntity> itemEntityList =  Optional.of(itemEntityOriginDataDTO).map(OriginDataDTO::getResult).orElse(Lists.newArrayList());

        RequestContext4Ald requestContext4Ald = (RequestContext4Ald)(sgFrameworkContextItem.getTacContext());
        Map<String, Object> aldParams = requestContext4Ald.getParams();
        String items = PageUrlUtil.getParamFromCurPageUrl(aldParams, "items"); // 二跳页展示的6个商品
        if(StringUtils.isNotBlank(items)) {
            List<String> itemList = Arrays.asList(items.split(","));
            Set<Long> itemSet = itemList.stream().map(Long::valueOf).collect(Collectors.toSet());
            List<ItemEntity> newItemEntityList = Lists.newArrayList();
            // 如果是第一页，要把二跳页的商品置顶
            if(Optional.ofNullable(sgFrameworkContextItem.getCommonUserParams().getUserPageInfo().getIndex()).orElse(0) == 0) {
                for(String itemId: itemList) {
                    ItemEntity item = new ItemEntity();
                    item.setItemId(Long.valueOf(itemId));
                    String locType = PageUrlUtil.getParamFromCurPageUrl(aldParams,"locType");
                    String detailLocType = getDetailLocType(locType, sgFrameworkContextItem);
                    item.setO2oType(detailLocType);
                    item.setBusinessType(detailLocType);
                    item.setBizType(BizType.SM.getCode());
                    newItemEntityList.add(item);
                }
            }
            for(ItemEntity itemEntity : itemEntityList) {
                if(itemSet.contains(itemEntity.getItemId())) {
                    continue;
                }
                newItemEntityList.add(itemEntity);
            }
            itemEntityOriginDataDTO.setResult(newItemEntityList);
        }
        else {
            itemEntityOriginDataDTO.setResult(itemEntityList);
        }
        return itemEntityOriginDataDTO;
    }

    private String getDetailLocType(String locType, SgFrameworkContextItem sgFrameworkContextItem) {
        if("B2C".equals(locType) || locType == null) {
            return com.tmall.txcs.gs.model.item.O2oType.B2C.name();
        } else {
            if(Optional.ofNullable(sgFrameworkContextItem.getCommonUserParams().getLocParams().getRt1HourStoreId()).orElse(0L) > 0) {
                return com.tmall.txcs.gs.model.item.O2oType.O2OOneHour.name();
            } else if(Optional.ofNullable(sgFrameworkContextItem.getCommonUserParams().getLocParams().getRtHalfDayStoreId()).orElse(0L) > 0){
                return com.tmall.txcs.gs.model.item.O2oType.O2OHalfDay.name();
            } else {
                return O2oType.O2O.name();
            }
        }
    }
}
