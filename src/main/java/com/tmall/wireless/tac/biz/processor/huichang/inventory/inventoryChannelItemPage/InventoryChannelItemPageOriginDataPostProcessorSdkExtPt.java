package com.tmall.wireless.tac.biz.processor.huichang.inventory.inventoryChannelItemPage;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.aselfcaptain.util.StackTraceUtil;
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
import lombok.SneakyThrows;
import org.apache.commons.collections.CollectionUtils;
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
    @SneakyThrows
    @Override
    public OriginDataDTO<ItemEntity> process(OriginDataProcessRequest originDataProcessRequest) {
        tacLogger.debug("扩展点InventoryChannelItemPageOriginDataPostProcessorSdkExtPt");
        OriginDataDTO<ItemEntity> itemEntityOriginDataDTO = null;
        List<ItemEntity> itemEntityList = null;
        try{
            itemEntityOriginDataDTO = originDataProcessRequest.getItemEntityOriginDataDTO();
            itemEntityList = itemEntityOriginDataDTO.getResult();
            if(CollectionUtils.isEmpty(itemEntityList)) {
                throw new Exception("itemEntity为空");
            }
            tacLogger.debug("商品旧顺序：" + JSONObject.toJSONString(itemEntityList));
        } catch (Exception e) {
            tacLogger.debug("商品信息解析出错：" + StackTraceUtil.stackTrace(e));
            throw new Exception("商品信息解析出错", e);
        }
        try{
            SgFrameworkContextItem sgFrameworkContextItem = originDataProcessRequest.getSgFrameworkContextItem();
            RequestContext4Ald requestContext4Ald = (RequestContext4Ald)(sgFrameworkContextItem.getTacContext());
            Map<String, Object> aldParams = requestContext4Ald.getAldParam();
            String items = PageUrlUtil.getParamFromCurPageUrl(aldParams, "items", tacLogger); // 二跳页展示的6个商品
            if(StringUtils.isNotBlank(items)) {
                List<String> itemList = Arrays.asList(items.split(","));
                Set<Long> itemSet = itemList.stream().map(Long::valueOf).collect(Collectors.toSet());
                List<ItemEntity> newItemEntityList = Lists.newArrayList();
                // 如果是第一页，要把二跳页的商品置顶(后来改成没有分页了)
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

                for(ItemEntity itemEntity : itemEntityList) {
                    if(!itemSet.contains(itemEntity.getItemId())) {
                        newItemEntityList.add(itemEntity);
                    }
                }
                itemEntityOriginDataDTO.setResult(newItemEntityList);
            }
        } catch (Exception e) {
            tacLogger.debug("商品重排序失败,使用原来顺序" + StackTraceUtil.stackTrace(e));
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
        Map<String, List<Integer>> map = Maps.newHashMap();
        map.put("9", new ArrayList<Integer>());
        int a = Optional.ofNullable(map.get("8").get(0)).orElse(0);
        System.out.println(a);
    }
}
