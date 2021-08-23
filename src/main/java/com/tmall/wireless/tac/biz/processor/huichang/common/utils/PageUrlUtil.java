package com.tmall.wireless.tac.biz.processor.huichang.common.utils;

import com.alibaba.aladdin.lamp.domain.request.RequestItem;
import com.alibaba.aladdin.lamp.sdk.solution.context.SolutionContext;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.table.TableCellEditor;
import java.net.URLDecoder;
import java.util.Map;


/**
 * Created by likunlin on 2021-08-20.
 */
public class PageUrlUtil {

    public static String getParamFromCurPageUrl(Map<String, Object> aldParams, String param, TacLogger tacLogger) {

        String curPageUrl = getCurPageUrl(aldParams, tacLogger);
        if(StringUtils.isEmpty(curPageUrl)){
            return null;
        }

        try {
            curPageUrl = URLDecoder.decode(URLDecoder.decode(curPageUrl, "UTF-8"),"UTF-8");
        } catch (Exception e) {
            tacLogger.debug("curPageUrl decode exception" + e.getMessage());
        }

        tacLogger.debug(curPageUrl);
        Map<String, String> paramsMap = URLUtil.URLRequest(curPageUrl);
        if(MapUtils.isNotEmpty(paramsMap) && StringUtils.isNotEmpty(paramsMap.get(param))){
            return paramsMap.get(param);
        }

        return null;
    }

    public static String getCurPageUrl(Map<String, Object> aldParams, TacLogger tacLogger) {
        Object currentPageUrl = aldParams.get("curPageUrl");
        if(currentPageUrl == null){
            tacLogger.debug( "curPageUrl is empty");
            return null;
        }
        return String.valueOf(currentPageUrl);
    }

    public static String addParams(String pageUrl, String paramKey, String paramValue) {
        if (pageUrl == null) {
            return null;
        }

        return pageUrl + "&" + paramKey + "=" + paramValue;
    }

    public static void main(String[] args)throws Exception{
        String url="https://pre-wormhole.wapa.tmall.com/wow/an/cs/act/wupr?wh_pid=act/173992bc4d1&topicIds=73941,77135,68753,65676&activityCode=57_223585&csa=7409543869_0_30.28743.120.038878_185203378_230146055_0_330110_107_110_0_236635411_330110005_318055001&disableNav=YES";
        String urldecode=URLDecoder.decode(url,"UTF-8");
        System.out.println(urldecode);
        System.out.println(URLDecoder.decode(urldecode,"UTF-8"));
    }
}
