package com.tmall.wireless.tac.biz.processor.todaycrazy.utils;

import com.tmall.wireless.tac.biz.processor.todaycrazy.LimitTairkeyEnum;

/**
 * @author guijian
 * @date 2021/05/18
 */
public class TairUtil {
    public static String formatHotTairKey(){
        String tairKey = "";
        int num = (int) (Math.random() * 5 + 1);
        switch (num){
            case 1:
                tairKey = LimitTairkeyEnum.FLASH_SALE_HB.getKey();
                break;
            case 3:
                tairKey = LimitTairkeyEnum.FLASH_SALE_HN.getKey();
                break;
            case 4:
                tairKey = LimitTairkeyEnum.FLASH_SALE_XB.getKey();
                break;
            case 5:
                tairKey = LimitTairkeyEnum.FLASH_SALE_XN.getKey();
                break;
            default:
                tairKey = LimitTairkeyEnum.FLASH_SALE_HD.getKey();
                break;
        }
        return tairKey+"_pre";
        //return tairKey;
    }
}
