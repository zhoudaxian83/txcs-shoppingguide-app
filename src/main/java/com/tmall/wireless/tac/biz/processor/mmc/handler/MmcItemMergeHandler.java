package com.tmall.wireless.tac.biz.processor.mmc.handler;

import com.alibaba.hyperlocalretail.sdk.member.o2otbmc.domain.ItemDirectionalDiscountRequest;
import com.alibaba.hyperlocalretail.sdk.member.o2otbmc.domain.ItemDirectionalDiscountResponse;
import com.alibaba.hyperlocalretail.sdk.member.o2otbmc.domain.O2OItemPriceDTO;
import com.alibaba.tcls.scrm.sdk.utils.domain.common.Result;
import com.google.common.collect.Lists;
import com.taobao.freshx.homepage.client.domain.ItemDO;
import com.taobao.freshx.homepage.client.domain.ItemType;
import com.taobao.freshx.homepage.client.domain.MaterialDO;
import com.tmall.txcs.gs.spi.recommend.MmcMemberService;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.handler.TacReactiveHandler;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
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
public class MmcItemMergeHandler implements TacReactiveHandler<MaterialDO> {

    @Autowired
    private MmcMemberService mmcMemberService;

    @Override
    public Flowable<TacResult<MaterialDO>> executeFlowable(Context context) throws Exception {

        Long userId = (Long)context.getParams().get("userId");
        MaterialDO materialDO = (MaterialDO)context.getParams().get("materialDO");
        Integer canExposureItemCount = (Integer)context.getParams().get("canExposureItemCount");
        Map extendData = (Map)context.getParams().get("extendData");

        List<Long> itemIdList = Lists.newArrayList();
        Long storeId = Long.valueOf(materialDO.getStores().get(0).getStoreId());
        materialDO.getItems().forEach(itemDO -> {
            ItemType itemType = itemDO.getType();
            if(itemType.getCode().equals(ItemType.NEW_USER_ITEM.getCode())){
                itemIdList.add(itemDO.getItemId());
            }
        });

        ItemDirectionalDiscountRequest request = new ItemDirectionalDiscountRequest();
        request.setItemIds(itemIdList);
        request.setStoreId(storeId);
        request.setUmpId(0L);
        request.setUserId(userId);
        if(CollectionUtils.isNotEmpty(itemIdList)){
            Result<ItemDirectionalDiscountResponse> responseResult =  mmcMemberService.queryItemDirectionalDiscount(request);
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

        List<ItemDO> reItemList = sortItem(canExposureItemCount,materialDO.getItems());
        materialDO.setItems(reItemList);
        return Flowable.just(TacResult.newResult(materialDO));
    }

    private BigDecimal getPrice(Long itemId,Map<Long, O2OItemPriceDTO> map){

        return map.get(itemId).getPrice();
    }

    /**
     * 新人优先
     * @param canExposureItemCount
     * @param itemList
     */
    private List<ItemDO> sortItem(Integer canExposureItemCount,List<ItemDO> itemList){
        List<ItemDO> reItemList = Lists.newArrayList();
        List<ItemDO> newItemList = Lists.newArrayList();
        itemList.forEach(itemDO -> {
            ItemType itemType = itemDO.getType();
            if(itemType.getCode().equals(ItemType.NEW_USER_ITEM.getCode())){
                newItemList.add(itemDO);
            }else {
                reItemList.add(itemDO);
            }

        });
        if(CollectionUtils.isNotEmpty(reItemList)){
            newItemList.addAll(reItemList);
        }
        return newItemList;

    }
}
