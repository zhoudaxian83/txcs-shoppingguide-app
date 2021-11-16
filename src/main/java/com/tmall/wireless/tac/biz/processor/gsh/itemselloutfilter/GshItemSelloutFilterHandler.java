package com.tmall.wireless.tac.biz.processor.gsh.itemselloutfilter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.alibaba.fastjson.JSON;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.taobao.eagleeye.EagleEye;
import com.tcls.mkt.atmosphere.model.response.ItemPromotionResp;
import com.tmall.aselfcaptain.item.constant.BizAttributes;
import com.tmall.aselfcaptain.item.constant.Channel;
import com.tmall.aselfcaptain.item.model.ItemDTO;
import com.tmall.aselfcaptain.item.model.ItemId;
import com.tmall.aselfcaptain.item.model.ItemId.ItemType;
import com.tmall.aselfcaptain.item.model.ItemQueryDO;
import com.tmall.aselfcaptain.item.model.QueryOptionDO;
import com.tmall.tcls.gs.sdk.biz.uti.MapUtil;
import com.tmall.tmallwireless.tac.spi.context.SPIResult;
import com.tmall.wireless.store.spi.render.RenderSpi;
import com.tmall.wireless.store.spi.render.model.RenderRequest;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.SupermarketHallContext;
import com.tmall.wireless.tac.biz.processor.huichang.common.utils.PageUrlUtil;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import com.tmall.wireless.tac.client.handler.TacReactiveHandler4Ald;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author wangguohui
 */
@Component
public class GshItemSelloutFilterHandler extends TacReactiveHandler4Ald {

    Logger logger = LoggerFactory.getLogger(GshItemSelloutFilterHandler.class);

    private static final Integer tenThousand = 10000;

    @Autowired
    RenderSpi renderSpi;

    private final String captainSceneCode = "conference.gsh.common";


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
        Map<Long, Map<String, Object>> staticMap = new HashMap<>();
        for(Map<String, Object> data : aldManualConfigDataList){
            String contentId = MapUtil.getStringWithDefault(data, "contentId", "");
            if(StringUtils.isNotEmpty(contentId)){
                itemIdList.add(Long.valueOf(contentId));
                staticMap.put(Long.valueOf(contentId), data);
            }
        }
        Map<Long, ItemDTO> captainItemMap = batchQueryItem(itemIdList, supermarketHallContext);

        List<GeneralItem> list = new ArrayList<>();
        List<GeneralItem> sellOutList = new ArrayList<>();
        for (int a = 0; a < itemIdList.size(); a++) {
            ItemDTO itemDTO = captainItemMap.get(itemIdList.get(a));

            GeneralItem itemMap = new GeneralItem();
            buildItemDTO(itemMap, itemDTO);
            //静态数据补充
            Map<String, Object> stringObjectMap = staticMap.get(itemIdList.get(a));
            if (stringObjectMap != null) {
                for (Map.Entry<String, Object> entry : stringObjectMap.entrySet()) {
                    itemMap.put(entry.getKey(), entry.getValue());
                }
            }
            if (itemDTO == null || itemDTO.isSoldout() || !itemDTO.isCanBuy()) {
                sellOutList.add(itemMap);
            }else {
                list.add(itemMap);
            }
        }
        list.addAll(sellOutList);

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
        //itemMap.put("locType", itemDTO.getLocType().name());
        itemMap.put("sellerId", itemDTO.getSellerId());
        itemMap.put("sellOut", itemDTO.isSoldout());
        itemMap.put("canBuy", itemDTO.isCanBuy());

        ItemPromotionResp itemPromotionResp = itemDTO.getItemPromotionResp();
        itemMap.put("itemPromotionResp", itemPromotionResp);
        if(itemDTO.getTargetSkuId()!=null){
            itemMap.put("skuId",itemDTO.getTargetSkuId());
        }
        itemMap.put("attachments", itemDTO.getAttachments());

        //够实惠修改
        Object itemUrl = itemMap.get("itemUrl");
        try {
            if(itemUrl != null){
                String newItemUrl = PageUrlUtil.removeParam(String.valueOf(itemUrl), "locType");
                itemMap.put("itemUrl", newItemUrl);
            }
        }catch (Exception e){
            logger.error("去除locType异常, url:" + itemUrl);
        }

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


    public Map<Long, ItemDTO> batchQueryItem(List<Long> itemIdList, SupermarketHallContext supermarketHallContext) {

        Map<Long, ItemDTO> captainItemMap = Maps.newHashMap();

        final Object rpcContext = EagleEye.currentRpcContext();
        final long callerId = Thread.currentThread().getId();

        if (CollectionUtils.isEmpty(itemIdList)) {
            logger.info("batchQueryItem start, itemIdList empty");
            return captainItemMap;
        }

        Lists.partition(itemIdList, 20)
            .parallelStream()
            .map(list -> {
                try {
                    EagleEye.setRpcContext(rpcContext);
                    Map<Long, ItemDTO> longItemDTOMap = queryItem(list, supermarketHallContext);
                    if (MapUtils.isEmpty(longItemDTOMap)) {
                        logger.info("GshItemSelloutFilterHandler.batch query capatin empty;" + JSON.toJSONString(list));
                    }
                    return longItemDTOMap;
                } catch (Exception e) {
                    logger.error("GshItemSelloutFilterHandler.batchQueryItem_catchException", e);
                    return new HashMap<Long, ItemDTO>();
                } finally {
                    if (Thread.currentThread().getId() != callerId) {
                        EagleEye.clearRpcContext();
                    }
                }
            })
            .forEach(e -> captainItemMap.putAll(e));

        return captainItemMap;
    }

    private Map<Long, ItemDTO> queryItem(List<Long> itemIds, SupermarketHallContext supermarketHallContext) {
        String traceId = EagleEye.getTraceId();
        RenderRequest renderRequest = buildRenderRequest(itemIds, supermarketHallContext.getSmAreaId(),null, supermarketHallContext.getUserId(), supermarketHallContext);
        logger.error("GshItemSelloutFilterHandler.traceId:{}, recourceId:{}, captainRequest:{}", traceId, supermarketHallContext.getCurrentResourceId(), JSON.toJSONString(renderRequest));
        SPIResult<List<ItemDTO>> itemDTOs = renderSpi.query(renderRequest);
        logger.error("GshItemSelloutFilterHandler.traceId:{}, recourceId:{}, captainReponse:{}", traceId, supermarketHallContext.getCurrentResourceId(), JSON.toJSONString(itemDTOs));
        return itemDTOs.getData().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    }

    RenderRequest buildRenderRequest(List<Long> itemIds, String smAreaId, String queryTime, Long userId, SupermarketHallContext supermarketHallContext) {
        RenderRequest renderRequest = new RenderRequest();
        ItemQueryDO query = new ItemQueryDO();

        if (org.apache.commons.lang.StringUtils.isNotBlank(smAreaId) && !"0".equals(smAreaId)) {
            query.setAreaId(Long.valueOf(smAreaId));
        } else {
            logger.debug("smAreaId is null" + smAreaId);
            return null;
        }
        List<ItemId> itemIdList = itemIds.stream()
            .map(itemId -> ItemId.valueOf(itemId, ItemType.GSH))
            .collect(Collectors.toList());
        query.setItemIds(itemIdList);

        query.setChannel(Channel.WAP);
        if(org.apache.commons.lang.StringUtils.isNotEmpty(queryTime)){
            query.setQueryTime(queryTime);
        }

        if (userId != null && userId != 0) {
            query.setBuyerId(userId);
        }

        query.setSource("txcs-shoppingguide", "hall");

        QueryOptionDO option = new QueryOptionDO();
        option.setOpenMkt(true);
        if(org.apache.commons.lang.StringUtils.isNotEmpty(supermarketHallContext.getSceneCode())){
            option.setSceneCode(supermarketHallContext.getSceneCode());
        }else {
            //会场默认
            option.setSceneCode("conference.promotion");
        }
        option.setIncludeQuantity(true);
        option.setIncludeSales(true);
        option.setIncludeMaiFanCard(true);
        option.setUserPromotion(false);
        renderRequest.setQuery(query);
        renderRequest.setOption(option);
        return renderRequest;
    }

}
