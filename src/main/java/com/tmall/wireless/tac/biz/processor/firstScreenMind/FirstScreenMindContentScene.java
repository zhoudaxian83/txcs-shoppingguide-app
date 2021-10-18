package com.tmall.wireless.tac.biz.processor.firstScreenMind;


import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.taobao.eagleeye.EagleEye;
import com.taobao.tair.json.Json;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.txcs.biz.supermarket.scene.UserParamsKeyConstant;
import com.tmall.txcs.biz.supermarket.scene.util.CsaUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.model.*;
import com.tmall.txcs.gs.framework.model.meta.*;
import com.tmall.txcs.gs.framework.service.impl.SgFrameworkServiceContent;
import com.tmall.txcs.gs.framework.support.LogUtil;
import com.tmall.txcs.gs.model.biz.context.PageInfoDO;
import com.tmall.txcs.gs.model.biz.context.SceneInfo;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.wireless.tac.biz.processor.common.RequestKeyConstantApp;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.common.util.TacResultBackupUtil;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.utils.ContentSetIdListUtil;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.utils.MindUtil;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.utils.PressureTestUtil;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.UserInfo;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.spec.OAEPParameterSpec;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class FirstScreenMindContentScene {
    Logger LOGGER = LoggerFactory.getLogger(FirstScreenMindContentScene.class);

    @Autowired
    SgFrameworkServiceContent sgFrameworkServiceContent;
    @Autowired
    TacLogger  tacLogger;

    public Flowable<TacResult<SgFrameworkResponse<ContentVO>>> recommend(Context context) {

        long startTime = System.currentTimeMillis();

        Long smAreaId = MapUtil.getLongWithDefault(context.getParams(), "smAreaId", 330100L);
        SgFrameworkContextContent sgFrameworkContextContent = new SgFrameworkContextContent();
        sgFrameworkContextContent.setRequestParams(context.getParams());


        sgFrameworkContextContent.setSceneInfo(getSceneInfo());
        sgFrameworkContextContent.setUserDO(getUserDO(context));
        sgFrameworkContextContent.setLocParams(CsaUtil.parseCsaObj(context.get(UserParamsKeyConstant.USER_PARAMS_KEY_CSA), smAreaId));

        sgFrameworkContextContent.setContentMetaInfo(getContentMetaInfo(context.getParams()));


        PageInfoDO pageInfoDO = new PageInfoDO();
        pageInfoDO.setIndex(Integer.parseInt(MapUtil.getStringWithDefault(context.getParams(), "index", "0")));
        pageInfoDO.setPageSize(Integer.valueOf(MapUtil.getStringWithDefault(context.getParams(), "pageSize", "20")));
        sgFrameworkContextContent.setUserPageInfo(pageInfoDO);

        HadesLogUtil.stream(ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_CONTENT)
                .kv("step", "requestLog")
                .kv("userId", Optional.of(sgFrameworkContextContent).map(SgFrameworkContext::getUserDO).map(UserDO::getUserId).map(Objects::toString).orElse("0"))
                .kv("sgFrameworkContextContent",JSON.toJSONString(sgFrameworkContextContent))
                .info();
        return sgFrameworkServiceContent.recommend(sgFrameworkContextContent)
                .map(response -> {
                    Map<String, Object> requestParams = sgFrameworkContextContent.getRequestParams();
                    if(requestParams == null || requestParams.isEmpty()){
                        return null;
                    }
                    Object isFixPositionBanner = requestParams.get("isFixPositionBanner");
                    Map<String,Object> propertyMap = Maps.newHashMap();

                    propertyMap.put("index",response.getIndex());
                    if((null == isFixPositionBanner
                            || ("".equals(isFixPositionBanner))
                            || StringUtils.equalsIgnoreCase("true",String.valueOf(isFixPositionBanner)))
                            && getMindContentSetId(requestParams) > 0L){
                        if (response.isHasMore()) {
                            propertyMap.put("isFixPositionBanner", true);
                        } else {
                            propertyMap.put("isFixPositionBanner", false);
                            propertyMap.put("index", 0);
                            response.setHasMore(true);
//                            response.setIndex(0);
                        }
                    } else{
                        propertyMap.put("isFixPositionBanner", false);
                    }
                    if (response.getExtInfos() == null) {
                        response.setExtInfos(Maps.newHashMap());
                    }
                    response.getExtInfos().put("propertyMap", propertyMap);
                    response.getExtInfos().put("traceId", EagleEye.getTraceId());
                    HadesLogUtil.stream(ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_CONTENT)
                            .kv("step", "requestLog")
                            .kv("userId", Optional.of(sgFrameworkContextContent).map(SgFrameworkContext::getUserDO).map(UserDO::getUserId).map(Objects::toString).orElse("0"))
                            .kv("rt", String.valueOf(System.currentTimeMillis() - startTime))
                            .info();
                    return response;
                }).map(TacResult::newResult)
                .map(tacResult -> {
                    BizScenario b = BizScenario.valueOf(
                        ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
                        ScenarioConstantApp.LOC_TYPE_B2C,
                        ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_CONTENT
                    );
                    tacResult = TacResultBackupUtil.tacResultBackupContent(tacResult,b);
                    return tacResult;
                }).onErrorReturn(r -> TacResult.errorResult(""));
    }

    private Long getMindContentSetId(Map<String, Object> requestParams) {
        return MapUtil.getLongWithDefault(requestParams,
                RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_MIND, 0L);
    }


    public SceneInfo getSceneInfo(){
        SceneInfo sceneInfo = new SceneInfo();
        sceneInfo.setBiz(ScenarioConstantApp.BIZ_TYPE_SUPERMARKET);
        sceneInfo.setSubBiz(ScenarioConstantApp.LOC_TYPE_B2C);
        sceneInfo.setScene(ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_CONTENT);
        return sceneInfo;
    }
    public UserDO getUserDO(Context context){
        UserDO userDO = new UserDO();
        userDO.setUserId(Optional.of(context).map(Context::getUserInfo).map(UserInfo::getUserId).orElse(0L));
        userDO.setNick(Optional.of(context).map(Context::getUserInfo).map(UserInfo::getNick).orElse(""));
        if (MapUtils.isNotEmpty(context.getParams())) {
            Object cookies = context.getParams().get("cookies");
            if (cookies != null && cookies instanceof Map) {
                String cna = (String)((Map)cookies).get("cna");
                userDO.setCna(cna);
            }
        }
        // 压测流量设置用户id
        fixUserId4Test(userDO);
        return userDO;
    }

    private void fixUserId4Test(UserDO userDO) {
        if (PressureTestUtil.isFromTest()) {
            userDO.setUserId(PressureTestUtil.pressureTestUserId());
        }
    }


    public ContentMetaInfo getContentMetaInfo(Map<String, Object> requestParams) {
        ContentMetaInfo contentMetaInfo = new ContentMetaInfo();
        List<ItemInfoSourceMetaInfo> itemInfoSourceMetaInfoList = Lists.newArrayList();
        ItemInfoSourceMetaInfo itemInfoSourceMetaInfoTpp = new ItemInfoSourceMetaInfo();
        itemInfoSourceMetaInfoTpp.setSourceName("tpp");
        itemInfoSourceMetaInfoList.add(itemInfoSourceMetaInfoTpp);
        ItemInfoSourceMetaInfo itemInfoSourceMetaInfoCaptain = new ItemInfoSourceMetaInfo();
        itemInfoSourceMetaInfoCaptain.setSourceName("captain");
        //captain SceneCode场景code
        itemInfoSourceMetaInfoCaptain.setSceneCode("visitSupermarket.main");
        itemInfoSourceMetaInfoList.add(itemInfoSourceMetaInfoCaptain);

        ItemGroupMetaInfo itemGroupMetaInfo = new ItemGroupMetaInfo();
        itemGroupMetaInfo.setGroupName("sm_B2C");
        itemGroupMetaInfo.setItemInfoSourceMetaInfos(itemInfoSourceMetaInfoList);
        ItemGroupMetaInfo itemGroupMetaInfo1 = new ItemGroupMetaInfo();
        itemGroupMetaInfo1.setGroupName("sm_O2OOneHour");
        itemGroupMetaInfo1.setItemInfoSourceMetaInfos(itemInfoSourceMetaInfoList);
        ItemGroupMetaInfo itemGroupMetaInfo2 = new ItemGroupMetaInfo();
        itemGroupMetaInfo2.setGroupName("sm_O2OHalfDay");
        itemGroupMetaInfo2.setItemInfoSourceMetaInfos(itemInfoSourceMetaInfoList);
        ItemGroupMetaInfo itemGroupMetaInfo3 = new ItemGroupMetaInfo();
        itemGroupMetaInfo3.setGroupName("sm_O2ONextDay");
        itemGroupMetaInfo3.setItemInfoSourceMetaInfos(itemInfoSourceMetaInfoList);

        List<ItemGroupMetaInfo> itemGroupMetaInfoList = Lists.newArrayList();
        itemGroupMetaInfoList.add(itemGroupMetaInfo);
        itemGroupMetaInfoList.add(itemGroupMetaInfo1);
        itemGroupMetaInfoList.add(itemGroupMetaInfo2);
        itemGroupMetaInfoList.add(itemGroupMetaInfo3);

        ItemMetaInfo itemMetaInfo = new ItemMetaInfo();
        itemMetaInfo.setItemGroupRenderInfoList(itemGroupMetaInfoList);

        contentMetaInfo.setItemMetaInfo(itemMetaInfo);
        ContentRecommendMetaInfo contentRecommendMetaInfo = new ContentRecommendMetaInfo();
        if(MindUtil.isMind(requestParams)){
            contentRecommendMetaInfo.setUseRecommendSpiV2(true);
        }else{
            contentRecommendMetaInfo.setUseRecommendSpiV2(false);
        }

        contentMetaInfo.setContentRecommendMetaInfo(contentRecommendMetaInfo);
        return contentMetaInfo;
    }
}
