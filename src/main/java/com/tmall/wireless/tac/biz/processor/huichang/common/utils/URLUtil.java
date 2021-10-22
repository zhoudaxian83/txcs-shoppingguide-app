package com.tmall.wireless.tac.biz.processor.huichang.common.utils;

import com.tmall.aselfcaptain.util.StackTraceUtil;
import com.tmall.hades.monitor.print.HadesLogUtil;

import java.util.HashMap;
import java.util.Map;

public class URLUtil {

    public static String UrlPage(String strURL) {
        String strPage = null;
        String[] arrSplit = null;

        strURL = strURL.trim().toLowerCase();

        arrSplit = strURL.split("[?]");
        if (strURL.length() > 0) {
            if (arrSplit.length > 1) {
                if (arrSplit[0] != null) {
                    strPage = arrSplit[0];
                }
            }
        }

        return strPage;
    }

    /**
     * 去掉url中的路径，留下请求参数部分
     *
     * @param strURL url地址
     * @return url请求参数部分
     */
    private static String TruncateUrlPage(String strURL) {
        String strAllParam = null;
        String[] arrSplit = null;

        strURL = strURL.trim();

        arrSplit = strURL.split("[?]");
        if (strURL.length() > 1) {
            if (arrSplit.length > 1) {
                if (arrSplit[1] != null) {
                    strAllParam = arrSplit[1];
                }
            }
        }

        return strAllParam;
    }

    /*
     * 解析出url参数中的键值对
     * 如 "index.jsp?Action=del&id=123"，解析出Action:del,id:123存入map中
     * @param URL  url地址
     * @return  url请求参数部分
     */
    public static Map<String, String> URLRequest(String URL) {
        Map<String, String> mapRequest = new HashMap<String, String>();
        try {
            String[] arrSplit = null;

            String strUrlParam = TruncateUrlPage(URL);
            if (strUrlParam == null) {
                return mapRequest;
            }
            //每个键值为一组 www.2cto.com
            arrSplit = strUrlParam.split("[&]");
            for (String strSplit : arrSplit) {
                String[] arrSplitEqual = null;
                arrSplitEqual = strSplit.split("[=]");

                //解析出键值
                if (arrSplitEqual.length > 1) {
                    //正确解析
                    mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);
                } else {
                    if (arrSplitEqual.length > 0 && arrSplitEqual[0] != "") {
                        //只有参数没有值，不加入
                        mapRequest.put(arrSplitEqual[0], "");
                    }
                }
            }
        } catch (Exception e) {
            //ignore
            HadesLogUtil.stream("utils")
                    .kv("URLUtil", "URLRequest")
                    .kv("URLUtil URLRequest exception", StackTraceUtil.stackTrace(e))
                    .error();
        }
        return mapRequest;
    }

    /**
     * 将map转换成url
     *
     * @param map
     * @return
     */
    public static String getUrlParamsByMap(Map<String, String> map) {
        if (map == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append(entry.getKey() + "=" + entry.getValue());
            sb.append("&");
        }
        String s = sb.toString();
        if (s.endsWith("&")) {
            s = org.apache.commons.lang3.StringUtils.substringBeforeLast(s, "&");
        }
        return s;
    }

    public static void main(String[] args) {
        String url = "https://pages.tmall.com/wow/an/cs/act/wupr?=&cpp=1&e=t-fAas_xspjc1KrNaDlw_vLkhKzI86GJ_4wEhIavMd3Jo7iD9i9teEDUl1GR57hwfIXmwaFN1AZIJ2TB_xgC0i_IAR7ok-np8sviUM61dt1cEUzfAxawn2s4vKiCvs_sGnP4eoNSgau1WRAIRiVe9rSyDPcS65mxe4Zl2gBL06Nw2qH-L52L1T4Tpgte29oET7L9zSqcxudaQJhxUPUeEtKYMBXg69krrlYyo_QbwE_DG_1N5hlzNg&union_lens=recoveryid%3A1634693009_227_476851127&wh_pid=act%2Fchubao&suid=51C59248-D50B-40A6-A08B-1133EC6E190F&type=2&sp_tk=REVoNlh0bGxjbDk&tkFlag=0&sourceType=other&un=2d511d09c8a6a4b646e6040d6f65052e&short_name=h.ffi7zm7&shareurl=true&share_crt_v=1&tk_cps_param=16473971&tk_cps_ut=2&visa=13a09278fde22a2e&ali_trackid=2%3Amm_28972722_978550135_109597350287%3A1634696063_162_1984263085&spm=a2159r.13376465.0.0&bxsign=tcdWPKIWjrxc-t1gfe-m8Vt37zadSUD47ZIAMkUIERZqYbHrLu712neFj9nIKFFmaQcHv_sq69T9FeuaKwm70WiLZhFDzpn0imm8kt67lFnH0U&sourceType=other&suid=bf17b024-934a-4895-a842-722fc7226008&ut_sk=1.YQdwC5hrrrMDAP2Z3LQH140Z_21646297_1634696060387.Copy.chaoshi_act_page_tb&clickid=I220_172083905216347385612474759&eRedirect=1&ttid=201200@taobao_iphone_10.4.10";
        System.out.println("URLRequest(url) = " + URLRequest(url));
    }
}
