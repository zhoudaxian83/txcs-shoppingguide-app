package com.tmall.wireless.tac.biz.processor.processtemplate.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tcls.mkt.atmosphere.model.response.ItemPromotionResp;
import com.tmall.aselfcaptain.item.constant.BizAttributes;
import com.tmall.aselfcaptain.item.model.ItemDTO;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemUtil {

    public static List<Map<String, Object>> buildItems(List<Long> itemIds, Map<Long, ItemDTO> longItemDTOMap) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<ItemDTO> sinkBottomItems = new ArrayList<>();
        for (Long itemId : itemIds) {
            ItemDTO itemDTO = longItemDTOMap.get(itemId);
            //有时候captain请求20个可能只返回少于20个品，因此从Map中获取的itemDTO可能为空，需要跳过
            if(itemDTO == null) {
                continue;
            }
            if(itemDTO.isSoldout() || !itemDTO.isCanBuy()) {
                sinkBottomItems.add(itemDTO);
            } else {
                result.add(ItemUtil.buildItemMap(itemDTO));
            }
        }
        for (ItemDTO sinkBottomItem : sinkBottomItems) {
            result.add(ItemUtil.buildItemMap(sinkBottomItem));
        }
        return result;
    }

    public static Map<String, Object> buildItemMap(ItemDTO itemDTO) {
        Map<String, Object> itemMap = new HashMap<>();
        if(itemDTO == null) {
            return itemMap;
        }
        itemMap.put("id", itemDTO.getItemId().getId());
        itemMap.put("itemId", itemDTO.getItemId().getId());
        //itemMap.put("storeId", tmcsContext.getStoreId());
        if (StringUtils.isBlank((String) itemMap.get("selfSupportProperties"))) {
            itemMap.put("selfSupportProperties", itemDTO.getSelfSupportProperties());
        }

        /*if (StringUtils.isBlank((String) itemMap.get("chaoshiItemTitle"))) {
            if ("O2O".equals(tmcsContext.getLocType())) {
                itemMap.put("chaoshiItemTitle", itemDTO.getTitle());
            } else {
                itemMap.put("chaoshiItemTitle", itemDTO.getShortTitle());
            }
        }*/

        itemMap.put("chaoshiItemTitle", itemDTO.getShortTitle());

        if (StringUtils.isBlank((String) itemMap.get("itemImg"))) {
            itemMap.put("itemImg", itemDTO.getWhitePic());
        }

        if (StringUtils.isBlank((String) itemMap.get("shortTitle"))) {
            itemMap.put("shortTitle", itemDTO.getShortTitle());
        }
        if (StringUtils.isBlank((String) itemMap.get("specDetail"))) {
            itemMap.put("specDetail", itemDTO.getSpecDetail());
        }

        String monthlySales = itemDTO.getAttributes().get(BizAttributes.ATTR_SALES_AMOUNT);
        if (org.apache.commons.lang3.StringUtils.isNotBlank(monthlySales) && Integer.valueOf(monthlySales) > 0) {
            itemMap.put("itemMonthSoldCount", toMonthlySalesView(monthlySales));
            itemMap.put("orignMonthSoldCount", monthlySales);
        }
        itemMap.put("itemUrl", itemDTO.getDetailUrl());
        //itemMap.put("scm", getScm1(tmcsContext, String.valueOf(itemDTO.getItemId().getId())));
        itemMap.put("_areaSellable", !itemDTO.isSoldout() && itemDTO.isCanBuy());
        itemMap.put("locType", itemDTO.getLocType().name());
        itemMap.put("sellerId", itemDTO.getSellerId());

        //itemMap.put("storeId", tmcsContext.getStoreId());
        //视频url地址补充

        if (BizAttributes.TRUE.equals(MapUtils.getString(itemDTO.getAttributes(), BizAttributes.ATTR_IS_WEIGHT_ITEM))) {
            String weightAttrJson = MapUtils.getString(itemDTO.getAttributes(), BizAttributes.ATTR_WEIGHT_ITEM);
            if (org.apache.commons.lang.StringUtils.isNotBlank(weightAttrJson)) {
                JSONObject ob = JSON.parseObject(weightAttrJson);
                String saleUnit = ob.getString(BizAttributes.WeightAttr.SALE_UNIT);
                itemMap.put("priceUnit", saleUnit);
            }
        }
        ItemPromotionResp itemPromotionResp = itemDTO.getItemPromotionResp();
        itemMap.put("itemPromotionResp", itemPromotionResp);
        if(itemDTO.getTargetSkuId()!=null){
            itemMap.put("skuId",itemDTO.getTargetSkuId());
        }

        //运营配置的数据

        return itemMap;
    }

    public static String toMonthlySalesView(String monthSalesAmount) {
        if (Integer.valueOf(monthSalesAmount) < 10000) {
            return monthSalesAmount;
        }
        if(Integer.valueOf(monthSalesAmount) < 1000000) {

            float tenThousands = Float.valueOf(monthSalesAmount) / Float.valueOf(10000);
            DecimalFormat decimalFormat = new DecimalFormat(".0");//构造方法的字符格式这里如果小数不足2位,会以0补足.
            String monthlySalesView = decimalFormat.format(tenThousands);
            return monthlySalesView + "万";
        }

        float tenThousands = Float.valueOf(monthSalesAmount) / Float.valueOf(10000);
        DecimalFormat decimalFormat = new DecimalFormat("#");//超过100万不展示小数部分
        String monthlySalesView = decimalFormat.format(tenThousands);
        return monthlySalesView + "万";
    }
}
