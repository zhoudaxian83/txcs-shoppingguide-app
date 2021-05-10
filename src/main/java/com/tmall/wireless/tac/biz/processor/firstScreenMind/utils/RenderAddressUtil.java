package com.tmall.wireless.tac.biz.processor.firstScreenMind.utils;

import com.tmall.aself.shoppingguide.client.loc.domain.AddressDTO;
import com.tmall.aself.shoppingguide.client.loc.util.AddressUtil;

public class RenderAddressUtil {

    /**csa地址信息格式化*/
    public static AddressDTO getAddressDTO(String csa){
        if(RenderCheckUtil.StringEmpty(csa)){
            return null;
        }
        try{
            return AddressUtil.parseCSA(csa);
        }catch (Exception e){
            return null;
        }
    }

    /**当前地址是否被一小时达门店覆盖*/
    public static boolean rt1HourStoreCover(String csa){
        AddressDTO addressDTO = getAddressDTO(csa);
        if(addressDTO == null){
            return false;
        }
        return addressDTO.isRt1HourStoreCover();
    }

    /**当前地址是否被半日达门店覆盖*/
    public static boolean rtHalfDayStoreCover(String csa){
        AddressDTO addressDTO = getAddressDTO(csa);
        if(addressDTO == null){
            return false;
        }
        return addressDTO.isRtHalfDayStoreCover();
    }

    /**当前地址是否被外仓门店覆盖*/
    public static boolean nextDayStoreCover(String csa){
        Long rtNextDayStoreId = getRtNextDayStoreId(csa);
        if(RenderCheckUtil.objectEmpty(rtNextDayStoreId)){
            return false;
        }
        return rtNextDayStoreId > 0;
    }

    /**获取一小时达门店ID*/
    public static Long getRt1HourStoreId(String csa){
        AddressDTO addressDTO = getAddressDTO(csa);
        if(addressDTO == null){
            return null;
        }
        return addressDTO.getRt1HourStoreId();
    }

    /**获取半日达门店ID*/
    public static Long getRtHalfDayStoreId(String csa){
        AddressDTO addressDTO = getAddressDTO(csa);
        if(addressDTO == null){
            return null;
        }
        return addressDTO.getRtHalfDayStoreId();
    }

    /**获取外仓门店ID*/
    public static Long getRtNextDayStoreId(String csa){
        AddressDTO addressDTO = getAddressDTO(csa);
        if(addressDTO == null){
            return null;
        }
        return addressDTO.getRtNextDayStoreId();
    }
}
