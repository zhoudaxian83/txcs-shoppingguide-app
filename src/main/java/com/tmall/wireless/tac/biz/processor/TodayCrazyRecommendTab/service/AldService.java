package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.service;

import com.alibaba.aladdin.lamp.domain.request.Request;
import com.alibaba.aladdin.lamp.domain.request.RequestItem;
import com.alibaba.aladdin.lamp.domain.response.ResResponse;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.tmall.txcs.gs.spi.recommend.AldSpi;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.constant.CommonConstant;
import com.tmall.wireless.tac.biz.processor.newproduct.constant.Constant;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class AldService {
    @Autowired
    private AldSpi aldSpi;

    @Autowired
    TacLoggerImpl tacLogger;

    public List<Map<String, Object>> getAldData() {
        Map<String, ResResponse> mapResponse = aldSpi.queryAldInfoSync(buildAldRequest());
        if (MapUtils.isNotEmpty(mapResponse)) {
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) mapResponse.get(Constant.ITEM_ALD_RES_ID).get("data");
            return dataList;
        }
        return null;
    }

    private Request buildAldRequest() {
        Request request = new Request();
        request.setBizId(Constant.ALD_BIZ_ID);
        request.setCallSource(Constant.ALD_CALL_SOURCE);
        request.setDebug(false);
        RequestItem item = new RequestItem();
        item.setResId(CommonConstant.ITEM_ALD_RES_ID);
        item.setBackup(true);
        request.setRequestItems(Lists.newArrayList(item));
//        UserProfile userProfile = request.getUserProfile();
//        userProfile.setUserId(context.getUserInfo().getUserId());

        //DeviceInfo deviceInfo = request.getDeviceInfo();
        //deviceInfo.setTtid(sgFrameworkContextContent.get().);
//        //地址信息
//        LocationInfo locationInfo = request.getLocationInfo();
//        //四级地址
//        Long smAreaId = MapUtil.getLongWithDefault(context.getParams(), "smAreaId", 330100L);
//        locationInfo.setCityLevel4(String.valueOf(smAreaId));
//        List<String> wdkCodes = Lists.newArrayList();
//        locationInfo.setWdkCodes(wdkCodes);
        tacLogger.info("阿拉丁排序入参" + JSON.toJSONString(request));
        return request;
    }
}
