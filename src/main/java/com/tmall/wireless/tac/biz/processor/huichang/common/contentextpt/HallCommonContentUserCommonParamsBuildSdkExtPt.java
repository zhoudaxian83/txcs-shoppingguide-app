package com.tmall.wireless.tac.biz.processor.huichang.common.contentextpt;

import java.util.Map;

import com.alibaba.cola.extension.Extension;

import com.tmall.aself.shoppingguide.client.loc.domain.AddressDTO;
import com.tmall.aself.shoppingguide.client.loc.util.AddressUtil;
import com.tmall.tcls.gs.sdk.biz.uti.MapUtil;
import com.tmall.tcls.gs.sdk.framework.extensions.content.context.ContentUserCommonParamsBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.constant.RequestKeyConstant;
import com.tmall.tcls.gs.sdk.framework.model.context.CommonUserParams;
import com.tmall.tcls.gs.sdk.framework.model.context.LocParams;
import com.tmall.tcls.gs.sdk.framework.model.context.UserDO;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import org.apache.commons.lang3.StringUtils;

@Extension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
    useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
    scenario = HallScenarioConstant.HALL_SCENARIO_SCENARIO)
public class HallCommonContentUserCommonParamsBuildSdkExtPt implements ContentUserCommonParamsBuildSdkExtPt {


    @Override
    public CommonUserParams process(Context context) {
        RequestContext4Ald requestContext4Ald = (RequestContext4Ald)context;
        Map<String, Object> aldParam = requestContext4Ald.getAldParam();//对应requestItem
        Map<String, Object> aldContext = requestContext4Ald.getAldContext();//对应solutionContext
        CommonUserParams commonUserParams = new CommonUserParams();
        UserDO userDO = new UserDO();
        Long userId = MapUtil.getLongWithDefault(aldContext, "userId", 0L);
        String userNick = MapUtil.getStringWithDefault(aldContext, "userNick", "");
        userDO.setUserId(userId);
        userDO.setNick(userNick);
        commonUserParams.setUserDO(userDO);

        Long smAreaId = MapUtil.getLongWithDefault(aldParam, RequestKeyConstant.SMAREAID, 330100L);
        commonUserParams.setLocParams(parseCsaObj(aldParam.get(RequestKeyConstant.USER_PARAMS_KEY_CSA), smAreaId));

        return commonUserParams;
    }

    public static LocParams parseCsaObj(Object csa, Long smAreaId) {
        if (csa != null && !StringUtils.isEmpty(csa.toString())) {
            return parseCsa(csa.toString(), smAreaId);
        } else {
            LocParams locParams = new LocParams();
            locParams.setSmAreaId(smAreaId);
            return locParams;
        }
    }



    public static LocParams parseCsa(String csa, Long smAreaId) {
        AddressDTO addressDTO = AddressUtil.parseCSA(csa);
        LocParams locParams;
        if (addressDTO == null) {
            locParams = new LocParams();
            locParams.setSmAreaId(smAreaId);
            return locParams;
        } else {
            locParams = new LocParams();
            locParams.setRt1HourStoreId(addressDTO.getRt1HourStoreId());
            locParams.setRtHalfDayStoreId(addressDTO.getRtHalfDayStoreId());
            locParams.setSmAreaId(smAreaId);
            locParams.setRegionCode(StringUtils.isNumeric(addressDTO.getRegionCode()) ? Long.parseLong(addressDTO.getRegionCode()) : 0L);
            locParams.setMajorCityCode(StringUtils.isNumeric(addressDTO.getMajorCityCode()) ? Long.parseLong(addressDTO.getMajorCityCode()) : 0L);
            return locParams;
        }
    }


}
