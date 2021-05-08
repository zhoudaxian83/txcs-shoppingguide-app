package com.tmall.wireless.tac.biz.processor.firstScreenMind.utils;

import org.springframework.stereotype.Component;

import java.util.Map;
@Component
public class RenderCheckUtil {

    public static boolean MapKeyValueNotEmpty(Map<String,String> map,String key){
        return map != null && map.containsKey(key) && map.get(key) != null;
    }

    public static boolean MapKeyValueEmpty(Map<String,String> map,String key){
        return map == null || !map.containsKey(key) || map.get(key) == null;
    }

    public static boolean StringEmpty(String s){
        return (s==null) || s.equals("");
    }

    public static boolean LongEmpty(Long l){
        return (l==null) || l==0L;
    }

    public static boolean LongNotEmpty(Long l){
        return (l!=null) && l>0L;
    }

    public static boolean StringNotEmpty(String s){
        return (s!=null) && !s.equals("");
    }

    public static boolean objectEmpty(Object o){
        return o == null;
    }

    public static boolean objectNotEmpty(Object o){
        return o != null;
    }

    /*public static boolean checkDebugWhite(RenderQuery renderQuery){
        String bizCode = renderQuery.getBizCode();
        Long userId = renderQuery.getUserInfo().getUserId();
        boolean userLogin = renderQuery.getUserInfo().userLogin();
        if(!userLogin){
            return false;
        }
        //逛超市栏目
        if(RenderBizCodeEnum.visitSupermarketColumn.getBusiness().equals(bizCode)
            || RenderBizCodeEnum.visitSupermarketColumnItemFeeds.getBusiness().equals(bizCode)){
            String vsRenderFlowDebugWhite = VisitSupermarketSwitch.vsRenderFlowDebugWhite;
            if(vsRenderFlowDebugWhite.contains(String.valueOf(userId))){
                return true;
            }
        }

        //分类改版栏目
        if(RenderBizCodeEnum.allClassificationContentFeeds.getBusiness().equals(bizCode)
            || RenderBizCodeEnum.allClassificationItemFeeds.getBusiness().equals(bizCode)){
            String acRenderFlowDebugWhite = AllClassificationSwitch.acRenderFlowDebugWhite;
            if(acRenderFlowDebugWhite.contains(String.valueOf(userId))){
                return true;
            }
        }
        return false;
    }*/
}
