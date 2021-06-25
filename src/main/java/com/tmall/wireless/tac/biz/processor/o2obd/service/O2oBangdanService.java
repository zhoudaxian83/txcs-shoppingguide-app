package com.tmall.wireless.tac.biz.processor.o2obd.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.txcs.biz.supermarket.scene.UserParamsKeyConstant;
import com.tmall.txcs.biz.supermarket.scene.util.CsaUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.model.ContentVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextContent;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.txcs.gs.framework.model.meta.*;
import com.tmall.txcs.gs.framework.service.impl.SgFrameworkServiceContent;
import com.tmall.txcs.gs.model.biz.context.PageInfoDO;
import com.tmall.txcs.gs.model.biz.context.SceneInfo;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.FirstScreenMindContentScene;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.utils.PressureTestUtil;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import com.tmall.wireless.tac.client.domain.UserInfo;
import io.reactivex.Flowable;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author haixiao.zhang
 * @date 2021/6/22
 */
@Service
public class O2oBangdanService {

    Logger LOGGER = LoggerFactory.getLogger(FirstScreenMindContentScene.class);

    @Autowired
    SgFrameworkServiceContent sgFrameworkServiceContent;
    @Autowired
    TacLogger tacLogger;

    public Flowable<TacResult<SgFrameworkResponse<ContentVO>>> recommend(RequestContext4Ald context) {

        long startTime = System.currentTimeMillis();
        tacLogger.info("***O2oBangdanService context***:"+ JSON.toJSONString(context));


        SgFrameworkContextContent sgFrameworkContextContent = new SgFrameworkContextContent();
        sgFrameworkContextContent.setRequestParams(context.getParams());

        String csa = MapUtil.getStringWithDefault(context.getAldParam(),UserParamsKeyConstant.USER_PARAMS_KEY_CSA,
            MapUtil.getStringWithDefault(context.getParams(),UserParamsKeyConstant.USER_PARAMS_KEY_CSA,null));
        Long smAreaId = MapUtil.getLongWithDefault(context.getAldParam(),"smAreaId",
            MapUtil.getLongWithDefault(context.getParams(),"smAreaId",0L));
        String contentSetIdList = MapUtil.getStringWithDefault(context.getAldParam(),"contentIds",
            MapUtil.getStringWithDefault(context.getParams(),"contentIds","167004"));
        sgFrameworkContextContent.getUserParams().put("contentSetIdList",contentSetIdList);
        sgFrameworkContextContent.setSceneInfo(getSceneInfo());
        sgFrameworkContextContent.setUserDO(getUserDO(context));
        sgFrameworkContextContent.setLocParams(CsaUtil.parseCsaObj(csa, smAreaId));
        sgFrameworkContextContent.setContentMetaInfo(getContentMetaInfo());
        PageInfoDO pageInfoDO = new PageInfoDO();
        pageInfoDO.setIndex(Integer.parseInt(MapUtil.getStringWithDefault(context.getParams(), "pageStartPosition", "0")));
        pageInfoDO.setPageSize(Integer.valueOf(MapUtil.getStringWithDefault(context.getParams(), "pageSize", "20")));
        sgFrameworkContextContent.setUserPageInfo(pageInfoDO);
        tacLogger.info("*****O2oBangdanService sgFrameworkContextContent.toString()***:"+sgFrameworkContextContent.toString());

        HadesLogUtil.stream(ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_CONTENT)
            .kv("step", "requestLog")
            .kv("userId", Optional.of(sgFrameworkContextContent).map(SgFrameworkContext::getUserDO).map(UserDO::getUserId).map(Objects::toString).orElse("0"))
            .error();
        return sgFrameworkServiceContent.recommend(sgFrameworkContextContent)
            .map(TacResult::newResult)
            .onErrorReturn(r -> TacResult.errorResult(""));
    }


    public SceneInfo getSceneInfo(){
        SceneInfo sceneInfo = new SceneInfo();
        sceneInfo.setBiz(ScenarioConstantApp.BIZ_TYPE_SUPERMARKET);
        sceneInfo.setSubBiz(ScenarioConstantApp.LOC_TYPE_O2O);
        sceneInfo.setScene(ScenarioConstantApp.O2O_BANG_DAN);
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
        ContentRecommendMetaInfo contentRecommendMetaInfo = new ContentRecommendMetaInfo();
        contentRecommendMetaInfo.setUseRecommendSpiV2(false);
        contentMetaInfo.setContentRecommendMetaInfo(contentRecommendMetaInfo);
        return contentMetaInfo;
    }
}
