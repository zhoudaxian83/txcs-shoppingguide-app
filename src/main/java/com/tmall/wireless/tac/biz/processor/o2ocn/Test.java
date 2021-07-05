package com.tmall.wireless.tac.biz.processor.o2ocn;

import com.tmall.aself.shoppingguide.client.loc.util.AddressUtil;

/**
 * @Author: luoJunChong
 * @Date: 2021/6/5 13:48
 */
public class Test {
    public static void main(String[] args) {
        String csa ="13276721967_0_30.197102.121.278105_0_0_0_330282_107_0_233930371_236608458_330282006_0";
        Long a=Long.valueOf(
            AddressUtil.parseCSA(csa).getRegionCode());
        System.out.println(Long.valueOf(
            AddressUtil.parseCSA(csa).getRegionCode()));
    }
}
