package com.tmall.wireless.tac.biz.processor.firstScreenMind;


import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.txcs.biz.supermarket.scene.UserParamsKeyConstant;
import com.tmall.txcs.biz.supermarket.scene.util.CsaUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.model.*;
import com.tmall.txcs.gs.framework.model.meta.ContentMetaInfo;
import com.tmall.txcs.gs.framework.model.meta.ItemGroupMetaInfo;
import com.tmall.txcs.gs.framework.model.meta.ItemInfoSourceMetaInfo;
import com.tmall.txcs.gs.framework.model.meta.ItemMetaInfo;
import com.tmall.txcs.gs.framework.service.impl.SgFrameworkServiceContent;
import com.tmall.txcs.gs.model.biz.context.PageInfoDO;
import com.tmall.txcs.gs.model.biz.context.SceneInfo;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.UserInfo;
import io.reactivex.Flowable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FirstScreenMindContentScene {

    @Autowired
    SgFrameworkServiceContent sgFrameworkServiceContent;
    @Autowired
    TacLogger  tacLogger;

    public Flowable<TacResult<SgFrameworkResponse<ContentVO>>> recommend(Context context) {

        tacLogger.info("***FirstScreenMindContentScene context***:"+ JSON.toJSONString(context));

        Long smAreaId = MapUtil.getLongWithDefault(context.getParams(), "smAreaId", 330100L);
        SgFrameworkContextContent sgFrameworkContextContent = new SgFrameworkContextContent();
        sgFrameworkContextContent.setRequestParams(context.getParams());


        sgFrameworkContextContent.setSceneInfo(getSceneInfo());
        sgFrameworkContextContent.setUserDO(getUserDO(context));
        sgFrameworkContextContent.setLocParams(CsaUtil.parseCsaObj(context.get(UserParamsKeyConstant.USER_PARAMS_KEY_CSA), smAreaId));
        sgFrameworkContextContent.setContentMetaInfo(getContentMetaInfo());


        PageInfoDO pageInfoDO = new PageInfoDO();
        pageInfoDO.setIndex(Integer.valueOf(MapUtil.getStringWithDefault(context.getParams(), "pageStartPosition", "0")));
        pageInfoDO.setPageSize(Integer.valueOf(MapUtil.getStringWithDefault(context.getParams(), "pageSize", "20")));
        sgFrameworkContextContent.setUserPageInfo(pageInfoDO);
        tacLogger.info("*****FirstScreenMindContentScene sgFrameworkContextContent.toString()***:"+sgFrameworkContextContent.toString());
        return sgFrameworkServiceContent.recommend(sgFrameworkContextContent)
                /*.map(response -> {
                    Map<String, Object> requestParams = sgFrameworkContextContent.getRequestParams();
                    tacLogger.info("*******requestParams:"+ requestParams);
                    if(requestParams == null || requestParams.isEmpty()){
                        return null;
                    }
                    Object isFixPositionBanner = requestParams.get("isFixPositionBanner");
                    Map<String,Object> propertyMap = Maps.newHashMap();

                    propertyMap.put("index",response.getIndex());
                    tacLogger.info("*******isFixPositionBanner:"+ isFixPositionBanner);
                    if((null == isFixPositionBanner) || ("".equals(isFixPositionBanner)) || StringUtils.equalsIgnoreCase("true",String.valueOf(isFixPositionBanner))){
                        if (response.isHasMore()) {
                            propertyMap.put("isFixPositionBanner", true);
                        } else {
                            propertyMap.put("isFixPositionBanner", false);
                            response.setHasMore(true);
                        }
                    } else if(StringUtils.equalsIgnoreCase("false",String.valueOf(isFixPositionBanner))){
                        propertyMap.put("isFixPositionBanner", false);
                    }
                    tacLogger.info("*******propertyMap:"+propertyMap);
                    response.getExtInfos().put("propertyMap", propertyMap);
                    return response;
                })*/
                .map(TacResult::newResult)
                .onErrorReturn(r -> TacResult.errorResult(""));
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
        return userDO;
    }
    public ContentMetaInfo getContentMetaInfo() {
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
        return contentMetaInfo;
    }
}
