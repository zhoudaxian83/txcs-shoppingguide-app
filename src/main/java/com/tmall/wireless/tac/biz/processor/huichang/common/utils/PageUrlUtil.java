package com.tmall.wireless.tac.biz.processor.huichang.common.utils;

import com.alibaba.aladdin.lamp.domain.request.RequestItem;
import com.alibaba.aladdin.lamp.sdk.solution.context.SolutionContext;

import com.taobao.csp.hotsensor.fastjson.JSON;
import com.tmall.aselfcaptain.util.StackTraceUtil;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.wireless.tac.biz.processor.huichang.inventory.InventoryEntranceModule.InventoryEntranceModuleContentInfoQuerySdkExtPt;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.table.TableCellEditor;
import java.net.URLDecoder;
import java.util.Map;


/**
 * Created by likunlin on 2021-08-20.
 */
public class PageUrlUtil {

    public static String getParamFromCurPageUrl(Map<String, Object> aldParams, String param) {

        String curPageUrl = getCurPageUrl(aldParams);
        if(StringUtils.isEmpty(curPageUrl)){
            return null;
        }

        try {
            curPageUrl = URLDecoder.decode(URLDecoder.decode(curPageUrl, "UTF-8"),"UTF-8");
        } catch (Exception e) {
            HadesLogUtil.stream("utils")
                    .kv("PageUrlUtil", "getParamFromCurPageUrl")
                    .kv("curPageUrl decode exception", StackTraceUtil.stackTrace(e))
                    .error();
        }

        Map<String, String> paramsMap = URLUtil.URLRequest(curPageUrl);
        if(MapUtils.isNotEmpty(paramsMap) && StringUtils.isNotEmpty(paramsMap.get(param))){
            return paramsMap.get(param);
        }

        return null;
    }

    public static String getCurPageUrl(Map<String, Object> aldParams) {
        Object currentPageUrl = aldParams.get("curPageUrl");
        if(currentPageUrl == null){
            HadesLogUtil.stream("utils")
                    .kv("PageUrlUtil", "getCurPageUrl")
                    .kv("getCurPageUrl exception", "curPageUrl is empty")
                    .error();
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

    /**
     * 删除url里面某个参数
     * @param url
     * @param name
     * @return
     */
    public static String removeParam(String url, String ...name){
        for (String s : name) {
            // 使用replaceAll正则替换,replace不支持正则
            url = url.replaceAll("&?"+s+"=[^&]*","");
        }
        return url;
    }

    public static void main(String[] args)throws Exception{
        String urlRequest = "//detail.tmall.com/item.htm?&id=634542893398&locType=GSH&scm=1007.38364.250179.0.FF-hyhsfZ_appId-28364_I-_Q-befaabab-272e-46b4-a8c9-257490a21a11_D-634542893398_T-ITEM_businessType-B2C_predCTR-0_predCVR-0.4911979094_predCTCVR-0_calibCTR-0_calibCVR-0_calibCTCVR-0_finalScore-0.4911979094-FF";
        String locType = removeParam(urlRequest, "locType");
        System.out.println(locType);
    }


}
