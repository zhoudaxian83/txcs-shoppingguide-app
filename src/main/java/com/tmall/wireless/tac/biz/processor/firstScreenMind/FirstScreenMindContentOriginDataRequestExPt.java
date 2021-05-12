package com.tmall.wireless.tac.biz.processor.firstScreenMind;

import com.alibaba.cola.extension.Extension;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.extensions.origindata.request.ContentOriginDataRequestExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextContent;
import com.tmall.txcs.gs.model.biz.context.LocParams;
import com.tmall.txcs.gs.model.biz.context.PageInfoDO;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.txcs.gs.model.spi.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.common.RequestKeyConstantApp;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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
        Map<String, Object> requestParams = sgFrameworkContextContent.getRequestParams();
        if(requestParams == null || requestParams.isEmpty()){
            return null;
        }
        List<Long> contentSetIdList = getContentSetIdList(requestParams);
        if(CollectionUtils.isEmpty(contentSetIdList)){
            params.put("contentSetIdList",Joiner.on(",").join(contentSetIdList));
        }

        // 新版本的内容集id
        List<String> newContentSetIdList = contentSetIdList.stream().map(id -> "intelligentCombinationItems_" + id).collect(Collectors.toList());
        params.put("sceneSet", Joiner.on(",").join(newContentSetIdList));

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
        Object isFixPositionBanner = requestParams.get("isFixPositionBanner");
        Boolean isMind = false;
        if(isFixPositionBanner == null || "".equals(isFixPositionBanner)){
            isMind = true;
        } else if(isFixPositionBanner instanceof Boolean){
            isMind = (Boolean) isFixPositionBanner;
        }else if(isFixPositionBanner instanceof String && "true".equals(isFixPositionBanner)){
            isMind = true;
        }
        //首次isFixPositionBanner为空或true，标识查询心智场景
        if(isMind){
            //tppRequest.setAppId(25379L);
            tppRequest.setAppId(23198L);
        }else{
            tppRequest.setAppId(25409L);
        }
        tppRequest.setParams(params);
        tppRequest.setLogResult(true);
        tppRequest.setUserId(Optional.ofNullable(sgFrameworkContextContent).map(SgFrameworkContext::getUserDO).map(UserDO::getUserId).orElse(0L));
        tacLogger.info("****FirstScreenMindContentOriginDataRequestExPt tppRequest***:"+tppRequest.toString());
        return tppRequest;
    }

    private List<Long> getContentSetIdList(Map<String, Object>  requestParams) {


        List<Long> result = Lists.newArrayList();
        result.add(MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_RANKING, 0L));
        result.add(MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_RECIPE, 0L));
        result.add(MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_BRAND, 0L));
        result.add(MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_MIND, 0L));
        result.add(MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_O2O, 0L));
        result.add(MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_B2C, 0L));

        return result.stream().filter(contentSetId -> contentSetId > 0).collect(Collectors.toList());
    }


}
