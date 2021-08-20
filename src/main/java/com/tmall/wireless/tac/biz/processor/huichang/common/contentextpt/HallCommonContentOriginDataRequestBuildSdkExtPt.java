package com.tmall.wireless.tac.biz.processor.huichang.common.contentextpt;

import java.util.Map;

import com.google.common.collect.Maps;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata
import com.tmall.txcs.gs.framework.model.SgFrameworkContextContent;

/**
 *
 * 请求tpp数据的入参
 * @author wangguohui
 */
public class HallCommonContentOriginDataRequestBuildSdkExtPt implements ContentOriginDataRequestBuildSdkExtPt {

    @Override
    public RecommendRequest process(SgFrameworkContextContent sgFrameworkContextContent) {
        RecommendRequest recommendRequest = new RecommendRequest();
        Map<String, String> paramsMap = Maps.newHashMap();

        //sgFrameworkContextContent.get
        //paramsMap.put("resourceId", )
        //recommendRequest.setParams();
        return null;
    }
}
