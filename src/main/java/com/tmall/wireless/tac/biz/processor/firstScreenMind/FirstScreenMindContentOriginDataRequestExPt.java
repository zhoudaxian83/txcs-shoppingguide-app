package com.tmall.wireless.tac.biz.processor.firstScreenMind;

import com.alibaba.cola.extension.Extension;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.txcs.gs.framework.extensions.origindata.request.ContentOriginDataRequestExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextContent;
import com.tmall.txcs.gs.model.biz.context.LocParams;
import com.tmall.txcs.gs.model.biz.context.PageInfoDO;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.txcs.gs.model.spi.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_CONTENT)
@Service
public class FirstScreenMindContentOriginDataRequestExPt implements ContentOriginDataRequestExtPt {

    @Autowired
    TacLogger tacLogger;

    @Override
    public RecommendRequest process(SgFrameworkContextContent sgFrameworkContextContent) {

        tacLogger.info("****FirstScreenMindContentOriginDataRequestExPt sgFrameworkContextContent***:"+sgFrameworkContextContent.toString());

        RecommendRequest tppRequest = new RecommendRequest();
        Map<String, String> params = Maps.newHashMap();
        Map requestParams = sgFrameworkContextContent.getRequestParams();
        if(requestParams == null || requestParams.isEmpty()){
            return null;
        }
        //获取心智场景和普通场景内容集id
        String contentSetIds = (String) requestParams.get("contentSetIds");
        if(StringUtils.isNotEmpty(contentSetIds)){
            params.put("contentSetIdList",contentSetIds);
        }
        params.put("pageSize", Optional.ofNullable(sgFrameworkContextContent).map(SgFrameworkContext::getUserPageInfo).map(PageInfoDO::getPageSize).orElse(20).toString());
        //逛超市TPP内容召回每个内容挂载的商品数量
        params.put("itemCountPerContent", "10");
        params.put("contentType", "7");
        params.put("contentSetSource", "intelligentCombinationItems");
        //未登陆用户唯一身份ID，确认是否必须
        //params.put("exposureDataUserId", "");
        params.put("smAreaId", Optional.ofNullable(sgFrameworkContextContent).map(SgFrameworkContext::getLocParams).map(LocParams::getSmAreaId).orElse(0L).toString());
        params.put("logicAreaId", Joiner.on(",").join(Optional.ofNullable(sgFrameworkContextContent).map(SgFrameworkContext::getLocParams).map(LocParams::getLogicIdByPriority).orElse(Lists.newArrayList())));
        Integer index = Optional.ofNullable(sgFrameworkContextContent).map(SgFrameworkContext::getUserPageInfo).map(PageInfoDO::getIndex).orElse(0);
        params.put("isFirstPage", index > 0 ? "false" : "true");
        Boolean isFixPositionBanner = (Boolean) requestParams.get("isFixPositionBanner");
        //首次isFixPositionBanner为空或true，标识查询心智场景
        if(isFixPositionBanner == null || isFixPositionBanner){
            tppRequest.setAppId(25379L);
        }else{
            tppRequest.setAppId(25409L);
        }
        tppRequest.setParams(params);
        tppRequest.setLogResult(true);
        tppRequest.setUserId(Optional.ofNullable(sgFrameworkContextContent).map(SgFrameworkContext::getUserDO).map(UserDO::getUserId).orElse(0L));
        tacLogger.info("****FirstScreenMindContentOriginDataRequestExPt tppRequest***:"+tppRequest.toString());
        return tppRequest;
    }
}
