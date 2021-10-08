package com.tmall.wireless.tac.biz.processor.gsh.itemselloutfilter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;

import com.tcls.mkt.atmosphere.model.response.ItemPromotionResp;
import com.tmall.aselfcaptain.item.constant.BizAttributes;
import com.tmall.aselfcaptain.item.model.ItemDTO;
import com.tmall.tcls.gs.sdk.biz.uti.MapUtil;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.SupermarketHallContext;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.service.SupermarketHallRenderService;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import com.tmall.wireless.tac.client.handler.TacReactiveHandler4Ald;
import io.reactivex.Flowable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author wangguohui
 */
@Component
public class GshItemSelloutFilterHandler extends TacReactiveHandler4Ald {

    private static final Integer tenThousand = 10000;

    private final String captainSceneCode = "supermarket.hall.inventory";

    @Autowired
    SupermarketHallRenderService supermarketHallRenderService;

    @Override
    public Flowable<TacResult<List<GeneralItem>>> executeFlowable(RequestContext4Ald requestContext4Ald)
        throws Exception {
        SupermarketHallContext supermarketHallContext = SupermarketHallContext.init(requestContext4Ald);
        List<Map<String, Object>> aldManualConfigDataList = supermarketHallContext.getAldManualConfigDataList();
        supermarketHallContext.setSceneCode(captainSceneCode);
        if(aldManualConfigDataList == null){
            throw new Exception("数据未填写");
        }
        List<Long> itemIdList= new ArrayList<>();
        for(Map<String, Object> data : aldManualConfigDataList){
            String contentId = MapUtil.getStringWithDefault(data, "contentId", "");
            if(StringUtils.isNotEmpty(contentId)){
                itemIdList.add(Long.valueOf(contentId));
            }
        }
        Map<Long, ItemDTO> captainItemMap = supermarketHallRenderService.batchQueryItem(itemIdList, supermarketHallContext);
        List<GeneralItem> list = new ArrayList<>();
        for (int a = 0; a < itemIdList.size(); a++) {
            ItemDTO itemDTO = captainItemMap.get(itemIdList.get(a));
            GeneralItem itemMap = new GeneralItem();
            buildItemDTO(itemMap, itemDTO);
            list.add(itemMap);
        }

        return Flowable.just(TacResult.newResult(list));
    }


    public void buildItemDTO(GeneralItem itemMap, ItemDTO itemDTO) {
        itemMap.put("id", itemDTO.getItemId().getId());
        itemMap.put("itemId", itemDTO.getItemId().getId());
        if (org.apache.commons.lang.StringUtils.isBlank((String) itemMap.get("selfSupportProperties"))) {
            itemMap.put("selfSupportProperties", itemDTO.getSelfSupportProperties());
        }
        itemMap.put("chaoshiItemTitle", itemDTO.getTitle());

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
        if (StringUtils.isNotBlank(monthlySales) && Integer.valueOf(monthlySales) > 0) {
            itemMap.put("itemMonthSoldCount", toMonthlySalesView(monthlySales));
            itemMap.put("orignMonthSoldCount", monthlySales);
        }
        itemMap.put("itemUrl", itemDTO.getDetailUrl());
        itemMap.put("_areaSellable", !itemDTO.isSoldout());
        itemMap.put("locType", itemDTO.getLocType().name());
        itemMap.put("sellerId", itemDTO.getSellerId());


        ItemPromotionResp itemPromotionResp = itemDTO.getItemPromotionResp();
        itemMap.put("itemPromotionResp", itemPromotionResp);
        if(itemDTO.getTargetSkuId()!=null){
            itemMap.put("skuId",itemDTO.getTargetSkuId());
        }
        itemMap.put("attachments", itemDTO.getAttachments());

    }

    public String toMonthlySalesView(String monthSalesAmount) {
        if (Integer.valueOf(monthSalesAmount) < tenThousand) {
            return monthSalesAmount;
        }
        float tenThousands = Float.valueOf(monthSalesAmount) / Float.valueOf(tenThousand);
        DecimalFormat decimalFormat = new DecimalFormat(".0");//构造方法的字符格式这里如果小数不足2位,会以0补足.
        String monthlySalesView = decimalFormat.format(tenThousands);
        return monthlySalesView + "万";
    }

}
