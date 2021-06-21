package com.tmall.wireless.tac.biz.processor.cnxh.utils;

import com.tmall.wireless.tac.biz.processor.cnxh.enums.O2OChannelEnum;

/**
 * @Author: luoJunChong
 * @Date: 2021/6/21 14:33
 */
public class O2OChannelUtil {
    public static String getO2OChannel(String csa) {
        String O2OChannel = "";
        boolean rt1HourStoreCover = RenderAddressUtil.rt1HourStoreCover(csa);
        boolean rtHalfDayStoreCover = RenderAddressUtil.rtHalfDayStoreCover(csa);
        boolean nextDayStoreCover = RenderAddressUtil.nextDayStoreCover(csa);
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
}
