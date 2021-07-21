package com.tmall.wireless.tac.biz.processor.mmc.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.hyperlocalretail.sdk.member.o2otbmc.domain.ItemDirectionalDiscountRequest;
import com.alibaba.hyperlocalretail.sdk.member.o2otbmc.domain.ItemDirectionalDiscountResponse;
import com.alibaba.hyperlocalretail.sdk.member.o2otbmc.domain.O2OItemPriceDTO;
import com.alibaba.tcls.scrm.sdk.utils.domain.common.Result;
import com.google.common.collect.Lists;
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

import java.math.BigDecimal;
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

    private static final String actionUrl = "https://pages.tmall.com/wow/an/cs/act/wupr?disableNav=YES&wh_biz=tm&wh_pid=o2o-mmc/index&sourceChannel=mmc-halfday&channel=halfday&pha=true";

    @Override
    public TacResult<MaterialDO> execute(Context context) throws Exception {

        MaterialDO materialDO = null;
        try{
            HadesLogUtil.stream("MmcItemMergeHandler request")
                .kv("context",JSON.toJSONString(context))
                .info();
            Long userId = MapUtil.getLongWithDefault(context.getParams(),"userId",0L);
            int canExposureItemCount = Integer.valueOf(MapUtil.getStringWithDefault(context.getParams(),"canExposureItemCount","0"));
            ItemDirectionalDiscountRequest request = new ItemDirectionalDiscountRequest();


            if(context.getParams().get("materialDO")!=null){
                materialDO = (MaterialDO)context.getParams().get("materialDO");
                Long storeId = Long.valueOf(materialDO.getStores().get(0).getStoreId());
                request.setStoreId(storeId);
            }

            String umpId = "0";
            if(context.getParams().get("extendData")!=null){
                Map extendData = (Map)context.getParams().get("extendData");

                HadesLogUtil.stream("MmcItemMergeHandler extendData")
                    .kv("extendData",JSON.toJSONString(extendData))
                    .info();
                umpId = (String)extendData.get("chooseUmpId");
                if(StringUtils.isNotBlank((String)extendData.get("benefitPic"))){
                    BenefitDO benefitDO;
                    if(materialDO.getBenefit() == null){
                        benefitDO = new BenefitDO();
                        materialDO.setBenefit(benefitDO);
                    }else{
                        benefitDO = materialDO.getBenefit();
                    }
                    benefitDO.setPicUrl((String)extendData.get("benefitPic"));
                    benefitDO.setId(umpId);
                    //benefitDO.setActionUrl(actionUrl);
                }
            }

            if(materialDO!=null && CollectionUtils.isNotEmpty(materialDO.getItems())){
                List<Long> newItemIdList = Lists.newArrayList();
                materialDO.getItems().forEach(itemDO -> {
                    ItemType itemType = itemDO.getType();
                    if(itemType.getCode().equals(ItemType.NEW_USER_ITEM.getCode())){
                        newItemIdList.add(itemDO.getItemId());
                    }
                });
                if(CollectionUtils.isNotEmpty(newItemIdList)
                    && request.getStoreId()!=null
                    && request.getStoreId()!=0L){
                    request.setItemIds(newItemIdList);
                    request.setUmpId(Long.valueOf(umpId));
                    request.setUserId(userId);
                    Result<ItemDirectionalDiscountResponse> responseResult =  mmcMemberService.queryItemDirectionalDiscount(request);
                    HadesLogUtil.stream("MmcItemMergeHandler responseResult")
                        .kv("request",JSON.toJSONString(request))
                        .kv("responseResult",JSON.toJSONString(responseResult))
                        .info();
                    if(responseResult!=null
                        && responseResult.getData()!=null
                        && responseResult.isSuccess()){
                        ItemDirectionalDiscountResponse itemDirectionalDiscountResponse = responseResult.getData();
                        Map<Long, O2OItemPriceDTO> itemPriceMap = itemDirectionalDiscountResponse.getItemPriceMap();
                        if(itemPriceMap!=null){
                            context.getParams().put("itemPriceMap",itemPriceMap);
                            materialDO.getItems().forEach(itemDO -> {
                                ItemType itemType = itemDO.getType();
                                if(itemType.getCode().equals(ItemType.NEW_USER_ITEM.getCode())){
                                    if(itemPriceMap.get(itemDO.getItemId())!=null){
                                        itemDO.setPromotedPriceYuan(itemPriceMap.get(itemDO.getItemId()).getPriceInYuan());
                                        BigDecimal pri = itemPriceMap.get(itemDO.getItemId()).getPrice();
                                        if(pri!=null){
                                            itemDO.setPromotedPrice(pri.multiply(new BigDecimal(100)).longValue());
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
                Map<Long, O2OItemPriceDTO> itemPriceMap = (Map<Long, O2OItemPriceDTO>)context.getParams().get("itemPriceMap");
                sortItem(materialDO,canExposureItemCount,itemPriceMap);
            }
            HadesLogUtil.stream("MmcItemMergeHandler response")
                .kv("materialDO",JSON.toJSONString(materialDO))
                .kv("code","0000")
                .info();
        }catch (Exception e){
            HadesLogUtil.stream("MmcItemMergeHandler error")
                .kv("code","1000")
                .kv("exception",JSON.toJSONString(e))
                .error();
            LOGGER.error("MmcItemMergeHandlerExecuteError",e);
        }

        return TacResult.newResult(materialDO);
    }


    /**
     * 新人优先
     * @param canExposureItemCount
     * @param materialDO
     */
    private void sortItem(MaterialDO materialDO,int canExposureItemCount,Map<Long, O2OItemPriceDTO> itemPriceMap){

        List<ItemDO> finalItemList = Lists.newArrayList();

        try{
            List<ItemDO> reItemList = Lists.newArrayList();
            List<ItemDO> itemList = materialDO.getItems();
            List<ItemDO> newItemList = Lists.newArrayList();
            if(CollectionUtils.isNotEmpty(itemList) && canExposureItemCount > 0){

                HadesLogUtil.stream("MmcItemMergeHandler itemList")
                    .kv("itemList",JSON.toJSONString(itemList))
                    .info();
                itemList.forEach(itemDO -> {
                    ItemType itemType = itemDO.getType();
                    if(itemType.getCode().equals(ItemType.NEW_USER_ITEM.getCode())
                        && itemPriceMap!=null
                        && itemPriceMap.get(itemDO.getItemId())!=null){
                        newItemList.add(itemDO);
                    }else if(itemType.getCode().equals(ItemType.NORMAL_ITEM.getCode())){
                        reItemList.add(itemDO);
                    }

                });
                if(newItemList.size() < canExposureItemCount){
                    if(reItemList.size() > canExposureItemCount-newItemList.size()){
                        newItemList.addAll(reItemList.subList(0,canExposureItemCount-newItemList.size()));
                    }else {
                        newItemList.addAll(reItemList);
                    }
                    finalItemList = newItemList;
                }else {
                    finalItemList = newItemList.subList(0,canExposureItemCount);
                }

                /**
                 * newItemIds=商品1ID:O2OHalfDay,商品2Id:O2OHalfDay,……
                 * itemIds=商品1ID:O2OHalfDay,商品2Id:O2OHalfDay,……
                 */
                StringBuilder oldItemIds = new StringBuilder();
                //新人品
                StringBuilder newItemIds = new StringBuilder();

                finalItemList.forEach(itemDO->{
                    if(itemDO.getType().getCode().equals(ItemType.NEW_USER_ITEM.getCode())){
                        newItemIds.append(itemDO.getItemId()).append(":O2OHalfDay").append(",");
                    }else {
                        oldItemIds.append(itemDO.getItemId()).append(":O2OHalfDay").append(",");
                    }
                });


                StringBuilder sbActionUrl = new StringBuilder();
                sbActionUrl.append(actionUrl);
                if(StringUtils.isNotBlank(oldItemIds.toString())){
                    sbActionUrl.append("&itemIds=");
                    if(oldItemIds.toString().endsWith(",")){
                        sbActionUrl.append(oldItemIds.substring(0,oldItemIds.length()-1));
                    }else{
                        sbActionUrl.append(oldItemIds.toString());
                    }
                }
                if(StringUtils.isNotBlank(newItemIds.toString())){
                    sbActionUrl.append("&newItemIds=");
                    if(newItemIds.toString().endsWith(",")){
                        sbActionUrl.append(newItemIds.substring(0,newItemIds.length()-1));
                    }else{
                        sbActionUrl.append(newItemIds.toString());
                    }
                }
                finalItemList.forEach(itemDO->{
                    itemDO.setActionUrl(sbActionUrl.toString());
                });

                if(materialDO.getBenefit()!=null){
                    materialDO.getBenefit().setActionUrl(sbActionUrl.toString());
                }
                HadesLogUtil.stream("MmcItemMergeHandler newItemList")
                    .kv("newItemList",JSON.toJSONString(finalItemList))
                    .info();

            }
        }catch (Exception e){
            LOGGER.error("MmcItemMergeHandler sortItem error",e);
        }

        if(CollectionUtils.isNotEmpty(finalItemList)){
            materialDO.setItems(finalItemList);
        }

        //return newItemList;

    }

}
