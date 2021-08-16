package com.tmall.wireless.tac.biz.processor.iconRecommend.service;

import com.google.common.collect.Lists;
import com.tmall.txcs.biz.supermarket.scene.UserParamsKeyConstant;
import com.tmall.txcs.biz.supermarket.scene.util.CsaUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.model.ContentVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextContent;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.txcs.gs.framework.model.meta.*;
import com.tmall.txcs.gs.framework.service.impl.SgFrameworkServiceContent;
import com.tmall.txcs.gs.model.biz.context.PageInfoDO;
import com.tmall.txcs.gs.model.biz.context.SceneInfo;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.wireless.tac.biz.processor.common.RequestKeyConstantApp;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.iconRecommend.constant.ConstantValue;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.DeviceInfo;
import com.tmall.wireless.tac.client.domain.UserInfo;
import io.reactivex.Flowable;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Yushan
 * @date 2021/8/9 5:59 下午
 */
@Service
public class IconRecommendService {

    @Autowired
    SgFrameworkServiceContent sgFrameworkServiceContent;

    @Autowired
    TacLogger logger;

    public Flowable<TacResult<SgFrameworkResponse<ContentVO>>> recommend(Context context, String recommendContent) {

        SgFrameworkContextContent sgFrameworkContextContent = new SgFrameworkContextContent();
        sgFrameworkContextContent.setRequestParams(context.getParams());

        sgFrameworkContextContent.setSceneInfo(getSceneInfo(recommendContent));
        sgFrameworkContextContent.setUserDO(getUserDO(context));
        sgFrameworkContextContent.setLocParams(CsaUtil.parseCsaObj(context.get(UserParamsKeyConstant.USER_PARAMS_KEY_CSA),
                MapUtil.getLongWithDefault(context.getParams(), "smAreaId", 330100L)));
        sgFrameworkContextContent.setContentMetaInfo(getContentMetaInfo(recommendContent));

        PageInfoDO pageInfoDO = new PageInfoDO();
        String index = MapUtil.getStringWithDefault(context.getParams(), RequestKeyConstantApp.INDEX, "0");
        pageInfoDO.setIndex(Integer.parseInt(index));
        sgFrameworkContextContent.setUserPageInfo(pageInfoDO);
        return sgFrameworkServiceContent.recommend(sgFrameworkContextContent)
                .map(TacResult::newResult)
                .onErrorReturn(err -> TacResult.errorResult(""));
    }

    public SceneInfo getSceneInfo(String recommendContent){
        SceneInfo sceneInfo = new SceneInfo();
        sceneInfo.setBiz(ScenarioConstantApp.BIZ_TYPE_SUPERMARKET);
        sceneInfo.setSubBiz(ScenarioConstantApp.LOC_TYPE_B2C);
        if (recommendContent.equals(ConstantValue.CLASSIFIER_WORD)) {
            sceneInfo.setScene(ScenarioConstantApp.SCENARIO_ICON_RECOMMEND_CLASSIFIER);
        } else if (recommendContent.equals(ConstantValue.SCENE_WORD)) {
            sceneInfo.setScene(ScenarioConstantApp.SCENARIO_ICON_RECOMMEND_SCENE);
        }
        return sceneInfo;
    }

    public UserDO getUserDO(Context context){
        UserDO userDO = new UserDO();
        userDO.setUserId(Optional.of(context).map(Context::getUserInfo).map(UserInfo::getUserId).orElse(0L));
        userDO.setNick(Optional.of(context).map(Context::getUserInfo).map(UserInfo::getNick).orElse(""));
        userDO.setUtdid(Optional.of(context).map(Context::getDeviceInfo).map(DeviceInfo::getUtdid).orElse(""));
        if (MapUtils.isNotEmpty(context.getParams())) {
            Object cookies = context.getParams().get("cookies");
            if (cookies != null && cookies instanceof Map) {
                String cna = (String)((Map)cookies).get("cna");
                userDO.setCna(cna);
            }
        }
        return userDO;
    }

    public ContentMetaInfo getContentMetaInfo(String recommendContent) {

        ContentMetaInfo contentMetaInfo = new ContentMetaInfo();
        // TPP
        List<ItemInfoSourceMetaInfo> itemInfoSourceMetaInfoList = Lists.newArrayList();
        ItemInfoSourceMetaInfo itemInfoSourceMetaInfoTpp = new ItemInfoSourceMetaInfo();
        itemInfoSourceMetaInfoTpp.setSourceName("tpp");
        itemInfoSourceMetaInfoList.add(itemInfoSourceMetaInfoTpp);
        // Captain
        ItemInfoSourceMetaInfo itemInfoSourceMetaInfoCaptain = new ItemInfoSourceMetaInfo();
        itemInfoSourceMetaInfoCaptain.setSourceName("captain");
        // Captain SceneCode场景code
        itemInfoSourceMetaInfoCaptain.setSceneCode("shoppingguide.category");

        List<ItemGroupMetaInfo> itemGroupMetaInfoList = Lists.newArrayList();
        itemInfoSourceMetaInfoList.add(itemInfoSourceMetaInfoCaptain);

        ItemGroupMetaInfo itemGroupMetaInfo = new ItemGroupMetaInfo();
        itemGroupMetaInfo.setGroupName("sm_B2C");
        itemGroupMetaInfo.setItemInfoSourceMetaInfos(itemInfoSourceMetaInfoList);
        itemGroupMetaInfoList.add(itemGroupMetaInfo);

        ItemMetaInfo itemMetaInfo = new ItemMetaInfo();
        itemMetaInfo.setItemGroupRenderInfoList(itemGroupMetaInfoList);

        contentMetaInfo.setItemMetaInfo(itemMetaInfo);
        ContentRecommendMetaInfo contentRecommendMetaInfo = new ContentRecommendMetaInfo();
        contentRecommendMetaInfo.setUseRecommendSpiV2(!recommendContent.equals(ConstantValue.CLASSIFIER_WORD));
        contentMetaInfo.setContentRecommendMetaInfo(contentRecommendMetaInfo);
        return contentMetaInfo;
    }
}
