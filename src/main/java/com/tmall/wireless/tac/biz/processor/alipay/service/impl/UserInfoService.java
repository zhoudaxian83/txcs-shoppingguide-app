package com.tmall.wireless.tac.biz.processor.alipay.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tmallwireless.tac.spi.context.SPIResult;
import com.tmall.wireless.store.spi.user.UserProvider;
import com.tmall.wireless.store.spi.user.base.UicDeliverAddressBO;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.FirstScreenMindContentFilterExtPt;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class UserInfoService {

    Logger LOGGER = LoggerFactory.getLogger(UserInfoService.class);

    @Autowired
    UserProvider userProvider;

    public TaobaoUserInfoDTO query(TaobaoUserInfoRequest taobaoUserInfoRequest) {
        TaobaoUserInfoDTO taobaoUserInfoDTO = new TaobaoUserInfoDTO();
        taobaoUserInfoDTO.setSmAreaId(taobaoUserInfoRequest.getAlipayCityCode());
        taobaoUserInfoDTO.setLogicAreaId("107");

        long start = System.currentTimeMillis();
        try {
            if (StringUtils.isEmpty(taobaoUserInfoRequest.getAlipayUserId())) {
                return taobaoUserInfoDTO;
            }

            SPIResult<Map<String, Long>> uicIdFromAlipayUid = userProvider.getUicIdFromAlipayUid(Lists.newArrayList(taobaoUserInfoRequest.getAlipayUserId()));

            Long taobaoUserId = Optional.of(uicIdFromAlipayUid).map(SPIResult::getData).map(map -> map.get(taobaoUserInfoRequest.getAlipayUserId())).orElse(0L);

            if (taobaoUserId == 0L) {
                return taobaoUserInfoDTO;
            }

            taobaoUserInfoDTO.setUserId(taobaoUserId);

            SPIResult<UicDeliverAddressBO> userDefaultAddressSyn = userProvider.getUserDefaultAddressSyn(taobaoUserId);
            String devisionCode = Optional.of(userDefaultAddressSyn).map(SPIResult::getData).map(UicDeliverAddressBO::getDevisionCode).orElse("");

            if (StringUtils.isEmpty(devisionCode) || !StringUtils.isNumeric(devisionCode)) {
                return taobaoUserInfoDTO;
            }

            taobaoUserInfoDTO.setSmAreaId(Long.valueOf(devisionCode));

            return taobaoUserInfoDTO;
        } catch (Exception e) {
            LOGGER.error("UserInfoService_error:{}", JSON.toJSONString(taobaoUserInfoDTO), e);
        } finally {
            HadesLogUtil.stream("UserInfoService").kv("timeCost", String.valueOf(System.currentTimeMillis()- start)).info();
        }
        return taobaoUserInfoDTO;

    }


}
