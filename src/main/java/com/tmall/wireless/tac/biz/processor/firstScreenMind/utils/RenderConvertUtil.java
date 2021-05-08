package com.tmall.wireless.tac.biz.processor.firstScreenMind.utils;

import com.taobao.util.CollectionUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RenderConvertUtil {

    /**把"1,2,3,4" 转换成 [1L,2L,3L,4L]*/
    public static List<Long> strToLongList(String str,String separator) {
        if(RenderCheckUtil.StringEmpty(str)){
            return new ArrayList<>();
        }
        List<Long> longList = new ArrayList<>();
        String[] strArr = str.split(separator);
        for(String singleStr : strArr){
            Long l = RenderLangUtil.safeLong(singleStr.trim());
            if(l == null){
                continue;
            }
            longList.add(l);
        }
        return longList;
    }

    /**把 "1,2,3,4" 转换成 ["1","2","3","4"] */
    public static List<String> strToStrList(String str,String separator) {
        if(RenderCheckUtil.StringEmpty(str)){
            return new ArrayList<>();
        }
        List<String> strList = new ArrayList<>();
        String[] strArr = str.split(separator);
        for(String singleStr : strArr){
            String s = RenderLangUtil.safeString(singleStr.trim());
            if(s == null){
                continue;
            }
            strList.add(s);
        }
        return strList;
    }

    /**把 [1L,2L,3L,4L] 转换成 "1,2,3,4"*/
    public static String longListToStr(List<Long> idLongList,String separator) {
        if(CollectionUtil.isEmpty(idLongList)){
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for(int i=1; i<=idLongList.size(); i++){
            Long idLong = idLongList.get(i-1);
            if(i < idLongList.size()){
                builder.append(idLong).append(separator);
            }else{
                builder.append(idLong);
            }
        }
        return builder.toString();
    }

    /**把 ["1","2","3","4"] 转换成 "1,2,3,4"*/
    public static String strListToStr(List<String> idStrList,String separator) {
        if(CollectionUtil.isEmpty(idStrList)){
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for(int i=1; i<=idStrList.size(); i++){
            String idStr = idStrList.get(i-1);
            if(i < idStrList.size()){
                builder.append(idStr).append(separator);
            }else{
                builder.append(idStr);
            }
        }
        return builder.toString();
    }

    /**把 "1,2,3,4" 转换成 {"1":value,"2":value,"3":value,"4":value,}*/
    public static Map<String, String> buildStringMapByStrList(String strListKey,String separator,String value){
        Map<String, String> map = new HashMap<>();
        List<String> stringList = strToStrList(strListKey,separator);
        if(CollectionUtil.isEmpty(stringList)){
            return map;
        }
        for(String key : stringList){
            map.put(key,value);
        }
        return map;
    }

}
