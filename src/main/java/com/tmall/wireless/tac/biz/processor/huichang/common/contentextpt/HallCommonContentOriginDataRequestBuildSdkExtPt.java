package com.tmall.wireless.tac.biz.processor.huichang.common.contentextpt;

import java.util.Map;

import com.google.common.collect.Maps;

import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextContent;
import com.tmall.txcs.gs.model.constant.AldConstant;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;

/**
 *
 * 请求tpp数据的入参
 * @author wangguohui
 */
public class HallCommonContentOriginDataRequestBuildSdkExtPt implements ContentOriginDataRequestBuildSdkExtPt {

    @Override
    public RecommendRequest process(SgFrameworkContextContent sgFrameworkContextContent) {
        Context tacContext = sgFrameworkContextContent.getTacContext();
        RequestContext4Ald requestContext4Ald = (RequestContext4Ald)tacContext;
        Map<String, Object> aldParam = requestContext4Ald.getAldParam();//对应requestItem
        Map<String, Object> aldContext = requestContext4Ald.getAldContext();//对应solutionContext

        return null;
    }


}
