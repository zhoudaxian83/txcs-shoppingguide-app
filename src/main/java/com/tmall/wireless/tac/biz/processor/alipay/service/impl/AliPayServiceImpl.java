package com.tmall.wireless.tac.biz.processor.alipay.service.impl;

import com.alipay.recmixer.common.service.facade.model.MixerCollectRecRequest;
import com.alipay.recmixer.common.service.facade.model.MixerCollectRecResult;
import com.google.common.collect.Lists;
import com.tmall.tmallwireless.tac.spi.context.SPIResult;
import com.tmall.wireless.store.spi.user.UserProvider;
import com.tmall.wireless.store.spi.user.base.UicDeliverAddressBO;
import com.tmall.wireless.tac.biz.processor.alipay.service.IAliPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service("aliPayServiceImpl")
public class AliPayServiceImpl implements IAliPayService {

    @Autowired
    UserProvider userProvider;


    @Override
    public MixerCollectRecResult processFirstPage(MixerCollectRecRequest mixerCollectRecRequest) {
        SPIResult<Map<String, Long>> uicIdFromAlipayUid = userProvider.getUicIdFromAlipayUid(Lists.newArrayList("2088602128328730"));

        Long taobaoUserId = Optional.of(uicIdFromAlipayUid).map(SPIResult::getData).map(map -> map.get("2088602128328730")).orElse(0L);

        SPIResult<UicDeliverAddressBO> userDefaultAddressSyn = userProvider.getUserDefaultAddressSyn(taobaoUserId);

        String userId = Optional.of(userDefaultAddressSyn).map(SPIResult::getData).map(UicDeliverAddressBO::getArea).orElse("");
        MixerCollectRecResult mixerCollectRecResult = new MixerCollectRecResult();
        mixerCollectRecResult.setErrorCode(taobaoUserId + "  " + userId);
        return mixerCollectRecResult;
    }
}
