package com.tmall.wireless.tac.biz.processor.extremeItem;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.taobao.common.keycenter.security.Cryptograph;
import com.taobao.eagleeye.EagleEye;
import com.tcls.mkt.atmosphere.model.response.ItemPromotionResp;
import com.tmall.aselfcaptain.item.constant.BizAttributes;
import com.tmall.aselfcaptain.item.model.ItemDTO;
import com.tmall.aselfcaptain.util.StackTraceUtil;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkItemService;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.SupermarketHallContext;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.service.SupermarketHallBottomService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.tmall.wireless.tac.biz.processor.extremeItem.common.config.SupermarketHallSwitch.extremeItemCouponAsac;
import static com.tmall.wireless.tac.biz.processor.extremeItem.common.config.SupermarketHallSwitch.openPriceFuzzy;


/**
 * Created from template by ?????? on 2021-09-10 14:36:48.
 *
 */

@Component
public class ExtremeItemSdkItemHandler extends TacReactiveHandler4Ald {

    Logger logger = LoggerProxy.getLogger(ExtremeItemSdkItemHandler.class);

    private static final Integer tenThousand = 10000;
    private static final Integer oneBillion = 1000000;
    private static final String captainSceneCode = "conference.zhj";
    private static final String SELLER_ID = "725677994";
    private static final String KEY_CENTER_KEY = "growth-os-service_ump_draw_key";
    @Resource
    private Cryptograph cryptograph;

    @Autowired
    ShoppingguideSdkItemService shoppingguideSdkItemService;
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

    @Autowired
    SupermarketHallBottomService supermarketHallBottomService;

    @Override
    public Flowable<TacResult<List<GeneralItem>>> executeFlowable(RequestContext4Ald requestContext4Ald) throws Exception {
        Long mainProcessStart = System.currentTimeMillis();
        SupermarketHallContext supermarketHallContext = new SupermarketHallContext();
        List<GeneralItem> generalItems = new ArrayList<>();
        try {
            //?????????SupermarketHallContext
            supermarketHallContext = SupermarketHallContext.init(requestContext4Ald);

            //????????????????????????????????????????????????
            supermarketHallContext.setSceneCode(captainSceneCode);

            //??????????????????????????????????????????
            ItemConfigs itemConfigs = ItemConfigs.valueOf(supermarketHallContext.getAldManualConfigDataList());

            //???????????????????????????????????????
            ItemConfigGroups itemConfigGroups = itemConfigs.splitGroup();

            //?????????????????????????????????ID??????
            List<Long> itemIds = itemConfigs.extractItemIds();
            logger.info("==========itemIds: " + JSON.toJSONString(itemIds));

            //??????captain????????????????????????
            Map<Long, ItemDTO> itemDTOMap = supermarketHallRenderService.batchQueryItem(itemIds, supermarketHallContext);

            //??????????????????
            groupSortDomainService.groupSort(itemConfigGroups, itemIds, supermarketHallContext);

            //??????"??????->????????????"Map???????????????????????????????????????
            Map<Long, Boolean> itemSoldOutMap = itemDTOMap.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> (e.getValue().isSoldout() || !e.getValue().isCanBuy())));
            logger.info("==========itemSoldOutMap: " + JSON.toJSONString(itemSoldOutMap));

            //????????????????????????
            Map<Integer, ItemConfig> afterPickGroupMap = itemPickService.pickItems(itemConfigGroups, itemSoldOutMap);

            //??????????????????
            generalItems = buildResult(itemConfigGroups, afterPickGroupMap, itemDTOMap, itemSoldOutMap);

            //???????????????
            if(CollectionUtils.isNotEmpty(generalItems) && generalItems.size() == itemConfigGroups.size()) {
                supermarketHallBottomService.writeBottomData(supermarketHallContext.getCurrentResourceId(), supermarketHallContext.getCurrentScheduleId(), generalItems);
                Long mainProcessEnd = System.currentTimeMillis();
                HadesLogUtil.stream("ExtremeItemSdkItemHandler|mainProcess|" + Logger.isEagleEyeTest() + "|success|" + (mainProcessEnd - mainProcessStart))
                        .error();
            } else {
                HadesLogUtil.stream("ExtremeItemSdkItemHandler|mainProcess|" + Logger.isEagleEyeTest() + "|error")
                        .kv("configSize", String.valueOf(itemConfigGroups.size()))
                        .kv("actualSize", String.valueOf(generalItems.size()))
                        .kv("curPageUrl", supermarketHallContext.getCurrentPageUrl())
                        .kv("resourceId", supermarketHallContext.getCurrentResourceId())
                        .kv("scheduleId", supermarketHallContext.getCurrentScheduleId())
                        .error();
                generalItems = supermarketHallBottomService.readBottomData(supermarketHallContext.getCurrentResourceId(), supermarketHallContext.getCurrentScheduleId());
            }
        } catch (Exception e) {
            HadesLogUtil.stream("ExtremeItemSdkItemHandler|mainProcess|" + Logger.isEagleEyeTest() + "|exception")
                    .kv("errorMsg", StackTraceUtil.stackTrace(e))
                    .kv("curPageUrl", supermarketHallContext.getCurrentPageUrl())
                    .kv("resourceId", supermarketHallContext.getCurrentResourceId())
                    .kv("scheduleId", supermarketHallContext.getCurrentScheduleId())
                    .error();
            logger.error("ExtremeItemSdkItemHandler error, traceId:" + EagleEye.getTraceId(), e);
            logger.error("ExtremeItemSdkItemHandler error, traceID:{}, requestContext4Ald:{}", EagleEye.getTraceId(), JSON.toJSONString(requestContext4Ald));

            generalItems = supermarketHallBottomService.readBottomData(supermarketHallContext.getCurrentResourceId(), supermarketHallContext.getCurrentScheduleId());
        }
        return Flowable.just(TacResult.newResult(generalItems));
    }

    private List<GeneralItem> buildResult(ItemConfigGroups itemConfigGroups, Map<Integer, ItemConfig> afterPickGroupMap, Map<Long, ItemDTO> longItemDTOMap, Map<Long, Boolean> itemSoldOutMap) {
        List<GeneralItem> result = new ArrayList<>();
        List<ItemConfigGroup> bottomItems = new ArrayList<>();
        for (ItemConfigGroup itemConfigGroup : itemConfigGroups.getItemConfigGroupList()) {
            Boolean soldOut = itemSoldOutMap.get(afterPickGroupMap.get(itemConfigGroup.getGroupNo()).getItemId());
            if(afterPickGroupMap.get(itemConfigGroup.getGroupNo()) != null && soldOut != null && soldOut) {
                bottomItems.add(itemConfigGroup);
            } else {
                GeneralItem generalItem = buildItemMap(itemConfigGroup, longItemDTOMap, afterPickGroupMap);
                if(generalItem != null) {
                    result.add(generalItem);
                }
            }
        }
        for (ItemConfigGroup itemConfigGroup : bottomItems) {
            GeneralItem generalItem = buildItemMap(itemConfigGroup, longItemDTOMap, afterPickGroupMap);
            if(generalItem != null) {
                result.add(generalItem);
            }
        }
        return result;
    }

    public GeneralItem buildItemMap(ItemConfigGroup itemConfigGroup, Map<Long, ItemDTO> longItemDTOMap, Map<Integer, ItemConfig> afterPickGroupMap) {
        ItemConfig itemConfig = afterPickGroupMap.get(itemConfigGroup.getGroupNo());
        ItemDTO itemDTO = longItemDTOMap.get(itemConfig.getItemId());
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

        if(openPriceFuzzy && StringUtils.isNotBlank(itemDTO.getFuzzySellCount()) && !"0".equals(itemDTO.getFuzzySellCount())) {
            itemMap.put("itemMonthSoldCount", itemDTO.getFuzzySellCount());
            itemMap.put("fuzzySellCount", itemDTO.getFuzzySellCount());
        }

        itemMap.put("itemUrl", itemDTO.getDetailUrl());
        //itemMap.put("scm", getScm1(tmcsContext, String.valueOf(itemDTO.getItemId().getId())));
        itemMap.put("_areaSellable", !itemDTO.isSoldout() && itemDTO.isCanBuy());
        itemMap.put("locType", itemDTO.getLocType().name());
        itemMap.put("sellerId", itemDTO.getSellerId());

        //itemMap.put("storeId", tmcsContext.getStoreId());
        //??????url????????????

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

        //?????????????????????
        if(StringUtils.isNotBlank(itemConfig.getActivityId()) && StringUtils.isNotBlank(itemConfig.getCouponValue())) {
            itemMap.put("activityId", itemConfig.getActivityId());
            itemMap.put("couponValue", itemConfig.getCouponValue());
            itemMap.put("sellerId", "725677994");
            String token = buildToken(itemConfig.getActivityId());
            itemMap.put("token", token);
            itemMap.put("asac", extremeItemCouponAsac);
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

        if(StringUtils.isNotBlank(itemConfig.getItemCarouselDesc())) {
            itemMap.put("itemCarouselDesc", itemConfig.getItemCarouselDesc());
        }

        return itemMap;
    }

    private String buildToken(String activityId) {
        if(StringUtils.isBlank(activityId)) {
            return null;
        }
        String rawToken = "sellerId=" + SELLER_ID + ";activityId=" + activityId  + ";asac=" + extremeItemCouponAsac;
        logger.info("rawToken:" + rawToken);
        return keyCenterEncrypt(rawToken);
    }

    private String keyCenterEncrypt(String rawToken) {
        String cipherText = cryptograph.encrypt(rawToken, KEY_CENTER_KEY);
        logger.info("cipherText:" + cipherText);
        return cipherText;
    }

    public String toMonthlySalesView(String monthSalesAmount) {
        if (Integer.valueOf(monthSalesAmount) < tenThousand) {
            return monthSalesAmount;
        }
        if(Integer.valueOf(monthSalesAmount) < oneBillion) {

            float tenThousands = Float.valueOf(monthSalesAmount) / Float.valueOf(tenThousand);
            DecimalFormat decimalFormat = new DecimalFormat(".0");//???????????????????????????????????????????????????2???,??????0??????.
            String monthlySalesView = decimalFormat.format(tenThousands);
            return monthlySalesView + "???";
        }

        float tenThousands = Float.valueOf(monthSalesAmount) / Float.valueOf(tenThousand);
        DecimalFormat decimalFormat = new DecimalFormat("#");//??????100????????????????????????
        String monthlySalesView = decimalFormat.format(tenThousands);
        return monthlySalesView + "???";
    }
}
