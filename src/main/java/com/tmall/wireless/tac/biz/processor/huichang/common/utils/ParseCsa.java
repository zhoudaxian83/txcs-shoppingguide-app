package com.tmall.wireless.tac.biz.processor.huichang.common.utils;

import com.tmall.aself.shoppingguide.client.loc.domain.AddressDTO;
import com.tmall.aself.shoppingguide.client.loc.util.AddressUtil;
import com.tmall.tcls.gs.sdk.framework.model.context.LocParams;
import org.apache.commons.lang3.StringUtils;

public class ParseCsa {
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
