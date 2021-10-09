package com.tmall.wireless.tac.biz.processor.extremeItem;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.taobao.tair.impl.mc.MultiClusterTairManager;
import com.tcls.mkt.atmosphere.model.response.ItemPromotionResp;
import com.tmall.aselfcaptain.item.constant.BizAttributes;
import com.tmall.aselfcaptain.item.model.ItemDTO;
import com.tmall.aselfcaptain.util.StackTraceUtil;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkItemService;
import com.tmall.txcs.gs.spi.recommend.TairFactorySpi;
import com.tmall.wireless.store.spi.render.RenderSpi;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.SupermarketHallContext;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.service.SupermarketHallIGraphSearchService;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.service.SupermarketHallRenderService;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.util.Logger;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.util.LoggerProxy;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemConfig;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemConfigGroup;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemConfigGroups;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemConfigs;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.service.GroupSortDomainService;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.service.ItemPickService;
import com.tmall.wireless.tac.biz.processor.extremeItem.service.ItemGmvService;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import com.tmall.wireless.tac.client.handler.TacReactiveHandler4Ald;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Created from template by 言武 on 2021-09-10 14:36:48.
 *
 */

@Component
public class ExtremeItemSdkItemHandler extends TacReactiveHandler4Ald {

    Logger logger = LoggerProxy.getLogger(ExtremeItemSdkItemHandler.class);

    public static final int ASG_NAMESPACE = 625;
    private static final Integer tenThousand = 10000;
    @Autowired
    ShoppingguideSdkItemService shoppingguideSdkItemService;
    @Autowired
    TacLogger tacLogger;
    @Autowired
    RenderSpi renderSpi;
    @Autowired
    GroupSortDomainService groupSortDomainService;
    @Autowired
    ItemPickService itemPickService;
    @Autowired
    SupermarketHallRenderService supermarketHallRenderService;
    @Autowired
    SupermarketHallIGraphSearchService supermarketHallIGraphSearchService;
    @Autowired
    ItemGmvService itemGmvService;

    /*@Autowired
    TairFactorySpi tairFactorySpi;*/


    @Override
    public Flowable<TacResult<List<GeneralItem>>> executeFlowable(RequestContext4Ald requestContext4Ald) throws Exception {
        try {
            Long totalStart = System.currentTimeMillis();
            //初始化SupermarketHallContext
            SupermarketHallContext supermarketHallContext = SupermarketHallContext.init(requestContext4Ald);

            //构造运营配置商品列表领域对象
            ItemConfigs itemConfigs = ItemConfigs.valueOf(supermarketHallContext.getAldManualConfigDataList());

            //将运营配置的商品进行拆分组
            ItemConfigGroups itemConfigGroups = itemConfigs.splitGroup();

            //获取需要进行渲染的商品ID列表
            List<Long> itemIds = itemConfigs.extractItemIds();
            logger.info("==========itemIds: " + JSON.toJSONString(itemIds));

            //查询captain获取商品渲染信息
            Map<Long, ItemDTO> itemDTOMap = supermarketHallRenderService.batchQueryItem(itemIds, supermarketHallContext);
            logger.info("==========itemDTOs: " + JSON.toJSONString(itemDTOMap));

            //进行组间排序
            groupSortDomainService.groupSort(itemConfigGroups, itemIds, supermarketHallContext);

            //构建"商品->是否售光"Map，供组内选品时库存过滤使用
            Map<Long, Boolean> itemSoldOutMap = itemDTOMap.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> (e.getValue().isSoldout() || !e.getValue().isCanBuy())));
            logger.info("==========itemSoldOutMap: " + JSON.toJSONString(itemSoldOutMap));

            //组内曝光比例选品
            Map<Integer, ItemConfig> afterPickGroupMap = itemPickService.pickItems(itemConfigGroups, itemSoldOutMap);
            logger.info("==========afterPickGroupMap: " + JSON.toJSONString(afterPickGroupMap));

            //构建响应对象
            List<GeneralItem> generalItems = buildResult(itemConfigGroups, afterPickGroupMap, itemDTOMap, itemSoldOutMap);
            logger.info("=========generalItems:" + JSON.toJSONString(generalItems));

            Long totalEnd = System.currentTimeMillis();
            HadesLogUtil.stream("ExtremeItemSdkItemHandler.mainProcess|totalCost|" + (totalEnd - totalStart))
                    .kv("totalCost", String.valueOf(totalEnd - totalStart))
                    .error();
            HadesLogUtil.stream("ExtremeItemSdkItemHandler.mainProcess|success")
                    .kv("totalCost", String.valueOf(totalEnd - totalStart))
                    .error();

            return Flowable.just(TacResult.newResult(generalItems));

        } catch (Exception e) {
            HadesLogUtil.stream("ExtremeItemSdkItemHandler.mainProcess|error")
                    .kv("context", JSON.toJSONString(requestContext4Ald))
                    .kv("errorMsg", StackTraceUtil.stackTrace(e))
                    .error();
            logger.error(e.getMessage(), e);
        }
        return Flowable.just(TacResult.newResult(new ArrayList<>()));
    }

    private List<GeneralItem> buildResult(ItemConfigGroups itemConfigGroups, Map<Integer, ItemConfig> afterPickGroupMap, Map<Long, ItemDTO> longItemDTOMap, Map<Long, Boolean> itemSoldOutMap) {
        List<GeneralItem> result = new ArrayList<>();
        List<ItemConfigGroup> bottomItems = new ArrayList<>();
        for (ItemConfigGroup itemConfigGroup : itemConfigGroups.getItemConfigGroupList()) {
            if(afterPickGroupMap.get(itemConfigGroup.getGroupNo()) != null && itemSoldOutMap.get(afterPickGroupMap.get(itemConfigGroup.getGroupNo()).getItemId())) {
                bottomItems.add(itemConfigGroup);
            } else {
                result.add(buildItemMap(itemConfigGroup, longItemDTOMap, afterPickGroupMap));
            }
        }
        for (ItemConfigGroup itemConfigGroup : bottomItems) {
            result.add(buildItemMap(itemConfigGroup, longItemDTOMap, afterPickGroupMap));
        }
        return result;
    }

    public GeneralItem buildItemMap(ItemConfigGroup itemConfigGroup, Map<Long, ItemDTO> longItemDTOMap, Map<Integer, ItemConfig> afterPickGroupMap) {
        ItemConfig itemConfig = afterPickGroupMap.get(itemConfigGroup.getGroupNo());
        ItemDTO itemDTO = longItemDTOMap.get(itemConfig.getItemId());
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
        if(StringUtils.isNotBlank(itemConfig.getActivityId()) && StringUtils.isNotBlank(itemConfig.getCouponValue())) {
            itemMap.put("activityId", itemConfig.getActivityId());
            itemMap.put("couponValue", itemConfig.getCouponValue());
            itemMap.put("sellerId", "725677994");
        }
        if(StringUtils.isNotBlank(itemConfig.getItemDescCustom())) {
            itemMap.put("itemDescCustom", itemConfig.getItemDescCustom());
        }

        if(StringUtils.isNotBlank(itemConfig.getItemImg())) {
            itemMap.put("itemImg", itemConfig.getItemImg());
        }

        if(StringUtils.isNotBlank(itemConfig.getItemName())) {
            itemMap.put("shortTitle", itemConfig.getItemName());
        }

        return itemMap;
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
