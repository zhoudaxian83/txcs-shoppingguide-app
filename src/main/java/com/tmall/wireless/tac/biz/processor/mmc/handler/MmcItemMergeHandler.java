package com.tmall.wireless.tac.biz.processor.mmc.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.hyperlocalretail.sdk.member.o2otbmc.domain.ItemDirectionalDiscountRequest;
import com.alibaba.hyperlocalretail.sdk.member.o2otbmc.domain.ItemDirectionalDiscountResponse;
import com.alibaba.hyperlocalretail.sdk.member.o2otbmc.domain.O2OItemPriceDTO;
import com.alibaba.tcls.scrm.sdk.utils.domain.common.Result;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.taobao.freshx.homepage.client.domain.BenefitDO;
import com.taobao.freshx.homepage.client.domain.ItemDO;
import com.taobao.freshx.homepage.client.domain.ItemType;
import com.taobao.freshx.homepage.client.domain.MaterialDO;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.spi.recommend.MmcMemberService;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.handler.TacHandler;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 买买菜商品渲染
 * @author haixiao.zhang
 * @date 2021/7/9
 */
@Service
public class MmcItemMergeHandler implements TacHandler<MaterialDO> {

    Logger LOGGER = LoggerFactory.getLogger(MmcItemMergeHandler.class);

    @Autowired
    private MmcMemberService mmcMemberService;

    @Override
    public TacResult<MaterialDO> execute(Context context) throws Exception {

        HadesLogUtil.stream("MmcItemMergeHandler")
            .kv("context",JSON.toJSONString(context))
            .info();
        Long userId = MapUtil.getLongWithDefault(context.getParams(),"userId",0L);
        int canExposureItemCount = Integer.valueOf(MapUtil.getStringWithDefault(context.getParams(),"canExposureItemCount","0"));
        ItemDirectionalDiscountRequest request = new ItemDirectionalDiscountRequest();

        MaterialDO materialDO = null;
        if(context.getParams().get("materialDO")!=null){
            materialDO = (MaterialDO)context.getParams().get("materialDO");
            Long storeId = Long.valueOf(materialDO.getStores().get(0).getStoreId());
            request.setStoreId(storeId);

        }
        Map extendData = Maps.newHashMap();
        String umpId = "0";
        if(context.getParams().get("extendData")!=null){
            extendData = (Map)context.getParams().get("extendData");
            umpId = (String)extendData.get("chooseUmpId");
        }

        List<Long> itemIdList = Lists.newArrayList();
        if(materialDO!=null && CollectionUtils.isNotEmpty(materialDO.getItems())){
            materialDO.getItems().forEach(itemDO -> {
                ItemType itemType = itemDO.getType();
                if(itemType.getCode().equals(ItemType.NEW_USER_ITEM.getCode())){
                    itemIdList.add(itemDO.getItemId());
                }
            });
            request.setItemIds(itemIdList);
            request.setUmpId(Long.valueOf(umpId));
            request.setUserId(userId);
            if(CollectionUtils.isNotEmpty(itemIdList) && request.getStoreId()!=null && request.getStoreId()!=0L){
                Result<ItemDirectionalDiscountResponse> responseResult =  mmcMemberService.queryItemDirectionalDiscount(request);

                HadesLogUtil.stream("MmcItemMergeHandler responseResult")
                    .kv("request",JSON.toJSONString(request))
                    .kv("responseResult",JSON.toJSONString(responseResult))
                    .info();
                if(responseResult!=null && responseResult.isSuccess()){
                    ItemDirectionalDiscountResponse itemDirectionalDiscountResponse = responseResult.getData();
                    Map<Long, O2OItemPriceDTO> itemPriceMap = itemDirectionalDiscountResponse.getItemPriceMap();
                    materialDO.getItems().forEach(itemDO -> {
                        ItemType itemType = itemDO.getType();
                        if(itemType.getCode().equals(ItemType.NEW_USER_ITEM.getCode())){
                            if(itemPriceMap.get(itemDO.getItemId())!=null){
                                itemDO.setPromotedPriceYuan(itemPriceMap.get(itemDO.getItemId()).getPriceInYuan());
                                itemDO.setPromotedPrice(itemPriceMap.get(itemDO.getItemId()).getPrice().longValue());
                            }
                        }
                    });
                }
            }
            if(StringUtils.isNotBlank((String)extendData.get("benefitPic"))){
                BenefitDO benefitDO;
                if(materialDO.getBenefit() == null){
                    benefitDO = new BenefitDO();
                }else{
                    benefitDO = materialDO.getBenefit();
                }
                benefitDO.setPicUrl((String)extendData.get("benefitPic"));
                benefitDO.setId(umpId);
                canExposureItemCount = canExposureItemCount - 1;
            }

            List<ItemDO> reItemList = sortItem(canExposureItemCount,materialDO.getItems());

            materialDO.setItems(reItemList);
        }

        HadesLogUtil.stream("MmcItemMergeHandler materialDO")
            .kv("materialDO",JSON.toJSONString(materialDO))
            .info();
        return TacResult.newResult(materialDO);
    }


    /**
     * 新人优先
     * @param canExposureItemCount
     * @param itemList
     */
    private List<ItemDO> sortItem(int canExposureItemCount,List<ItemDO> itemList){

        List<ItemDO> newItemList = Lists.newArrayList();
        List<ItemDO> reItemList = Lists.newArrayList();

        if(CollectionUtils.isNotEmpty(itemList) && canExposureItemCount > 0){
            itemList.forEach(itemDO -> {
                ItemType itemType = itemDO.getType();
                if(itemType.getCode().equals(ItemType.NEW_USER_ITEM.getCode())){
                    newItemList.add(itemDO);
                }else {
                    reItemList.add(itemDO);
                }

            });
            if(newItemList.size() < canExposureItemCount){
                if(reItemList.size() > canExposureItemCount-newItemList.size()){
                    newItemList.addAll(reItemList.subList(0,canExposureItemCount-newItemList.size()));
                }else {
                    newItemList.addAll(reItemList);
                }
            }
            /**
             * newItemIds=商品1ID:O2OHalfDay,商品2Id:O2OHalfDay,……
             * itemIds=商品1ID:O2OHalfDay,商品2Id:O2OHalfDay,……
             */
            StringBuilder oldItemActionUrl = new StringBuilder();
            oldItemActionUrl.append("itemIds=");
            //新人品
            StringBuilder newItemActionUrl = new StringBuilder();
            newItemActionUrl.append("newItemIds=");

            newItemList.forEach(itemDO->{
                if(itemDO.getType().getCode().equals(ItemType.NEW_USER_ITEM.getCode())){
                    newItemActionUrl.append(itemDO.getItemId()).append(":O2OHalfDay").append(",");
                }else {
                    oldItemActionUrl.append(itemDO.getItemId()).append(":O2OHalfDay").append(",");
                }
            });
            newItemList.forEach(itemDO->{
                if(itemDO.getType().getCode().equals(ItemType.NEW_USER_ITEM.getCode())){
                    itemDO.setActionUrl(newItemActionUrl.toString());
                }else {
                    itemDO.setActionUrl(oldItemActionUrl.toString());
                }
            });

        }else {
            return itemList;
        }

        return newItemList;

    }
}
