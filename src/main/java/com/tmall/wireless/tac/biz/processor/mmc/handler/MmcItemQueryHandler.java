package com.tmall.wireless.tac.biz.processor.mmc.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.taobao.poi2.client.result.StoreResult;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.spi.recommend.AldSpi;
import com.tmall.txcs.gs.spi.recommend.MmcMemberService;
import com.tmall.wireless.tac.biz.processor.newproduct.constant.Constant;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.handler.TacReactiveHandler;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 买买菜商品查询
 *
 * @author haixiao.zhang
 * @date 2021/7/9
 */
@Service
public class MmcItemQueryHandler implements TacReactiveHandler<ItemRecallModeDO> {

    public static final String MMC_HOT_ITEM_ALD_RES_ID = "13757822";

    @Autowired
    TacLogger tacLogger;


    @Autowired
    private AldSpi aldSpi;

    @Autowired
    private MmcMemberService mmcMemberService;


    @Override
    public Flowable<TacResult<ItemRecallModeDO>> executeFlowable(Context context) throws Exception {
        tacLogger.info("------------------------------");
        Long start = System.currentTimeMillis();
        tacLogger.info("MmcItemQueryHandler.start. ----- context:{}" + JSON.toJSONString(context));
        ItemRecallModeDO itemRecallModeDO = new ItemRecallModeDO();
        List<ItemDO> returnItemIdList = new ArrayList<>();
        Map<String, Object> extendDataMap = new HashMap<>();//扩展参数，权益信息会放到这个里面
        if(MapUtils.isNotEmpty(itemRecallModeDO.getExtendData())){
            extendDataMap = itemRecallModeDO.getExtendData();
        }

        Long userId = MapUtil.getLongWithDefault(context.getParams(), "userId", 0L);
        List<StoreResult> storeList = new ArrayList<>();
        Object stores = context.getParams().get("stores");
        if (stores != null && stores instanceof List) {
            storeList = (List<StoreResult>)stores;
        } else {
            //TODO 异常处理
        }
        List<String> storeIdList = storeList.stream().map(StoreResult::getStoreId).collect(Collectors.toList());
        //String storeId = MapUtil.getStringWithDefault(context.getParams(), "storeId", "");
        //List<String> storeIdList = Arrays.asList(storeId);

        Request request = buildAldRequest(userId, storeIdList);
        Long aldStart = System.currentTimeMillis();
        Map<String, ResResponse> aldResponseMap = aldSpi.queryAldInfoSync(request);
        Long aldEnd = System.currentTimeMillis();
        tacLogger.info("-----------ald cost : " + (aldEnd - aldStart)+"-------------------");
        tacLogger.info("aldResponseMap:" + JSON.toJSONString(aldResponseMap));
        if (MapUtils.isNotEmpty(aldResponseMap)) {
            ResResponse resResponse = aldResponseMap.get(MMC_HOT_ITEM_ALD_RES_ID);
            if(resResponse != null){
                List<Map<String, Object>> dataList = (List<Map<String, Object>>)aldResponseMap.get(MMC_HOT_ITEM_ALD_RES_ID)
                    .get("data");
                if(CollectionUtils.isNotEmpty(dataList)){
                    List<ItemDO> oldItemIdList = dataList.stream().map(e -> {
                        Long contentId = Long.valueOf(String.valueOf(e.get("contentId")));
                        ItemDO oldItemDO = new ItemDO();
                        oldItemDO.setItemId(contentId);
                        oldItemDO.setType(ItemType.NORMAL_ITEM);
                        return oldItemDO;
                    }).collect(Collectors.toList());
                    returnItemIdList.addAll(oldItemIdList);
                }
            }

        }

        //如果userId为空，则不取新人三选一数据和券数据
        if (userId != null && userId != 0L) {
            O2OItemBenfitsRequest o2OItemBenfitsRequest = new O2OItemBenfitsRequest();
            o2OItemBenfitsRequest.setUserId(userId);
            o2OItemBenfitsRequest.setStoreId(Long.valueOf(storeIdList.get(0)));
            Long memberStart = System.currentTimeMillis();
            Result<O2OItemBenfitsResponse> o2OItemBenfitsResponseResult = mmcMemberService.queryItemAndBenefits(o2OItemBenfitsRequest);
            Long memberEnd = System.currentTimeMillis();
            tacLogger.info("-----------member cost : " + (memberEnd - memberStart)+"-------------------");
            if(o2OItemBenfitsResponseResult.isSuccess()){
                O2OItemBenfitsResponse o2OItemBenfitsResponse = o2OItemBenfitsResponseResult.getData();
                List<Long> chooseItemIds = o2OItemBenfitsResponse.getChooseItemIds();
                if(CollectionUtils.isNotEmpty(chooseItemIds)){
                    List<ItemDO> newItemList = chooseItemIds.stream().map(e -> {
                        ItemDO itemDO = new ItemDO();
                        itemDO.setItemId(e);
                        itemDO.setType(ItemType.NEW_USER_ITEM);
                        return itemDO;
                    }).collect(Collectors.toList());
                    //新人商品数据
                    returnItemIdList.addAll(newItemList);
                }
                //红包数据
                extendDataMap.putAll(o2OItemBenfitsResponse.getExt());
            }
        }

        itemRecallModeDO.setItems(returnItemIdList);
        itemRecallModeDO.setExtendData(extendDataMap);
        tacLogger.info("return itemRecallModeDO:" + JSON.toJSONString(itemRecallModeDO));
        Long end = System.currentTimeMillis();
        tacLogger.info("final cost:" + (end - start));
        return Flowable.just(TacResult.newResult(itemRecallModeDO));

    }

    private Request buildAldRequest(Long userId, List<String> storeIdList) {
        Request request = new Request();
        request.setBizId(Constant.ALD_BIZ_ID);
        request.setCallSource(Constant.ALD_CALL_SOURCE);
        request.setDebug(false);
        RequestItem requestItem = new RequestItem();
        requestItem.setResId(MMC_HOT_ITEM_ALD_RES_ID);
        JSONObject data = new JSONObject();
        //渠道参数，流程模板需要识别，识别到以后流程模板内部只返回静态数据，也就是只拿到商品id，不走渲染逻辑
        data.put("sourceChannel", "mmc-halfday");
        requestItem.setData(data);
        UserProfile userProfile = request.getUserProfile();
        userProfile.setUserId(userId);
        request.setRequestItems(Lists.newArrayList(requestItem));
        //地址信息
        LocationInfo locationInfo = request.getLocationInfo();
        locationInfo.setWdkCodes(storeIdList);
        return request;

    }

}
