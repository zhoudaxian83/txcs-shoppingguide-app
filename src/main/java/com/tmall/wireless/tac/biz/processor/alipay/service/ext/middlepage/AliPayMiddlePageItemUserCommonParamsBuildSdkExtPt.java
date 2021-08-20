package com.tmall.wireless.tac.biz.processor.alipay.service.ext.middlepage;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.alipay.recmixer.common.service.facade.model.MixerCollectRecRequest;
import com.alipay.tradecsa.common.service.spi.request.MiddlePageClientRequestDTO;
import com.alipay.tradecsa.common.service.spi.request.MiddlePageSPIRequest;
import com.alipay.tradecsa.common.service.spi.request.MiddlePageUserDTO;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.contextbuild.ItemUserCommonParamsBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.CommonUserParams;
import com.tmall.tcls.gs.sdk.framework.model.context.LocParams;
import com.tmall.tcls.gs.sdk.framework.model.context.UserDO;
import com.tmall.wireless.store.spi.user.UserProvider;
import com.tmall.wireless.tac.biz.processor.alipay.AlipayMiddlePageHandler;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.AldService;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.TaobaoUserInfoDTO;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.TaobaoUserInfoRequest;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.UserInfoService;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.domain.Context;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@SdkExtension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C
        , scenario = ScenarioConstantApp.SCENARIO_ALI_PAY_MIDDLE_PAGE
)
public class AliPayMiddlePageItemUserCommonParamsBuildSdkExtPt extends Register implements ItemUserCommonParamsBuildSdkExtPt {

    public static final String CONTEXT_KEY = "aldItem";
    @Autowired
    UserProvider userProvider;

    @Autowired
    UserInfoService userInfoService;
    @Autowired
    AldService aldService;
    @Override
    public CommonUserParams process(Context context) {
        CommonUserParams commonUserParams = new CommonUserParams();
        Object param = Optional.of(context).map(Context::getParams).map(m -> m.get(AlipayMiddlePageHandler.PARAM_KEY)).orElse(null);
        MiddlePageSPIRequest middlePageSPIRequest = (MiddlePageSPIRequest) param;

        TaobaoUserInfoRequest taobaoUserInfoRequest = new TaobaoUserInfoRequest();

        String alipayUserUserId = Optional.of(middlePageSPIRequest).map(MiddlePageSPIRequest::getMiddlePageUserDTO).map(MiddlePageUserDTO::getAlipayUserId).orElse("");
        taobaoUserInfoRequest.setAlipayUserId(alipayUserUserId);

        String cityCode = Optional.of(middlePageSPIRequest).map(MiddlePageSPIRequest::getMiddlePageClientRequestDTO).map(MiddlePageClientRequestDTO::getCityCode).orElse("");

        taobaoUserInfoRequest.setAlipayCityCode(StringUtils.isNumeric(cityCode) ? Long.parseLong(cityCode) : 0L);
        TaobaoUserInfoDTO taobaoUserInfoDTO = userInfoService.query(taobaoUserInfoRequest);

        UserDO userDO = new UserDO();
        userDO.setUserId(taobaoUserInfoDTO.getUserId());
        commonUserParams.setUserDO(userDO);


        LocParams locParams = new LocParams();
        locParams.setSmAreaId(taobaoUserInfoDTO.getSmAreaId());
        locParams.setRegionCode(107L);
        commonUserParams.setLocParams(locParams);


        GeneralItem aldData = aldService.getAldData(taobaoUserInfoDTO.getUserId(), String.valueOf(taobaoUserInfoDTO.getSmAreaId()));
        context.put(CONTEXT_KEY, aldData);
        return commonUserParams;
    }
}
