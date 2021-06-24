package com.tmall.wireless.tac.biz.processor.cnxh.utils;

import com.tmall.aself.shoppingguide.client.loc.domain.AddressDTO;
import com.tmall.aself.shoppingguide.client.loc.util.AddressUtil;
import com.tmall.wireless.tac.biz.processor.cnxh.enums.O2OChannelEnum;

/**
 * @Author: luoJunChong
 * @Date: 2021/6/21 14:33
 */
public class O2OChannelUtil {
    public static String getO2OChannel(String csa) {
        String O2OChannel = "";
        AddressDTO addressDTO = AddressUtil.parseCSA(csa);
        boolean rt1HourStoreCover = addressDTO.isRt1HourStoreCover();
        boolean rtHalfDayStoreCover = addressDTO.isRtHalfDayStoreCover();
        boolean nextDayStoreCover = isRtHalfDayStoreCover(addressDTO.getRtNextDayStoreId());
        //默认优先级 一小时达 > 半日达 > 外仓
        if (rt1HourStoreCover) {
            return O2OChannelEnum.ONE_HOUR.getCode();
        } else if (rtHalfDayStoreCover) {
            return O2OChannelEnum.HALF_DAY.getCode();
        } else if (nextDayStoreCover) {
            return O2OChannelEnum.NEXT_DAY.getCode();
        }
        return O2OChannel;
    }

    public static boolean isRtHalfDayStoreCover(Long rtNextDayStoreId) {
        return rtNextDayStoreId != null && rtNextDayStoreId > 0;
    }
}
