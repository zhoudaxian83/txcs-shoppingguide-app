package com.tmall.wireless.tac.biz.processor.wzt.utils;

import com.tmall.aself.shoppingguide.client.loc.domain.AddressDTO;
import com.tmall.aself.shoppingguide.client.loc.util.AddressUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.wireless.tac.biz.processor.wzt.enums.LogicalArea;

/**
 * @author luojunchong
 */
public class SmAreaIdUtil {

    public static Long getSmAreaId(SgFrameworkContextItem sgFrameworkContextItem) {
        //csa默认只为了区分大区，如有其它作用请检查
        String csa = MapUtil.getStringWithDefault(sgFrameworkContextItem.getRequestParams(), "csa",
            "13278278282_0_38.066124.114.465406_0_0_0_130105_107_0_0_0_130105007_0");
        return getSmAreaId(getAddressDTO(csa).getRegionCode());
    }

    public static AddressDTO getAddressDTO(String csa) {
        return AddressUtil.parseCSA(csa);
    }

    /**
     * 根据regionCode获取coreCityCode,华东打底
     *
     * @param regionCode
     * @return
     */
    private static Long getSmAreaId(String regionCode) {
        LogicalArea logicalArea = LogicalArea.ofCode(regionCode);
        if (logicalArea == null) {
            return LogicalArea.HD.getCoreCityCode();
        } else {
            return logicalArea.getCoreCityCode();
        }
    }

}
