package com.tmall.wireless.tac.biz.processor.alipay.service.impl;

import com.alibaba.aladdin.lamp.domain.request.Request;
import com.alibaba.aladdin.lamp.domain.request.RequestItem;
import com.alibaba.aladdin.lamp.domain.request.modules.LocationInfo;
import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.alibaba.aladdin.lamp.domain.response.ResResponse;
import com.alibaba.aladdin.lamp.domain.user.UserProfile;
import com.google.common.collect.Lists;
import com.tmall.txcs.gs.spi.recommend.AldSpi;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.PredicateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AldService {
    public static final String ALD_RES_ID = "18639997";
    @Autowired
    private AldSpi aldSpi;

    public GeneralItem getAldData(Long userId, String smAreaId) {
        Request request = buildAldRequest(userId, smAreaId);
        Map<String, ResResponse> stringResResponseMap =
                aldSpi.queryAldInfoSync(request);
        List<GeneralItem> generalItemList = (List<GeneralItem>) Optional.of(stringResResponseMap).map(m -> m.get(ALD_RES_ID)).map(ResResponse::getData).orElse(null);
        if (CollectionUtils.isNotEmpty(generalItemList)) {
            return generalItemList.get(0);
        } else {
            return null;
        }
    }

    private Request buildAldRequest(Long userId, String smAreaId) {

        Request re = new Request();
        re.setResIds(ALD_RES_ID);

        LocationInfo locationInfo = new LocationInfo();
        //四级地址
        locationInfo.setCityLevel4(smAreaId);

        re.setLocationInfo(locationInfo);
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(userId);
        re.setUserProfile(userProfile);
        RequestItem requestItem = new RequestItem();
        requestItem.setResId(ALD_RES_ID);
        re.setRequestItems(Lists.newArrayList(requestItem));
        return re;
    }
}
