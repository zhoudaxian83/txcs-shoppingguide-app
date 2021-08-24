package com.tmall.wireless.tac.biz.processor.huichang.inventory.inventoryChannelItemPage;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.tcls.gs.sdk.biz.uti.MapUtil;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataPostProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.OriginDataProcessRequest;
import com.tmall.tcls.gs.sdk.framework.model.constant.RequestKeyConstant;
import com.tmall.tcls.gs.sdk.framework.model.context.*;
import com.tmall.txcs.gs.model.item.BizType;
import com.tmall.txcs.gs.model.item.O2oType;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.utils.PageUrlUtil;
import com.tmall.wireless.tac.biz.processor.huichang.common.utils.ParseCsa;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import com.tmall.wireless.tac.dataservice.log.TacLogConsts;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

@SdkExtension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
        useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
        scenario = HallScenarioConstant.HALL_SCENARIO_SCENARIO_INVENTORY_CHANNEL_ITEM_PAGE)
public class InventoryChannelItemPageOriginDataPostProcessorSdkExtPt extends Register implements ItemOriginDataPostProcessorSdkExtPt {
    @Autowired
    TacLogger tacLogger;
    @Override
    public OriginDataDTO<ItemEntity> process(OriginDataProcessRequest originDataProcessRequest) {
        tacLogger.debug("扩展点InventoryChannelItemPageOriginDataPostProcessorSdkExtPt");
        SgFrameworkContextItem sgFrameworkContextItem = Optional.of(originDataProcessRequest.getSgFrameworkContextItem()).orElse(new SgFrameworkContextItem());
        OriginDataDTO<ItemEntity> itemEntityOriginDataDTO = Optional.of(originDataProcessRequest.getItemEntityOriginDataDTO()).orElse(new OriginDataDTO<ItemEntity>());
        tacLogger.debug("商品旧顺序：" + JSONObject.toJSONString(itemEntityOriginDataDTO.getResult()));

        RequestContext4Ald requestContext4Ald = (RequestContext4Ald)(sgFrameworkContextItem.getTacContext());
        Map<String, Object> aldParams = requestContext4Ald.getAldParam();
        String items = PageUrlUtil.getParamFromCurPageUrl(aldParams, "items", tacLogger); // 二跳页展示的6个商品
        if(StringUtils.isNotBlank(items)) {
            List<ItemEntity> itemEntityList =  Optional.of(itemEntityOriginDataDTO).map(OriginDataDTO::getResult).orElse(Lists.newArrayList());
            List<String> itemList = Arrays.asList(items.split(","));
            Set<Long> itemSet = itemList.stream().map(Long::valueOf).collect(Collectors.toSet());
            List<ItemEntity> newItemEntityList = Lists.newArrayList();
            // 如果是第一页，要把二跳页的商品置顶
            String indexStr = PageUrlUtil.getParamFromCurPageUrl(aldParams, "index", tacLogger);
            int index = 0;
            if(StringUtils.isNotBlank(indexStr)) {
                index = Integer.valueOf(indexStr);
            } else {
                index = Optional.ofNullable((String)(aldParams.get("pageIndex"))).map(Integer::valueOf).orElse(0);
            }

            if(index == 0) {
                for(String itemId: itemList) {
                    ItemEntity item = new ItemEntity();
                    item.setItemId(Long.valueOf(itemId));
                    String locType = PageUrlUtil.getParamFromCurPageUrl(aldParams,"locType", tacLogger);
                    String detailLocType = getDetailLocType(locType, aldParams);
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
        tacLogger.debug("商品新顺序：" + JSONObject.toJSONString(itemEntityOriginDataDTO.getResult()));
        return itemEntityOriginDataDTO;
    }

    private String getDetailLocType(String locType, Map<String, Object> aldParams) {
        if("B2C".equals(locType) || locType == null) {
            if(StringUtils.isBlank(locType)) {
                tacLogger.debug("locType是空");
            }
            return O2oType.B2C.name();
        } else {
            Long smAreaId = MapUtil.getLongWithDefault(aldParams, RequestKeyConstant.SMAREAID, 310100L);
            LocParams locParams = ParseCsa.parseCsaObj(aldParams.get(RequestKeyConstant.USER_PARAMS_KEY_CSA), smAreaId);
            if(Optional.ofNullable(locParams).map(params -> params.getRt1HourStoreId()).orElse(0L) > 0) {
                return O2oType.O2OOneHour.name();
            } else if(Optional.ofNullable(locParams).map(params -> params.getRtHalfDayStoreId()).orElse(0L) > 0){
                return O2oType.O2OHalfDay.name();
            } else {
                return O2oType.O2O.name();
            }


        }
    }

    public static void main(String[] args){
        Map<String, String> map = Maps.newHashMap();
        map.put("9", "9");
        int a = Optional.ofNullable(map).map(myMap -> myMap.get("9")).map(Integer::valueOf).orElse(0);
        System.out.println(a);
    }
}
