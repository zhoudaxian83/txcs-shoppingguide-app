package com.tmall.wireless.tac.biz.processor.alipay.service.impl;

import com.alibaba.aladdin.lamp.domain.request.Request;
import com.alibaba.aladdin.lamp.domain.request.RequestItem;
import com.alibaba.aladdin.lamp.domain.request.modules.LocationInfo;
import com.alibaba.aladdin.lamp.domain.response.ResResponse;
import com.alibaba.aladdin.lamp.domain.user.UserProfile;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.recmixer.common.service.facade.model.MixerCollectRecRequest;
import com.alipay.recmixer.common.service.facade.model.MixerCollectRecResult;
import com.google.common.collect.Lists;
import com.taobao.poi2.client.result.StoreResult;
import com.tmall.aselfcaptain.common.manager.LocationManager;
import com.tmall.tmallwireless.tac.spi.context.SPIResult;
import com.tmall.txcs.gs.spi.recommend.AldSpi;
import com.tmall.wireless.store.spi.user.UserProvider;
import com.tmall.wireless.store.spi.user.base.UicDeliverAddressBO;
import com.tmall.wireless.tac.biz.processor.alipay.service.IAliPayService;
import com.tmall.wireless.tac.biz.processor.newproduct.constant.Constant;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.valueOf;

@Service("aliPayServiceImpl")
public class AliPayServiceImpl implements IAliPayService {

    public static final String ALD_RES_ID = "18639997";

    @Autowired
    UserProvider userProvider;

    @Autowired
    private AldSpi aldSpi;

    @Override
    public MixerCollectRecResult processFirstPage(MixerCollectRecRequest mixerCollectRecRequest) {
        SPIResult<Map<String, Long>> uicIdFromAlipayUid = userProvider.getUicIdFromAlipayUid(Lists.newArrayList("2088602128328730"));

        Long taobaoUserId = Optional.of(uicIdFromAlipayUid).map(SPIResult::getData).map(map -> map.get("2088602128328730")).orElse(0L);

        SPIResult<UicDeliverAddressBO> userDefaultAddressSyn = userProvider.getUserDefaultAddressSyn(357133924L);

        String devisionCode = Optional.of(userDefaultAddressSyn).map(SPIResult::getData).map(UicDeliverAddressBO::getDevisionCode).orElse("");

        getAldData(taobaoUserId, devisionCode);

        MixerCollectRecResult mixerCollectRecResult = new MixerCollectRecResult();
        mixerCollectRecResult.setErrorCode(JSON.toJSONString(userDefaultAddressSyn));
        return mixerCollectRecResult;
    }

    Object getAldData(Long userId, String smAreaId) {
        Request request = buildAldRequest(userId, smAreaId);
        Map<String, ResResponse> stringResResponseMap =
                aldSpi.queryAldInfoSync(request);
        return Optional.of(stringResResponseMap).map(m -> m.get(ALD_RES_ID)).map(ResResponse::getData);
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
