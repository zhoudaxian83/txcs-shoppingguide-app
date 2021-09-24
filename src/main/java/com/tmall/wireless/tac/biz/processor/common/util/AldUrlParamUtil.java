package com.tmall.wireless.tac.biz.processor.common.util;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.taobao.tair.json.Json;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import com.alibaba.fastjson.JSON;

/**
 * Created by yangqing.byq on 2021/7/1.
 */
public class AldUrlParamUtil {

    public static Map<String, Object> getAldUrlKv(RequestContext4Ald requestContext4Ald) {
        String url = Optional.ofNullable(requestContext4Ald).
                map(RequestContext4Ald::getAldParam).
                map(map -> map.get("url")).
                map(Object::toString).
                map(URLDecoder::decode).
                orElse("");

        if (StringUtils.isEmpty(url)) {
            return Maps.newHashMap();
        }

        String[] split = url.split("\\?");

        if (split.length < 2) {
            return Maps.newHashMap();
        }

        String s = split[1];
        Map<String, String> urlKv = Maps.newHashMap();
        /*Map<String, String> urlKv = Splitter.on("&").withKeyValueSeparator("=").split(s);*/
        if(StringUtils.isNotEmpty(s) && s.contains("&")){
            String[] tmpArry1 = s.split("&");
            for(String m : tmpArry1){
                if(StringUtils.isNotEmpty(m) && m.contains("=")){
                    String[] tmpArry2 = m.split("=");
                    MapUtils.putAll(urlKv, tmpArry2);
                }
            }
        }
        Map<String, Object> result = Maps.newHashMap();
        urlKv.keySet().forEach(k -> {
            result.put(k, urlKv.get(k));
        });
        HadesLogUtil.stream("AldUrlParamUtil")
            .kv("url",url)
            .kv("result", JSON.toJSONString(result))
            .info();
        return result;
    }

    public static String getAldUrlParam(RequestContext4Ald requestContext4Ald, String key, String defaultValue) {
        String url = Optional.ofNullable(requestContext4Ald).
                map(RequestContext4Ald::getAldParam).
                map(map -> map.get("url")).
                map(Object::toString).
                map(URLDecoder::decode).
                orElse("");

        if (StringUtils.isEmpty(url)) {
            return defaultValue;
        }

        String[] split = url.split(",");

        if (split.length < 2) {
            return defaultValue;
        }

        String s = split[1];

        Map<String, String> urlKv = Splitter.on("&").withKeyValueSeparator("=").split(s);

        return urlKv.get(key);
    }
}
