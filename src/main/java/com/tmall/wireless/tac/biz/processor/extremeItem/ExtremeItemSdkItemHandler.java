package com.tmall.wireless.tac.biz.processor.extremeItem;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.alibaba.aladdin.lamp.sdk.solution.context.SolutionContext;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.txcs.common.util.FlogUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.taobao.eagleeye.EagleEye;
import com.taobao.igraph.client.model.*;
import com.tcls.mkt.atmosphere.model.response.ItemPromotionResp;
import com.tmall.aself.shoppingguide.client.loc.domain.AddressDTO;
import com.tmall.aselfcaptain.common.model.promotion.ItemPromotionCluster;
import com.tmall.aselfcaptain.item.constant.BizAttributes;
import com.tmall.aselfcaptain.item.constant.Channel;
import com.tmall.aselfcaptain.item.model.ItemDTO;
import com.tmall.aselfcaptain.item.model.ItemId;
import com.tmall.aselfcaptain.item.model.ItemQueryDO;
import com.tmall.aselfcaptain.item.model.QueryOptionDO;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkItemService;
import com.tmall.tmallwireless.tac.spi.context.SPIResult;
import com.tmall.wireless.store.spi.render.RenderSpi;
import com.tmall.wireless.store.spi.render.model.RenderRequest;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemConfig;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemConfigGroup;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemConfigGroups;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemConfigs;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.service.ItemPickService;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.biz.processor.huichang.inventory.InventoryEntranceModule.InventoryEntranceModuleHandler;
import com.tmall.wireless.tac.biz.processor.huichang.service.HallCommonContentRequestProxy;
import com.tmall.wireless.tac.biz.processor.huichang.service.HallCommonItemRequestProxy;
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
import java.text.DecimalFormat;
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

    private static final Integer tenThousand = 10000;
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
    @Autowired
    HallCommonItemRequestProxy hallCommonItemRequestProxy;

    @Override
    public Flowable<TacResult<List<GeneralItem>>> executeFlowable(RequestContext4Ald requestContext4Ald) throws Exception {
        try {

            BizScenario bizScenario = BizScenario.valueOf(HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
                    HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
                    "extreme_item");
            bizScenario.addProducePackage(HallScenarioConstant.HALL_ITEM_SDK_PACKAGE);
            Flowable<TacResult<List<GeneralItem>>> itemVOs = hallCommonItemRequestProxy.recommend(requestContext4Ald, bizScenario);

            logger.warn("=====itemVOs:" + JSON.toJSONString(itemVOs));
            tacLogger.warn("=====itemVOs:" + JSON.toJSONString(itemVOs));

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
        GeneralItem generalItem = new GeneralItem();
        List<Map<String, Object>> items = new ArrayList<>();
        for (ItemConfigGroup itemConfigGroup : itemConfigGroups.getItemConfigGroups()) {
            items.add(buildItemMap(longItemDTOMap.get(afterPickGroupMap.get(itemConfigGroup.getGroupNo()).getItemId())));
        }
        generalItem.put("items", items);
        result.add(generalItem);
        return result;
    }



    RenderRequest buildRenderRequest(List<Long> itemIds, String smAreaId, String queryTime, Long userId) {
        RenderRequest renderRequest = new RenderRequest();
        ItemQueryDO query = new ItemQueryDO();

        if (StringUtils.isNotBlank(smAreaId) && !"0".equals(smAreaId)) {
            query.setAreaId(Long.valueOf(smAreaId));
        } else {
            tacLogger.debug("smAreaId is null" + smAreaId);
            return null;
        }
        List<ItemId> itemIdList = itemIds.stream()
                .map(itemId -> ItemId.valueOf(itemId, ItemId.ItemType.B2C))
                .collect(Collectors.toList());
        query.setItemIds(itemIdList);

        query.setChannel(Channel.WAP);
        if(StringUtils.isNotEmpty(queryTime)){
            query.setQueryTime(queryTime);
        }

        if (userId != null && userId != 0) {
            query.setBuyerId(userId);
        }

        query.setSource("txcs-shoppingguide", "hall");

        QueryOptionDO option = new QueryOptionDO();
        option.setOpenMkt(true);
        option.setSceneCode("conference.promotion");
        option.setIncludeQuantity(true);
        option.setIncludeSales(true);
        option.setIncludeMaiFanCard(true);
        option.setUserPromotion(false);
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
        RenderRequest renderRequest = buildRenderRequest(itemIds, "330110",null, 1034513083L);
        SPIResult<List<ItemDTO>> itemDTOs = renderSpi.query(renderRequest);
        return itemDTOs.getData().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    }

    public Map<String, Object> buildItemMap(ItemDTO itemDTO) {
        Map<String, Object> itemMap = Maps.newHashMap();
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
        itemMap.put("_areaSellable", !itemDTO.isSoldout());
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

        return itemMap;
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
