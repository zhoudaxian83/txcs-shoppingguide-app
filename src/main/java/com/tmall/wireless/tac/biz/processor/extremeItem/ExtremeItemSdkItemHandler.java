package com.tmall.wireless.tac.biz.processor.extremeItem;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.alibaba.aladdin.lamp.sdk.solution.context.SolutionContext;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.taobao.eagleeye.EagleEye;
import com.taobao.igraph.client.model.*;
import com.tcls.mkt.atmosphere.model.response.ItemPromotionResp;
import com.tmall.aselfcaptain.common.model.promotion.ItemPromotionCluster;
import com.tmall.aselfcaptain.item.constant.BizAttributes;
import com.tmall.aselfcaptain.item.constant.Channel;
import com.tmall.aselfcaptain.item.model.ItemDTO;
import com.tmall.aselfcaptain.item.model.ItemId;
import com.tmall.aselfcaptain.item.model.ItemQueryDO;
import com.tmall.aselfcaptain.item.model.QueryOptionDO;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkItemService;
import com.tmall.tmallwireless.tac.spi.context.SPIResult;
import com.tmall.wireless.store.spi.render.RenderSpi;
import com.tmall.wireless.store.spi.render.model.RenderRequest;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemConfig;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemConfigGroup;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemConfigGroups;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemConfigs;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.service.ItemPickService;
import com.tmall.wireless.tac.biz.processor.huichang.inventory.InventoryEntranceModule.InventoryEntranceModuleHandler;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import com.tmall.wireless.tac.client.handler.TacReactiveHandler4Ald;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallCommonAldConstant.STATIC_SCHEDULE_DATA;


/**
 * Created from template by 言武 on 2021-09-10 14:36:48.
 *
 */

@Component
public class ExtremeItemSdkItemHandler extends TacReactiveHandler4Ald {

    Logger logger = LoggerFactory.getLogger(ExtremeItemSdkItemHandler.class);
    @Autowired
    ShoppingguideSdkItemService shoppingguideSdkItemService;
    @Autowired
    TacLogger tacLogger;
    @Autowired
    RenderSpi renderSpi;
    @Autowired
    ItemPickService itemPickService;
    @Autowired
    com.taobao.igraph.client.core.IGraphClientWrap iGraphClientWrap;

    @Override
    public Flowable<TacResult<List<GeneralItem>>> executeFlowable(RequestContext4Ald requestContext4Ald) throws Exception {
        try {
            logger.warn("=====context:" + JSON.toJSONString(requestContext4Ald));
            //tacLogger.info("context:" + JSON.toJSONString(requestContext4Ald));
            List<Map<String, Object>> aldDataList = (List<Map<String, Object>>) requestContext4Ald.getAldContext().get(STATIC_SCHEDULE_DATA);
            //tacLogger.info("aldDataList:" + aldDataList);
            logger.warn("aldDataList:" + aldDataList);
            ItemConfigs itemConfigs = ItemConfigs.valueOf(aldDataList);
            //tacLogger.info("itemConfigs:" + JSON.toJSONString(itemConfigs));
            logger.warn("itemConfigs:" + JSON.toJSONString(itemConfigs));
            itemConfigs.checkItemConfig();
            ItemConfigGroups itemConfigGroups = itemConfigs.splitGroup();
            //tacLogger.info("itemConfigGroups:" + JSON.toJSONString(itemConfigGroups));
            logger.warn("itemConfigGroupList:" + JSON.toJSONString(itemConfigGroups));
            itemConfigGroups.sortGroup();
            //tacLogger.info("==========after sort itemConfigGroupList:" + JSON.toJSONString(itemConfigGroupList));
            logger.warn("==========after sort itemConfigGroupList:" + JSON.toJSONString(itemConfigGroups));

            //查询captain
            List<Long> itemIds = itemConfigs.extractItemIds();
            tacLogger.info("==========itemIds: " + JSON.toJSONString(itemIds));
            logger.warn("==========itemIds: " + JSON.toJSONString(itemIds));
            Map<Long, ItemDTO> longItemDTOMap = batchQueryItem(itemIds);
            //tacLogger.info("==========itemDTOs: " + JSON.toJSONString(longItemDTOMap));
            logger.warn("==========itemDTOs: " + JSON.toJSONString(longItemDTOMap));

            Map<Long, Boolean> inventoryMap = longItemDTOMap.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().isSoldout()));
            //tacLogger.info("==========inventoryMap: " + JSON.toJSONString(inventoryMap));
            logger.info("==========inventoryMap: " + JSON.toJSONString(inventoryMap));

            Map<Integer, ItemConfig> afterPickGroupMap = itemPickService.pickItems(itemConfigGroups, inventoryMap);
            tacLogger.info("==========afterPickGroupMap: " + JSON.toJSONString(afterPickGroupMap));
            logger.info("==========afterPickGroupMap: " + JSON.toJSONString(afterPickGroupMap));

            List<GeneralItem> generalItems = buildResult(itemConfigGroups, afterPickGroupMap, longItemDTOMap, inventoryMap);

            doPGSearch("TPP_tmall_sm_tmcs_item_gmv_history", "552982987824");

            logger.warn("=========generalItems:" + JSON.toJSONString(generalItems));
            return Flowable.just(TacResult.newResult(generalItems));

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            tacLogger.error(e.getMessage(), e);
        }
        return Flowable.just(TacResult.newResult(new ArrayList<>()));
    }

    private List<GeneralItem> buildResult(ItemConfigGroups itemConfigGroups, Map<Integer, ItemConfig> afterPickGroupMap, Map<Long, ItemDTO> longItemDTOMap, Map<Long, Boolean> inventoryMap) {
        List<GeneralItem> result = new ArrayList<>();
        for (ItemConfigGroup itemConfigGroup : itemConfigGroups.getItemConfigGroups()) {
            GeneralItem generalItem = new GeneralItem();
            generalItem.put("groupNo", itemConfigGroup.getGroupNo());
            generalItem.put("item", buildItemMap(longItemDTOMap.get(afterPickGroupMap.get(itemConfigGroup.getGroupNo()).getItemId())));
            result.add(generalItem);
        }
        return result;
    }

    public RenderRequest buildRenderRequest(List<Long> itemIds, Long buyerId, Long areaId) {
        RenderRequest renderRequest = new RenderRequest();
        ItemQueryDO query = new ItemQueryDO();
        List<ItemId> itemIdList = itemIds.stream()
                .map(itemId -> ItemId.valueOf(itemId, ItemId.ItemType.B2C))
                .collect(Collectors.toList());
        query.setItemIds(itemIdList);
        query.setBuyerId(buyerId);
        query.setSource("txcs-shoppingguide");
        query.setChannel(Channel.WAP);
        //query.setLocationId(storeId);
        query.setAreaId(areaId);
        QueryOptionDO option = new QueryOptionDO();
        /*if (StringUtils.isNotEmpty(itemInfoSourceMetaInfo.getUmpChannelKey())) {
            Map<String, String> extraParams = Maps.newHashMap();
            extraParams.put("umpChannel", itemInfoSourceMetaInfo.getUmpChannelKey());
            query.setExtraParams(extraParams);
        }*/

        option.setIncludeQuantity(true);
        option.setIncludeSales(true);
        option.setIncludeItemTags(true);
        option.setIncludeItemFeature(true);
        option.setIncludeMaiFanCard(true);
        option.setIncludeTiming(true);
        /*if (StringUtils.isNotEmpty(itemInfoSourceMetaInfo.getMktSceneCode())) {
            option.setSceneCode(itemInfoSourceMetaInfo.getMktSceneCode());
            option.setOpenMkt(true);
        }*/

        renderRequest.setQuery(query);
        renderRequest.setOption(option);
        return renderRequest;
    }

    Map<Long, ItemDTO> batchQueryItem(List<Long> itemIdList) {

        tacLogger.info("batchQueryItem start");

        Map<Long, ItemDTO> captainItemMap = Maps.newHashMap();

        final Object rpcContext = EagleEye.currentRpcContext();
        final long callerId = Thread.currentThread().getId();

        if (CollectionUtils.isEmpty(itemIdList)) {
            tacLogger.info("batchQueryItem start, itemIdList empty");
            return captainItemMap;
        }

        Lists.partition(itemIdList, 20)
                .parallelStream()
                .map(list -> {
                    try {
                        EagleEye.setRpcContext(rpcContext);
                        Map<Long, ItemDTO> longItemDTOMap = queryItem(list);
                        if (MapUtils.isEmpty(longItemDTOMap)) {
                            tacLogger.info("batch query capatin empty;" + JSON.toJSONString(list));
                        }
                        return longItemDTOMap;
                    } catch (Exception e) {
                        tacLogger.error("batchQueryItem_catchException", e);
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

    private Map<Long, ItemDTO> queryItem(List<Long> itemIds) {
        RenderRequest renderRequest = buildRenderRequest(itemIds, 0L, 330110L);
        SPIResult<List<ItemDTO>> itemDTOs = renderSpi.query(renderRequest);
        return itemDTOs.getData().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    }

    public Map<String, Object> buildItemMap(ItemDTO itemDTO) {
        Map<String, Object> itemMap = Maps.newHashMap();
        if (true) {
            buildItemDTO(itemMap, itemDTO);
        } else {
            itemMap.put("itemId", itemDTO.getId());
            itemMap.put("itemImg", itemDTO.getWhitePic());
            itemMap.put("itemMPrice", itemDTO.getDisplayPrice());
            //itemMap.put("storeId", tmContext.getStoreId());
            itemMap.put("shortTitle", itemDTO.getShortTitle());
            itemMap.put("itemUrl", itemDTO.getDetailUrl());
            itemMap.put("_areaSellable", !itemDTO.isSoldout() && itemDTO.isCanBuy());
            itemMap.put("locType", "B2C");

            itemMap.put("reservePrice", itemDTO.getReservePrice());

            //买赠
            ItemPromotionCluster itemPromotionCluster = itemDTO.getItemPromotionCluster();
            if (StringUtils.isBlank((String) itemMap.get("itemDesc"))) {
                //店铺优惠
                if (itemPromotionCluster != null && itemPromotionCluster.getGiftPromotionDTO() != null) {
                    itemMap.put("itemDesc", itemPromotionCluster.getGiftPromotionDTO().getCopywriting());
                } else {
                    //店铺优惠
                    List<String> shopPromotionList = itemDTO.getShopPromotionList();
                    if (CollectionUtils.isNotEmpty(shopPromotionList)) {
                        String itemDesc = shopPromotionList.get(0);
                        String[] st = itemDesc.split(";");
                        if (!st[0].startsWith("【超值换购")) {
                            itemMap.put("itemDesc", st[0]);
                        }
                    }
                }
            }

            itemMap.put("chaoshiItemTitle", itemDTO.getShortTitle());
        }

        return itemMap;
    }

    public void buildItemDTO(Map<String, Object> item, ItemDTO itemDTO) {
        //item.put("currentResourceId", tmcsContext.getCurrentResourceId());
        item.put("id", itemDTO.getItemId().getId());
        item.put("itemId", itemDTO.getItemId().getId());
        //item.put("storeId", tmcsContext.getStoreId());
        if (StringUtils.isBlank((String) item.get("selfSupportProperties"))) {
            item.put("selfSupportProperties", itemDTO.getSelfSupportProperties());
        }

        if (StringUtils.isBlank((String) item.get("chaoshiItemTitle"))) {
            //if ("O2O".equals(tmcsContext.getLocType())) {
            //    item.put("chaoshiItemTitle", itemDTO.getTitle());
            //} else {
                item.put("chaoshiItemTitle", itemDTO.getShortTitle());
            //}
        }

        if (StringUtils.isBlank((String) item.get("itemImg"))) {
            item.put("itemImg", itemDTO.getWhitePic());
        }

        if (StringUtils.isBlank((String) item.get("shortTitle"))) {
            item.put("shortTitle", itemDTO.getShortTitle());
        }
        if (StringUtils.isBlank((String) item.get("specDetail"))) {
            item.put("specDetail", itemDTO.getSpecDetail());
        }

        /*if (CommonSwitch.monthSoldCount) {
            String monthlySales = itemDTO.getAttributes().get(BizAttributes.ATTR_SALES_AMOUNT);
            if (org.apache.commons.lang3.StringUtils.isNotBlank(monthlySales) && Integer.valueOf(monthlySales) > 0) {
                item.put("itemMonthSoldCount", toMonthlySalesView(monthlySales));
                item.put("orignMonthSoldCount", monthlySales);
            }
        }*/
        item.put("itemUrl", itemDTO.getDetailUrl());
        //item.put("scm", getScm1(tmcsContext, String.valueOf(itemDTO.getItemId().getId())));
        item.put("_areaSellable", !itemDTO.isSoldout());
        item.put("locType", itemDTO.getLocType().name());
        item.put("sellerId", itemDTO.getSellerId());


        if (BizAttributes.TRUE.equals(MapUtils.getString(itemDTO.getAttributes(), BizAttributes.ATTR_IS_WEIGHT_ITEM))) {
            String weightAttrJson = MapUtils.getString(itemDTO.getAttributes(), BizAttributes.ATTR_WEIGHT_ITEM);
            if (org.apache.commons.lang.StringUtils.isNotBlank(weightAttrJson)) {
                JSONObject ob = JSON.parseObject(weightAttrJson);
                String saleUnit = ob.getString(BizAttributes.WeightAttr.SALE_UNIT);
                item.put("priceUnit", saleUnit);
            }
        }
        ItemPromotionResp itemPromotionResp = itemDTO.getItemPromotionResp();
        item.put("itemPromotionResp", itemPromotionResp);
        if(itemDTO.getTargetSkuId()!=null){
            item.put("skuId",itemDTO.getTargetSkuId());
        }
    }

    public void doPGSearch(String tableName, String searchKey) {
        // 查询语句构造
        AtomicQuery atomicQuery = new AtomicQuery(tableName, Arrays.asList(new KeyList(searchKey), new KeyList("44107699067")));
        atomicQuery.setReturnFields("gmv", "item_id", "window_start", "window_end");
        atomicQuery.setRange(0, 10);

        // 查询接口调用
        QueryResult queryResult;
        try {
            queryResult = iGraphClientWrap.search(atomicQuery);
        } catch (Exception e) {
            tacLogger.error("search failed", e);
            return;
        }
        List<SingleQueryResult> allQueryResult = queryResult.getAllQueryResult();
        tacLogger.info("=========allQueryResult:" + JSON.toJSONString(allQueryResult));

        // 查询结果读取
        SingleQueryResult singleQueryResult = queryResult.getSingleQueryResult();
        if (singleQueryResult.hasError()) {
            tacLogger.warn("oops, got errorMsg:["+singleQueryResult.getErrorMsg()+"]");
        }
        tacLogger.info("got ["+singleQueryResult.size()+"] records");
        for (MatchRecord matchRecord : singleQueryResult.getMatchRecords()) {
            // 注意,fieldValue/fieldValue2可能为null，后续逻辑使用时务必进行null值判断
            String fieldValue = matchRecord.getFieldValue(0, MatchRecord.EncodeType.UTF8);
            Double fieldValue2 = matchRecord.getDouble("gmv");
            tacLogger.info("fieldValue" + fieldValue);
            tacLogger.info("fieldValue2" + fieldValue2);
        }
    }
}
