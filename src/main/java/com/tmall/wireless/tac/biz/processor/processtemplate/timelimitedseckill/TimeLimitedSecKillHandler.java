package com.tmall.wireless.tac.biz.processor.processtemplate.timelimitedseckill;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tcls.mkt.atmosphere.model.response.ItemPromotionResp;
import com.tmall.aselfcaptain.item.constant.BizAttributes;
import com.tmall.aselfcaptain.item.model.ItemDTO;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemConfig;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemConfigGroup;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemConfigGroups;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.ProcessTemplateContext;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.ProcessTemplateRecommendService;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.ProcessTemplateRenderService;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend.ItemSetRecommendModelHandler;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend.RecommendModel;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend.RecommendResponseHandler;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import com.tmall.wireless.tac.client.handler.TacReactiveHandler4Ald;
import io.reactivex.Flowable;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TimeLimitedSecKillHandler extends TacReactiveHandler4Ald {

    @Autowired
    private ProcessTemplateRenderService renderService;

    @Autowired
    private ProcessTemplateRecommendService recommendService;

    @Autowired
    private TacLogger tacLogger;

    @Override
    public Flowable<TacResult<List<GeneralItem>>> executeFlowable(RequestContext4Ald requestContext4Ald) throws Exception {
        ProcessTemplateContext context = new ProcessTemplateContext();
        context.setUserId(1034513083L);
        Map<String, String> params = new HashMap<>();
        params.put("contentType", "3");
        params.put("itemSetIdList", "415609,415620");
        RecommendResponseHandler handler = new ItemSetRecommendModelHandler();
        RecommendModel recommendModel = recommendService.recommendContent(21557L, context, params, handler);
        tacLogger.warn("allItemIds" + JSON.toJSONString(recommendModel.getAllItemIds()));
        if(CollectionUtils.isNotEmpty(recommendModel.getAllItemIds())) {
            Map<Long, ItemDTO> longItemDTOMap = renderService.batchQueryItem(recommendModel.getAllItemIds(), context);
            tacLogger.warn("longItemDTOMap: " + JSON.toJSONString(longItemDTOMap));
            List<GeneralItem> generalItemList = buildResult(recommendModel.getAllItemIds(), longItemDTOMap);
            return Flowable.just(TacResult.newResult(generalItemList));
        }
        return Flowable.just(TacResult.newResult(null));
    }

    private List<GeneralItem> buildResult(List<Long> ids, Map<Long, ItemDTO> longItemDTOMap) {
        List<GeneralItem> result = new ArrayList<>();
        for (Long itemId : ids) {
            GeneralItem generalItem = buildItemMap(longItemDTOMap.get(itemId));
            if(generalItem != null) {
                result.add(generalItem);
            }
        }
        return result;
    }

    public GeneralItem buildItemMap(ItemDTO itemDTO) {
        if(itemDTO == null) {
            return null;
        }
        GeneralItem itemMap = new GeneralItem();
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

    public String toMonthlySalesView(String monthSalesAmount) {
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
