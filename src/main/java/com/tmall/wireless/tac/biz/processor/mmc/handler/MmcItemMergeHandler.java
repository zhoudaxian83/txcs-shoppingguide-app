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
import java.util.stream.Collectors;

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

    private static final int newItemSize = 1;

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

                HadesLogUtil.stream("MmcItemMergeHandler extendData")
                    .kv("extendData",JSON.toJSONString(extendData))
                    .info();

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
                                    //remove
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
                String locType = "O2OHalfDay";
                if(materialDO.getBizType()!=null
                    && StringUtils.isNotBlank(materialDO.getBizType().getCode())
                    && materialDO.getBizType().getCode().equals(BizType.ONE_HOUR_TO_HOME.getCode())){
                    locType = "O2OOneHour";
                }
                Map<Long, O2OItemPriceDTO> itemPriceMap = (Map<Long, O2OItemPriceDTO>)context.getParams().get("itemPriceMap");
                sortItem(materialDO,canExposureItemCount,itemPriceMap,locType);
            }
            HadesLogUtil.stream("MmcItemMergeHandler response")
                .kv("materialDO",JSON.toJSONString(materialDO.getBenefit()))
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
            List<ItemDO> itemList = materialDO.getItems();
            List<ItemDO> oldItemList = Lists.newArrayList();
            List<ItemDO> newItemList = Lists.newArrayList();


            //&& itemPriceMap.get(itemDO.getItemId())!=null
            if(CollectionUtils.isNotEmpty(itemList) && canExposureItemCount > 0){
                for(int i = 0;i<itemList.size();i++){
                    ItemDO itemDO = itemList.get(i);
                    ItemType itemType = itemDO.getType();
                    if(itemType.getCode().equals(ItemType.NEW_USER_ITEM.getCode())
                        && itemPriceMap!=null){
                        if(newItemList.size() > newItemSize){
                            continue;
                        }
                        if(newItemList.size() > canExposureItemCount-1){
                            break;
                        }
                        newItemList.add(itemDO);
                    }else if(itemType.getCode().equals(ItemType.NORMAL_ITEM.getCode())){
                        if(oldItemList.size() > canExposureItemCount-1){
                            continue;
                        }
                        oldItemList.add(itemDO);
                    }
                }
                finalItemList.addAll(newItemList);
                if(oldItemList.size() > canExposureItemCount-newItemList.size()){
                    oldItemList = oldItemList.subList(0,canExposureItemCount-newItemList.size());
                }
                finalItemList.addAll(oldItemList);


                /**
                 * newItemIds=商品1ID:O2OHalfDay,商品2Id:O2OHalfDay,……
                 * itemIds=商品1ID:O2OHalfDay,商品2Id:O2OHalfDay,……
                 */
                List<Long> oldItemIdList = Lists.newArrayList();
                List<Long> newItemIdList = Lists.newArrayList();

                Map<Long,String> newUrlMap = Maps.newHashMap();
                Map<Long,String> oldUrlMap = Maps.newHashMap();
                newItemList.forEach(itemDO -> {
                    newItemIdList.add(itemDO.getItemId());
                });

                oldItemList.forEach(itemDO -> {
                    oldItemIdList.add(itemDO.getItemId());
                });

                newItemList.forEach(itemDO -> {
                    List<Long> itemIdList = Lists.newArrayList(newItemIdList);
                    if(!itemIdList.get(0).equals(itemDO.getItemId())){
                        itemIdList.remove(itemDO.getItemId());
                        itemIdList.add(0,itemDO.getItemId());
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append("&newItemIds=");
                    List li = itemIdList.stream().map(e->{
                        return e+":"+locType;
                    }).collect(Collectors.toList());
                    sb.append(String.join(",",li));
                    newUrlMap.put(itemDO.getItemId(),sb.toString());
                    newUrlMap.put(1L,sb.toString());
                });

                oldItemList.forEach(itemDO -> {
                    List<Long> itemIdList = Lists.newArrayList(oldItemIdList);
                    if(!itemIdList.get(0).equals(itemDO.getItemId())){
                        itemIdList.remove(itemDO.getItemId());
                        itemIdList.add(0,itemDO.getItemId());
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append("&itemIds=");
                    List li = itemIdList.stream().map(e->{
                        return e+":"+locType;
                    }).collect(Collectors.toList());
                    sb.append(String.join(",",li));
                    oldUrlMap.put(itemDO.getItemId(),sb.toString());
                    oldUrlMap.put(2L,sb.toString());

                });


                finalItemList.forEach(itemDO -> {
                    StringBuilder sb = new StringBuilder();
                    if(itemDO.getType().getCode().equals(ItemType.NEW_USER_ITEM.getCode())){
                        if(StringUtils.isNotBlank(newUrlMap.get(itemDO.getItemId()))){
                            sb.append(actionUrl).append(newUrlMap.get(itemDO.getItemId()));
                        }
                        if(StringUtils.isNotBlank(oldUrlMap.get(2L))){
                            sb.append(oldUrlMap.get(2L));
                        }
                        itemDO.setActionUrl(sb.toString());
                    }else if(itemDO.getType().getCode().equals(ItemType.NORMAL_ITEM.getCode())){
                        if(StringUtils.isNotBlank(oldUrlMap.get(itemDO.getItemId()))){
                            sb.append(actionUrl).append(oldUrlMap.get(itemDO.getItemId()));
                        }
                        if(StringUtils.isNotBlank(newUrlMap.get(1L))){
                            sb.append(newUrlMap.get(1L));
                        }
                        itemDO.setActionUrl(sb.toString());
                    }
                    if(materialDO.getBenefit()!=null){
                        materialDO.getBenefit().setActionUrl(sb.toString());
                    }
                });



            }
        }catch (Exception e){
            LOGGER.error("MmcItemMergeHandler sortItem error",e);
        }

        if(CollectionUtils.isNotEmpty(finalItemList)){
            materialDO.setItems(finalItemList);
        }

        //return newItemList;

    }

    public static void  main(String args[]){

        List<String> lis = Lists.newArrayList();
        lis.add("111");
        lis.add("222");

        System.out.println(JSON.toJSONString(lis));

        List<String> lis1 = Lists.newArrayList(lis);
        lis1.remove("222");
        System.out.println(JSON.toJSONString(lis1));
        System.out.println(JSON.toJSONString(lis));



    }

}
