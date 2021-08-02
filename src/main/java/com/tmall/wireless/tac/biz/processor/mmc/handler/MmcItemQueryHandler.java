package com.tmall.wireless.tac.biz.processor.mmc.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.alibaba.aladdin.lamp.domain.request.Request;
import com.alibaba.aladdin.lamp.domain.request.RequestItem;
import com.alibaba.aladdin.lamp.domain.request.modules.LocationInfo;
import com.alibaba.aladdin.lamp.domain.response.ResResponse;
import com.alibaba.aladdin.lamp.domain.user.UserProfile;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.hyperlocalretail.sdk.member.o2otbmc.domain.O2OItemBenfitsRequest;
import com.alibaba.hyperlocalretail.sdk.member.o2otbmc.domain.O2OItemBenfitsResponse;
import com.alibaba.tcls.scrm.sdk.utils.domain.common.Result;

import com.google.common.collect.Lists;
import com.taobao.freshx.homepage.client.domain.ItemDO;
import com.taobao.freshx.homepage.client.domain.ItemRecallModeDO;
import com.taobao.freshx.homepage.client.domain.ItemType;
import com.taobao.freshx.homepage.client.domain.RecallType;
import com.taobao.poi2.client.enumtype.ServiceRangeDeliveryTimeType;
import com.taobao.poi2.client.result.StoreResult;
import com.tmall.aself.shoppingguide.client.loc.util.AddressUtil;
import com.tmall.aselfcaptain.util.StackTraceUtil;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.spi.recommend.AldSpi;
import com.tmall.txcs.gs.spi.recommend.MmcMemberService;
import com.tmall.wireless.tac.biz.processor.newproduct.constant.Constant;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.handler.TacHandler;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 买买菜商品查询
 *
 * @author haixiao.zhang
 * @date 2021/7/9
 */
@Service
public class MmcItemQueryHandler implements TacHandler<ItemRecallModeDO> {

    Logger LOGGER = LoggerFactory.getLogger(MmcItemQueryHandler.class);

    public static final String HALS_DAY_PREFIX = "SG_TMCS_HALF_DAY_DS:";

    public static final String MMC_HOT_ITEM_ALD_RES_ID = "18105096";

    @Autowired
    private AldSpi aldSpi;

    @Autowired
    private MmcMemberService mmcMemberService;

    @Override
    public TacResult<ItemRecallModeDO> execute(Context context) throws Exception {
        try {
            HadesLogUtil.stream("MmcItemQueryHandler inner|context")
                .kv("context", JSON.toJSONString(context))
                .info();
            Long totalStart = System.currentTimeMillis();
            ItemRecallModeDO itemRecallModeDO = new ItemRecallModeDO();
            List<ItemDO> returnItemIdList = new ArrayList<>();
            Map<String, Object> extendDataMap = new HashMap<>();//扩展参数，权益信息会放到这个里面

            Long userId = MapUtil.getLongWithDefault(context.getParams(), "userId", 0L);
            List<StoreResult> storeList = new ArrayList<>();
            Object stores = context.getParams().get("stores");
            if (stores != null && stores instanceof List) {
                storeList = (List<StoreResult>)stores;
            } else {
                HadesLogUtil.stream("MmcItemQueryHandler inner|stores is empty")
                    .kv("stores", JSON.toJSONString(stores))
                    .info();
            }

            //获取阿拉丁的爆款专区数据
            List<ItemDO> aldData = getAldData(userId, storeList);
            HadesLogUtil.stream("MmcItemQueryHandler inner|aldDataSize")
                .kv("aldDataSize", String.valueOf(aldData.size()))
                .info();
            returnItemIdList.addAll(aldData);

            //如果userId为空，则不取新人三选一数据和券数据
            if (userId != null && userId != 0L) {
                //获取新人数据
                List<ItemDO> memberData = getMemberData(userId, storeList, extendDataMap);
                HadesLogUtil.stream("MmcItemQueryHandler inner|memberDataSize")
                    .kv("memberDataSize", String.valueOf(memberData.size()))
                    .info();
                returnItemIdList.addAll(memberData);
            }
            //删除重复商品
            removeDuplicateItems(returnItemIdList);

            itemRecallModeDO.setItems(returnItemIdList);
            itemRecallModeDO.setExtendData(extendDataMap);
            itemRecallModeDO.setType(RecallType.ASSIGN_ITEM_ID);
            Long totalEnd = System.currentTimeMillis();
            HadesLogUtil.stream("MmcItemQueryHandler inner|totalCost" + (totalEnd - totalStart))
                .kv("totalCost", String.valueOf(totalEnd - totalStart))
                .info();
            HadesLogUtil.stream("MmcItemQueryHandler inner|main process|success")
                .kv("totalCost", String.valueOf(totalEnd - totalStart))
                .info();
            return TacResult.newResult(itemRecallModeDO);
        } catch (Exception e) {
            HadesLogUtil.stream("MmcItemQueryHandler inner|main process|error")
                .kv("context", JSON.toJSONString(context))
                .kv("errorMsg", StackTraceUtil.stackTrace(e))
                .error();
            throw e;
        }

    }

    private List<ItemDO> getMemberData(Long userId, List<StoreResult> storeList, Map<String, Object> extendDataMap) {
        List<ItemDO> returnItemIdList = new ArrayList<>();
        try {
            List<String> storeIdList = storeList.stream().map(StoreResult::getStoreId).collect(Collectors.toList());
            O2OItemBenfitsRequest o2OItemBenfitsRequest = new O2OItemBenfitsRequest();
            o2OItemBenfitsRequest.setUserId(userId);
            o2OItemBenfitsRequest.setStoreId(Long.valueOf(storeIdList.get(0)));
            Long memberStart = System.currentTimeMillis();
            Result<O2OItemBenfitsResponse> o2OItemBenfitsResponseResult = mmcMemberService.queryItemAndBenefits(
                o2OItemBenfitsRequest);
            Long memberEnd = System.currentTimeMillis();
            if (o2OItemBenfitsResponseResult.isSuccess() && o2OItemBenfitsResponseResult.getData() != null) {
                O2OItemBenfitsResponse o2OItemBenfitsResponse = o2OItemBenfitsResponseResult.getData();
                List<Long> chooseItemIds = o2OItemBenfitsResponse.getChooseItemIds();
                if (CollectionUtils.isNotEmpty(chooseItemIds)) {
                    List<ItemDO> newItemList = chooseItemIds.stream().map(e -> {
                        ItemDO itemDO = new ItemDO();
                        itemDO.setItemId(e);
                        itemDO.setType(ItemType.NEW_USER_ITEM);
                        return itemDO;
                    }).collect(Collectors.toList());
                    //新人商品数据
                    returnItemIdList.addAll(newItemList);
                } else {
                    HadesLogUtil.stream("MmcItemQueryHandler inner|get member data|empty")
                        .kv("o2OItemBenfitsRequest", JSON.toJSONString(o2OItemBenfitsRequest))
                        .kv("o2OItemBenfitsResponseResult", JSON.toJSONString(o2OItemBenfitsResponseResult))
                        .error();
                }
                //红包数据
                extendDataMap.putAll(o2OItemBenfitsResponse.getExt());
            }
            HadesLogUtil.stream("MmcItemQueryHandler inner|memberCost|" + (memberEnd - memberStart))
                .info();
            return returnItemIdList;
        } catch (Exception e) {
            HadesLogUtil.stream("MmcItemQueryHandler inner|get member data|error")
                .kv("userId", String.valueOf(userId))
                .kv("storeList", JSON.toJSONString(storeList))
                .kv("errorMsg", StackTraceUtil.stackTrace(e))
                .error();
            return returnItemIdList;
        }

    }

    private List<ItemDO> getAldData(Long userId, List<StoreResult> storeList) {
        List<ItemDO> returnItemIdList = new ArrayList<>();
        Long aldStart = System.currentTimeMillis();
        try {
            Request request = buildAldRequest(userId, storeList);
            Map<String, ResResponse> aldResponseMap = aldSpi.queryAldInfoSync(request);
            Long aldEnd = System.currentTimeMillis();
            if (MapUtils.isNotEmpty(aldResponseMap)) {
                ResResponse resResponse = aldResponseMap.get(MMC_HOT_ITEM_ALD_RES_ID);
                if (resResponse != null) {
                    List<Map<String, Object>> dataList = (List<Map<String, Object>>)aldResponseMap.get(
                        MMC_HOT_ITEM_ALD_RES_ID)
                        .get("data");
                    if (CollectionUtils.isNotEmpty(dataList)) {
                        List<ItemDO> oldItemIdList = dataList.stream().map(e -> {
                            Long contentId = Long.valueOf(String.valueOf(e.get("contentId")));
                            ItemDO oldItemDO = new ItemDO();
                            oldItemDO.setItemId(contentId);
                            oldItemDO.setType(ItemType.NORMAL_ITEM);
                            return oldItemDO;
                        }).collect(Collectors.toList());
                        returnItemIdList.addAll(oldItemIdList);
                    } else {
                        HadesLogUtil.stream("MmcItemQueryHandler inner|get ald data|empty")
                            .kv("request", JSON.toJSONString(request))
                            .kv("aldResponseMap", JSON.toJSONString(aldResponseMap))
                            .info();
                    }
                }
            }
            HadesLogUtil.stream("MmcItemQueryHandler inner|aldCost|" + (aldEnd - aldStart))
                .info();
            return returnItemIdList;
        } catch (Exception e) {
            HadesLogUtil.stream("MmcItemQueryHandler inner|get ald data|error")
                .kv("userId", String.valueOf(userId))
                .kv("storeList", JSON.toJSONString(storeList))
                .kv("errorMsg", StackTraceUtil.stackTrace(e))
                .error();
            return returnItemIdList;
        }
    }

    private Request buildAldRequest(Long userId, List<StoreResult> storeIdList) {
        Request request = new Request();
        request.setBizId(Constant.ALD_BIZ_ID);
        request.setCallSource(Constant.ALD_CALL_SOURCE);
        request.setDebug(false);
        RequestItem requestItem = new RequestItem();
        requestItem.setResId(MMC_HOT_ITEM_ALD_RES_ID);
        JSONObject data = new JSONObject();
        //渠道参数，流程模板需要识别，识别到以后流程模板内部只返回静态数据，也就是只拿到商品id，不走渲染逻辑
        data.put("mmcProjectChannel", "mmc-halfday");
        requestItem.setData(data);
        UserProfile userProfile = request.getUserProfile();
        userProfile.setUserId(userId);
        request.setRequestItems(Lists.newArrayList(requestItem));
        //地址信息
        LocationInfo locationInfo = request.getLocationInfo();

        if(CollectionUtils.isNotEmpty(storeIdList)){
            List<String> collect = storeIdList.stream()
                .map(e -> HALS_DAY_PREFIX + e.getStoreId())
                .collect(Collectors.toList());
            locationInfo.setWdkCodes(collect);
        }

        return request;

    }

    /**
     * 删除重复商品，改变原始对象
     *
     * @param itemDOList
     * @return
     */
    public static void removeDuplicateItems(List<ItemDO> itemDOList) {
        Set<Long> itemIdSet = new HashSet<>();
        // 删除重复的元素
        Iterator<ItemDO> iterator = itemDOList.iterator();
        while (iterator.hasNext()) {
            ItemDO itemDO = iterator.next();
            if (itemIdSet.contains(itemDO.getItemId())) {
                iterator.remove();
            } else {
                itemIdSet.add(itemDO.getItemId());
            }
        }

    }

    private static String aldUrl = "https://ald-lamp.tmall.com/recommend?resIds=%s&_d=true&rp_%s=userId"
        + ":%s;csa:%s&locationInfo"
        + ".wdkCodes=%s";

    public static void main(String[] args) {
        //List<ItemDO> oldItemIdList = new ArrayList<>();
        //ItemDO itemDO1 = new ItemDO();
        //itemDO1.setItemId(11L);
        //oldItemIdList.add(itemDO1);
        //ItemDO itemDO2 = new ItemDO();
        //itemDO2.setItemId(22L);
        //oldItemIdList.add(itemDO2);
        //ItemDO itemDO3 = new ItemDO();
        //itemDO3.setItemId(11L);
        //oldItemIdList.add(itemDO3);
        //System.out.println("原始:" + JSON.toJSONString(oldItemIdList));
        //removeDuplicateItems(oldItemIdList);
        //System.out.println("去重后："+JSON.toJSONString(oldItemIdList));
        Long time = 0L;


        System.out.println(String.format(aldUrl,"1111","1111",1832025789,"3333","SG_TMCS_HALF_DAY_DS:3333"));

        System.out.println(JSON.toJSONString(AddressUtil.parseCSA("8739373185_0_30.278433.120.028764_0_0_0_330110_107_0_0_236635411_330110005_0")));
    }



}
