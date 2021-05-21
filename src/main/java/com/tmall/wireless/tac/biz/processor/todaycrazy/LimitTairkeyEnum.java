package com.tmall.wireless.tac.biz.processor.todaycrazy;

import lombok.Getter;

/**
 * @author guijian
 * @date 2021/05/18
 * 为了防止热点key,源数据存在于5个key中
 */
public enum LimitTairkeyEnum {
    FLASH_SALE_HD("flashSale_HD"),
    FLASH_SALE_HN("flashSale_HN"),
    FLASH_SALE_HB("flashSale_HB"),
    FLASH_SALE_XN("flashSale_XN"),
    FLASH_SALE_XB("flashSale_HZ");
    /*FLASH_SALE_HD("flashSaleNew_HD"),
    FLASH_SALE_HN("flashSaleNew_HN"),
    FLASH_SALE_HB("flashSaleNew_HB"),
    FLASH_SALE_XN("flashSaleNew_XN"),
    FLASH_SALE_XB("flashSaleNew_HZ");*/
    LimitTairkeyEnum(String key) {
        this.key = key;
    }
    @Getter
    private String key;
}
