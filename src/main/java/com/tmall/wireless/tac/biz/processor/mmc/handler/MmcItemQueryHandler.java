package com.tmall.wireless.tac.biz.processor.mmc.handler;

import com.google.common.collect.Lists;
import com.taobao.freshx.homepage.client.domain.ItemRecallModeDO;
import com.taobao.poi2.client.result.StoreResult;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.spi.recommend.AldSpi;
import com.tmall.wireless.tac.biz.processor.newproduct.constant.Constant;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.UserInfo;
import com.tmall.wireless.tac.client.handler.TacReactiveHandler;
import io.reactivex.Flowable;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.alibaba.aladdin.lamp.domain.request.Request;
import com.alibaba.aladdin.lamp.domain.request.RequestItem;
import com.alibaba.aladdin.lamp.domain.request.modules.DeviceInfo;
import com.alibaba.aladdin.lamp.domain.request.modules.LocationInfo;
import com.alibaba.aladdin.lamp.domain.response.ResResponse;
import com.alibaba.aladdin.lamp.domain.user.UserProfile;

/**
 * 买买菜商品查询
 * @author haixiao.zhang
 * @date 2021/7/9
 */
public class MmcItemQueryHandler implements TacReactiveHandler<ItemRecallModeDO> {


    public static final String MMC_HOT_ITEM_ALD_RES_ID = "17385421";

    @Autowired
    private AldSpi aldSpi;

    @Override
    public Flowable<TacResult<ItemRecallModeDO>> executeFlowable(Context context) throws Exception {
        //如果userId为空，则不取新人三选一数据和券数据
        Long userId = MapUtil.getLongWithDefault(context.getParams(), "userId", 0L);
        Request request = buildAldRequest(context);
        Map<String, ResResponse> aldResponseMap = aldSpi.queryAldInfoSync(request);
        if(MapUtils.isNotEmpty(aldResponseMap)) {
            List<Map<String, Object>> dataList = (List<Map<String, Object>>)aldResponseMap.get(MMC_HOT_ITEM_ALD_RES_ID)
                .get("data");

        }
            if(userId != null && userId != 0L){

        }




        ItemRecallModeDO itemRecallModeDO = new ItemRecallModeDO();

        return Flowable.just(TacResult.newResult(itemRecallModeDO));
    }


    private Request buildAldRequest(Context context){
        Long userId = MapUtil.getLongWithDefault(context.getParams(), "userId", 0L);
        List<StoreResult> storeList = new ArrayList<>();
        Object stores = context.getParams().get("stores");
        if (stores != null && stores instanceof List) {
            storeList = (List<StoreResult>)stores;
        }else {
            //TODO 异常处理
        }
        List<String> storeIdList = storeList.stream().map(StoreResult::getStoreId).collect(Collectors.toList());
        Request request = new Request();
        request.setBizId(Constant.ALD_BIZ_ID);
        request.setCallSource(Constant.ALD_CALL_SOURCE);
        request.setDebug(false);
        RequestItem item = new RequestItem();
        item.setResId(Constant.CONTENT_ALD_RES_ID);
        UserProfile userProfile = request.getUserProfile();
        userProfile.setUserId(userId);
        request.setRequestItems(Lists.newArrayList(item));
        //地址信息
        LocationInfo locationInfo = request.getLocationInfo();
        locationInfo.setWdkCodes(storeIdList);
        return request;


    }

}
