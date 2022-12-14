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
 * ?????????????????????
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
            LOGGER.error("[MmcItemQueryHandler].execute.start|context:{}", JSON.toJSONString(context));
            Long totalStart = System.currentTimeMillis();
            ItemRecallModeDO itemRecallModeDO = new ItemRecallModeDO();
            List<ItemDO> returnItemIdList = new ArrayList<>();
            Map<String, Object> extendDataMap = new HashMap<>();//????????????????????????????????????????????????

            Long userId = MapUtil.getLongWithDefault(context.getParams(), "userId", 0L);
            List<StoreResult> storeList = new ArrayList<>();
            Object stores = context.getParams().get("stores");
            if (stores != null && stores instanceof List) {
                storeList = (List<StoreResult>)stores;
            } else {
                HadesLogUtil.stream("MmcItemQueryHandler inner|stores is empty")
                    .kv("stores", JSON.toJSONString(stores))
                    .error();
            }

            //????????????????????????????????????
            List<ItemDO> aldData = getAldData(userId, storeList);
            HadesLogUtil.stream("MmcItemQueryHandler inner|aldDataSize")
                .kv("aldDataSize", String.valueOf(aldData.size()))
                .error();
            returnItemIdList.addAll(aldData);

            //??????userId???????????????????????????????????????????????????
            if (userId != null && userId != 0L) {
                //??????????????????
                List<ItemDO> memberData = getMemberData(userId, storeList, extendDataMap);
                HadesLogUtil.stream("MmcItemQueryHandler inner|memberDataSize")
                    .kv("memberDataSize", String.valueOf(memberData.size()))
                    .kv("extendDataMap", JSON.toJSONString(extendDataMap))
                    .error();
                returnItemIdList.addAll(memberData);
            }
            //??????????????????
            removeDuplicateItems(returnItemIdList);

            itemRecallModeDO.setItems(returnItemIdList);
            itemRecallModeDO.setExtendData(extendDataMap);
            itemRecallModeDO.setType(RecallType.ASSIGN_ITEM_ID);
            Long totalEnd = System.currentTimeMillis();
            HadesLogUtil.stream("MmcItemQueryHandler inner|totalCost|" + (totalEnd - totalStart))
                .kv("totalCost", String.valueOf(totalEnd - totalStart))
                .error();
            HadesLogUtil.stream("MmcItemQueryHandler inner|main process|success")
                .kv("totalCost", String.valueOf(totalEnd - totalStart))
                .error();
            return TacResult.newResult(itemRecallModeDO);
        } catch (Exception e) {
            HadesLogUtil.stream("MmcItemQueryHandler inner|main process|error")
                .kv("context", JSON.toJSONString(context))
                .kv("errorMsg", StackTraceUtil.stackTrace(e))
                .error();
            LOGGER.error("[MmcItemQueryHandler].execute.error", JSON.toJSONString(context));
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
                    //??????????????????
                    returnItemIdList.addAll(newItemList);
                } else {
                    HadesLogUtil.stream("MmcItemQueryHandler inner|get member data|empty")
                        .kv("o2OItemBenfitsRequest", JSON.toJSONString(o2OItemBenfitsRequest))
                        .kv("o2OItemBenfitsResponseResult", JSON.toJSONString(o2OItemBenfitsResponseResult))
                        .error();
                }
                //????????????

                extendDataMap.putAll(o2OItemBenfitsResponse.getExt());
            }
            HadesLogUtil.stream("MmcItemQueryHandler inner|memberCost|" + (memberEnd - memberStart))
                .error();
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
                    Object data = resResponse.get("data");
                    HadesLogUtil.stream("MmcItemQueryHandler inner|resResponse.get(data)|")
                        .kv("request", JSON.toJSONString(request))
                        .kv("data", JSON.toJSONString(data))
                        .error();
                    List<Map<String, Object>> dataList = (List<Map<String, Object>>)data;
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
                            .error();
                    }
                }
            }
            HadesLogUtil.stream("MmcItemQueryHandler inner|aldCost|" + (aldEnd - aldStart))
                .error();
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
        //???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????id?????????????????????
        data.put("mmcProjectChannel", "mmc-halfday");
        requestItem.setData(data);
        UserProfile userProfile = request.getUserProfile();
        userProfile.setUserId(userId);
        request.setRequestItems(Lists.newArrayList(requestItem));
        //????????????
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
     * ???????????????????????????????????????
     *
     * @param itemDOList
     * @return
     */
    public static void removeDuplicateItems(List<ItemDO> itemDOList) {
        Set<Long> itemIdSet = new HashSet<>();
        // ?????????????????????
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
        //System.out.println("??????:" + JSON.toJSONString(oldItemIdList));
        //removeDuplicateItems(oldItemIdList);
        //System.out.println("????????????"+JSON.toJSONString(oldItemIdList));

        System.out.println(String.format(aldUrl, "1111", "1111", 1832025789, "3333", "SG_TMCS_HALF_DAY_DS:3333"));

    }


}
