package com.tmall.wireless.tac.biz.processor.mmc.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.hyperlocalretail.sdk.member.o2otbmc.domain.ItemDirectionalDiscountRequest;
import com.alibaba.hyperlocalretail.sdk.member.o2otbmc.domain.ItemDirectionalDiscountResponse;
import com.alibaba.hyperlocalretail.sdk.member.o2otbmc.domain.O2OItemPriceDTO;
import com.alibaba.tcls.scrm.sdk.utils.domain.common.Result;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.taobao.freshx.homepage.client.domain.*;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.spi.recommend.MmcMemberService;
import com.tmall.wireless.tac.biz.processor.mmc.context.ItemInfo;
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

    private static final int newItemSize = 2;

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

                String locType = "O2OHalfDay";
                if(materialDO.getBizType()!=null
                    && StringUtils.isNotBlank(materialDO.getBizType().getCode())
                    && materialDO.getBizType().getCode().equals(BizType.ONE_HOUR_TO_HOME.getCode())){
                    locType = "O2OOneHour";
                }
                sortItem(materialDO,canExposureItemCount,itemPriceMap,locType);
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
    private void sortItem(MaterialDO materialDO,int canExposureItemCount,Map<Long, O2OItemPriceDTO> itemPriceMap,String locType){

        List<ItemDO> finalItemList = Lists.newArrayList();

        try{


            List<ItemDO> oldItemList = Lists.newArrayList();
            List<ItemDO> itemList = materialDO.getItems();
            List<ItemDO> newItemList = Lists.newArrayList();
            if(CollectionUtils.isNotEmpty(itemList) && canExposureItemCount > 0){
                for(int i = 0;i<itemList.size();i++){
                    ItemDO itemDO = itemList.get(i);
                    ItemType itemType = itemDO.getType();
                    if(itemType.getCode().equals(ItemType.NEW_USER_ITEM.getCode())
                        && itemPriceMap!=null
                        && itemPriceMap.get(itemDO.getItemId())!=null){
                        if(newItemList.size() > newItemSize){
                            continue;
                        }
                        newItemList.add(itemDO);
                    }else if(itemType.getCode().equals(ItemType.NORMAL_ITEM.getCode())){
                        oldItemList.add(itemDO);
                    }
                }
                if(newItemList.size() < canExposureItemCount){
                    if(oldItemList.size() > canExposureItemCount-newItemList.size()){
                        oldItemList = oldItemList.subList(0,canExposureItemCount-newItemList.size());
                    }
                }
                finalItemList.addAll(newItemList);
                finalItemList.addAll(oldItemList);

                /**
                 * newItemIds=商品1ID:O2OHalfDay,商品2Id:O2OHalfDay,……
                 * itemIds=商品1ID:O2OHalfDay,商品2Id:O2OHalfDay,……
                 */
                //StringBuilder oldItemIds = new StringBuilder();
                //新人品
                //StringBuilder newItemIds = new StringBuilder();
                Map<Long,List<ItemInfo>> newMap = Maps.newHashMap();
                Map<Long,String> oldMap = Maps.newHashMap();
                List<ItemInfo> oldItemInfoList = Lists.newArrayList();
                List<ItemInfo> newItemInfoList = Lists.newArrayList();

                for(int i=0;i<newItemList.size();i++){
                    ItemDO itemDO = newItemList.get(i);
                    ItemInfo itemInfo = new ItemInfo();
                    itemInfo.setItemId(itemDO.getItemId());
                    itemInfo.setLocType(locType);
                    newItemInfoList.add(itemInfo);
                    newMap.put(itemDO.getItemId(),newItemInfoList);
                }

                if(CollectionUtils.isNotEmpty(newItemList)){

                    newItemList.forEach(itemDO -> {

                    });
                }



                finalItemList.forEach(itemDO->{
                    ItemInfo itemInfo = new ItemInfo();
                    itemInfo.setItemId(itemDO.getItemId());
                    //itemInfo.setLocType(locType);
                    if(itemDO.getType().getCode().equals(ItemType.NEW_USER_ITEM.getCode())){
                        newItemInfoList.add(itemInfo);
                        //newItemIds.append(itemDO.getItemId()).append(":").append(locType).append(",");
                    }else {
                        oldItemInfoList.add(itemInfo);
                        //oldItemIds.append(itemDO.getItemId()).append(":").append(locType).append(",");
                    }
                });






                StringBuilder sbActionUrl = new StringBuilder();
                sbActionUrl.append(actionUrl);

               /* if(StringUtils.isNotBlank(oldItemIds.toString())){
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
                }*/

                if(materialDO.getBenefit()!=null){
                    materialDO.getBenefit().setActionUrl(sbActionUrl.toString());
                }

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
