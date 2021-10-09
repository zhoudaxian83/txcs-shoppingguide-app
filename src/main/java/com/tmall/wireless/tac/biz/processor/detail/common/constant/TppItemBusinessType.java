package com.tmall.wireless.tac.biz.processor.detail.common.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: guichen
 * @Data: 2020/7/20
 * @Description:
 */
public enum TppItemBusinessType {
    B2C,OneHour,HalfDay,NextDay;
    private static final long serialVersionUID = 1L;

    private static Map<String,TppItemBusinessType> bizTypeMap;
    static {
        bizTypeMap=new HashMap<String,TppItemBusinessType>(4);
        bizTypeMap.put("B2C",B2C);
        bizTypeMap.put("O2OOneHour",OneHour);
        bizTypeMap.put("O2OHalfDay",HalfDay);
        bizTypeMap.put("O2ONextDay",NextDay);
    }

    public static TppItemBusinessType from(String name){
        try {
            if(bizTypeMap.containsKey(name)){
                return bizTypeMap.get(name);
            }
        }catch (Exception ignore){

        }
        return B2C;
    }
}
