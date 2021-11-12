package com.tmall.wireless.tac.biz.processor.firstScreenMind;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.taobao.eagleeye.EagleEye;
import com.tmall.aselfcaptain.item.model.ChannelDataDO;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.txcs.biz.supermarket.scene.UserParamsKeyConstant;
import com.tmall.txcs.biz.supermarket.scene.util.CsaUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.model.ContentVO;
import com.tmall.txcs.gs.framework.model.EntityVO;
import com.tmall.txcs.gs.framework.model.ErrorCode;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.txcs.gs.framework.model.constant.ItemInfoSourceKey;
import com.tmall.txcs.gs.framework.model.meta.ItemGroupMetaInfo;
import com.tmall.txcs.gs.framework.model.meta.ItemInfoSourceMetaInfo;
import com.tmall.txcs.gs.framework.model.meta.ItemMetaInfo;
import com.tmall.txcs.gs.framework.model.meta.node.ItemInfoNode;
import com.tmall.txcs.gs.framework.service.impl.SgFrameworkServiceItem;
import com.tmall.txcs.gs.model.biz.context.LocParams;
import com.tmall.txcs.gs.model.biz.context.PageInfoDO;
import com.tmall.txcs.gs.model.biz.context.SceneInfo;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.wireless.tac.biz.processor.common.RequestKeyConstantApp;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.common.util.TacResultBackupUtil;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.common.ContentInfoSupport;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.RenderContentTypeEnum;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.utils.ContentSetIdListUtil;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.alibaba.fastjson.JSON;

@Service
public class FirstScreenMindItemScene {

    private static final String CHANNELNAME = "sceneLdb";
    private static final String ACTIVITY_SCENE_PREFIX = "tcls_ugc_scene_v1_";

    @Autowired
    TacLogger tacLogger;
    @Autowired
    SgFrameworkServiceItem sgFrameworkServiceItem;

    @Autowired
    ContentInfoSupport contentInfoSupport;

    public Flowable<TacResult<SgFrameworkResponse<EntityVO>>> recommend(Context context) {

        HadesLogUtil.stream(ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_ITEM)
            .kv("FirstScreenMindItemScene", "recommend")
            .kv("context",JSON.toJSONString(context))
            .info();

        /**兼容前端无效请求**/
        String noProcess = MapUtil.getStringWithDefault(context.getParams(),
            RequestKeyConstantApp.FIRST_SCREEN_NO_PROCESS,"false");
        if(StringUtils.isNotBlank(noProcess) && "true".equals(noProcess)){
            SgFrameworkResponse<EntityVO> response = new SgFrameworkResponse<>();
            EntityVO entityVO = new EntityVO();
            /*entityVO.put("noProcess",noProcess);*/
            List<EntityVO> entityVOS = Lists.newArrayList(entityVO);
            response.setItemAndContentList(entityVOS);
            response.setHasMore(true);
            response.setSuccess(true);
            return Flowable.just(TacResult.newResult(response));
        }

        Long smAreaId = MapUtil.getLongWithDefault(context.getParams(), "smAreaId", 330100L);

        SgFrameworkContextItem sgFrameworkContextItem = new SgFrameworkContextItem();

        sgFrameworkContextItem.setRequestParams(context.getParams());

        sgFrameworkContextItem.setSceneInfo(getSceneInfo());
        sgFrameworkContextItem.setUserDO(getUserDO(context));
        sgFrameworkContextItem.setLocParams(CsaUtil.parseCsaObj(context.get(UserParamsKeyConstant.USER_PARAMS_KEY_CSA), smAreaId));
        sgFrameworkContextItem.setItemMetaInfo(this.getRecommendItemMetaInfo(sgFrameworkContextItem));


        PageInfoDO pageInfoDO = new PageInfoDO();
        pageInfoDO.setIndex(Integer.parseInt(MapUtil.getStringWithDefault(context.getParams(), "pageStartPosition", "0")));
        pageInfoDO.setPageSize(Integer.valueOf(MapUtil.getStringWithDefault(context.getParams(), "pageSize", "20")));
        sgFrameworkContextItem.setUserPageInfo(pageInfoDO);
        HadesLogUtil.stream(ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_ITEM)
            .kv("step", "requestLog")
            .kv("userId", Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getUserDO).map(UserDO::getUserId).map(
                Objects::toString).orElse("0"))
            .kv("sgFrameworkContextItem", JSON.toJSONString(sgFrameworkContextItem))
            .info();

        return sgFrameworkServiceItem.recommend(sgFrameworkContextItem)
                .map(response -> {
                    Map<String, Object> contentInfo = queryContentInfo(sgFrameworkContextItem);

                    if (response.getExtInfos() == null) {
                        response.setExtInfos(Maps.newHashMap());
                    }
                    response.getExtInfos().put("contentModel", contentInfo);
                    response.getExtInfos().put("traceId", EagleEye.getTraceId());
                    return response;
                })
                .map(TacResult::newResult)
                .map(tacResult -> {
                    BizScenario b = BizScenario.valueOf(
                        ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
                        ScenarioConstantApp.LOC_TYPE_B2C,
                        ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_ITEM
                    );
                    tacResult = TacResultBackupUtil.tacResultBackupItem(tacResult,b);
                    return tacResult;
                })
                .onErrorReturn(r -> TacResult.errorResult(""));

    }

    protected Map<String, Object> queryContentInfo(SgFrameworkContextItem sgFrameworkContextItem) {
        Map<String, Object> contentInfo = Maps.newHashMap();
        Long moduleId = MapUtil.getLongWithDefault(sgFrameworkContextItem.getRequestParams(), "moduleId", 0L);
        if (moduleId <= 0) {
            moduleId = MapUtil.getLongWithDefault(sgFrameworkContextItem.getRequestParams(), "contentId", 0L);;
        }
        if (moduleId <= 0) {
            return contentInfo;
        }
        Long contentId = moduleId;
        Map<Long, Map<String, Object>> contentIdToContentInfoMap = contentInfoSupport.queryContentInfoByContentIdList(Lists.newArrayList(moduleId));
        return Optional.ofNullable(contentIdToContentInfoMap).map(map -> map.get(contentId)).orElse(Maps.newHashMap());
    }

    public SceneInfo getSceneInfo(){
        SceneInfo sceneInfo = new SceneInfo();
        sceneInfo.setBiz(ScenarioConstantApp.BIZ_TYPE_SUPERMARKET);
        sceneInfo.setSubBiz(ScenarioConstantApp.LOC_TYPE_B2C);
        sceneInfo.setScene(ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_ITEM);
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
        // 压测用户id
        fixUserId4Test(userDO);
        return userDO;
    }

    private void fixUserId4Test(UserDO userDO) {
        if (PressureTestUtil.isFromTest()) {
            userDO.setUserId(PressureTestUtil.pressureTestUserId());
        }
    }

//    public ItemMetaInfo getRecommendItemMetaInfo(SgFrameworkContextItem sgFrameworkContextItem) {
//        ItemMetaInfo itemMetaInfo = new ItemMetaInfo();
//        List<ItemGroupMetaInfo> itemGroupMetaInfoList = Lists.newArrayList();
//        List<ItemInfoSourceMetaInfo> itemInfoSourceMetaInfoList = Lists.newArrayList();
//        ItemInfoSourceMetaInfo itemInfoSourceMetaInfoCaptain = new ItemInfoSourceMetaInfo();
//        itemInfoSourceMetaInfoCaptain.setSourceName("captain");
//
//        if(!isO2oScene(sgFrameworkContextItem)){
//            itemInfoSourceMetaInfoCaptain.setSceneCode("visitSupermarket.main");
//        }
//        itemInfoSourceMetaInfoList.add(itemInfoSourceMetaInfoCaptain);
//        itemMetaInfo.setItemGroupRenderInfoList(itemGroupMetaInfoList);
//        ItemInfoSourceMetaInfo itemInfoSourceMetaInfoTpp = new ItemInfoSourceMetaInfo();
//        itemInfoSourceMetaInfoTpp.setSourceName("tpp");
//        itemInfoSourceMetaInfoList.add(itemInfoSourceMetaInfoTpp);
//
//
//
//        List<ItemInfoNode> itemInfoNodes = Lists.newArrayList();
//        ItemInfoNode itemInfoNodeFirst = new ItemInfoNode();
//        itemInfoNodes.add(itemInfoNodeFirst);
//        itemInfoNodeFirst.setItemInfoSourceMetaInfos(itemInfoSourceMetaInfoList);
//
//
//        ItemInfoNode itemInfoNodeSceond = new ItemInfoNode();
//        itemInfoNodes.add(itemInfoNodeSceond);
//        itemInfoNodeSceond.setItemInfoSourceMetaInfos(Lists.newArrayList(getItemInfoBySourceTimeLabel()));
//
//        ItemGroupMetaInfo itemGroupMetaInfo = new ItemGroupMetaInfo();
//        itemGroupMetaInfoList.add(itemGroupMetaInfo);
//        itemGroupMetaInfo.setGroupName("sm_B2C");
//        itemGroupMetaInfo.setItemInfoSourceMetaInfos(itemInfoSourceMetaInfoList);
//        ItemGroupMetaInfo itemGroupMetaInfo1 = new ItemGroupMetaInfo();
//        itemGroupMetaInfoList.add(itemGroupMetaInfo1);
//        itemGroupMetaInfo1.setGroupName("sm_O2OOneHour");
////        itemGroupMetaInfo1.setItemInfoSourceMetaInfos(itemInfoSourceMetaInfoList);
//        itemGroupMetaInfo1.setItemInfoNodes(itemInfoNodes);
//        ItemGroupMetaInfo itemGroupMetaInfo2 = new ItemGroupMetaInfo();
//        itemGroupMetaInfoList.add(itemGroupMetaInfo2);
//        itemGroupMetaInfo2.setGroupName("sm_O2OHalfDay");
////        itemGroupMetaInfo2.setItemInfoSourceMetaInfos(itemInfoSourceMetaInfoList);
//        itemGroupMetaInfo2.setItemInfoNodes(itemInfoNodes);
//        ItemGroupMetaInfo itemGroupMetaInfo3 = new ItemGroupMetaInfo();
//        itemGroupMetaInfoList.add(itemGroupMetaInfo3);
//        itemGroupMetaInfo3.setGroupName("sm_O2ONextDay");
//        itemGroupMetaInfo3.setItemInfoSourceMetaInfos(itemInfoSourceMetaInfoList);
//
//        itemMetaInfo.setItemGroupRenderInfoList(itemGroupMetaInfoList);
//        return itemMetaInfo;
//    }

    public ItemMetaInfo getRecommendItemMetaInfo(SgFrameworkContextItem sgFrameworkContextItem) {
        ItemMetaInfo itemMetaInfo = new ItemMetaInfo();
        List<ItemGroupMetaInfo> itemGroupMetaInfoList = Lists.newArrayList();
        List<ItemInfoSourceMetaInfo> itemInfoSourceMetaInfoList = Lists.newArrayList();
        ItemInfoSourceMetaInfo itemInfoSourceMetaInfoCaptain = new ItemInfoSourceMetaInfo();
        itemInfoSourceMetaInfoCaptain.setSourceName("captain");
        itemInfoSourceMetaInfoCaptain.setSceneCode("visitSupermarket.main");
        itemInfoSourceMetaInfoList.add(itemInfoSourceMetaInfoCaptain);


        itemMetaInfo.setItemGroupRenderInfoList(itemGroupMetaInfoList);
        ItemInfoSourceMetaInfo itemInfoSourceMetaInfoTpp = new ItemInfoSourceMetaInfo();
        itemInfoSourceMetaInfoTpp.setSourceName("tpp");
        itemInfoSourceMetaInfoList.add(itemInfoSourceMetaInfoTpp);

        boolean bangdan = isBangdan(sgFrameworkContextItem);
        if(bangdan){
            ItemInfoSourceMetaInfo channelDataItemInfoSource = new ItemInfoSourceMetaInfo();
            channelDataItemInfoSource.setSourceName("captain_channel");
            String sKey = MapUtil.getStringWithDefault(sgFrameworkContextItem.getRequestParams(),"contentId","");
            String key  = ACTIVITY_SCENE_PREFIX + sKey + "_";
            channelDataItemInfoSource.setQueryCaptainChannelKeyPrefix(key);

            List<ChannelDataDO> channelDataDOList = new ArrayList<>();
            List<String> paramsName = Arrays.asList("itemId", "itemRankValue", "itemRankDesc");
            for(String paramName : paramsName){
                ChannelDataDO channelDataDO = new ChannelDataDO();
                channelDataDO.setDataKey(paramName);
                channelDataDO.setChannelField(paramName);
                channelDataDO.setChannelName(CHANNELNAME);
                channelDataDOList.add(channelDataDO);
            }
            channelDataItemInfoSource.setChannelFields(channelDataDOList);
            //Map<String, String> extraMap = Maps.newHashMap();
            //extraMap.put("salePointType", "MOST_WORTH_BUYING");
            //channelDataItemInfoSource.setExtraMap(extraMap);
            itemInfoSourceMetaInfoList.add(channelDataItemInfoSource);
        }

        List<ItemInfoNode> itemInfoNodes = Lists.newArrayList();
        ItemInfoNode itemInfoNodeFirst = new ItemInfoNode();
        itemInfoNodes.add(itemInfoNodeFirst);
        itemInfoNodeFirst.setItemInfoSourceMetaInfos(itemInfoSourceMetaInfoList);


        ItemInfoNode itemInfoNodeSceond = new ItemInfoNode();
        itemInfoNodes.add(itemInfoNodeSceond);
        itemInfoNodeSceond.setItemInfoSourceMetaInfos(Lists.newArrayList(getItemInfoBySourceTimeLabel()));

        ItemGroupMetaInfo itemGroupMetaInfo = new ItemGroupMetaInfo();
        itemGroupMetaInfoList.add(itemGroupMetaInfo);
        itemGroupMetaInfo.setGroupName("sm_B2C");
        itemGroupMetaInfo.setItemInfoSourceMetaInfos(itemInfoSourceMetaInfoList);
        ItemGroupMetaInfo itemGroupMetaInfo1 = new ItemGroupMetaInfo();
        itemGroupMetaInfoList.add(itemGroupMetaInfo1);
        itemGroupMetaInfo1.setGroupName("sm_O2OOneHour");
        //        itemGroupMetaInfo1.setItemInfoSourceMetaInfos(itemInfoSourceMetaInfoList);
        itemGroupMetaInfo1.setItemInfoNodes(itemInfoNodes);
        ItemGroupMetaInfo itemGroupMetaInfo2 = new ItemGroupMetaInfo();
        itemGroupMetaInfoList.add(itemGroupMetaInfo2);
        itemGroupMetaInfo2.setGroupName("sm_O2OHalfDay");
        //        itemGroupMetaInfo2.setItemInfoSourceMetaInfos(itemInfoSourceMetaInfoList);
        itemGroupMetaInfo2.setItemInfoNodes(itemInfoNodes);
        ItemGroupMetaInfo itemGroupMetaInfo3 = new ItemGroupMetaInfo();
        itemGroupMetaInfoList.add(itemGroupMetaInfo3);
        itemGroupMetaInfo3.setGroupName("sm_O2ONextDay");
        itemGroupMetaInfo3.setItemInfoSourceMetaInfos(itemInfoSourceMetaInfoList);

        itemMetaInfo.setItemGroupRenderInfoList(itemGroupMetaInfoList);
        return itemMetaInfo;
    }

    private ItemInfoSourceMetaInfo getItemInfoBySourceTimeLabel() {
        ItemInfoSourceMetaInfo itemInfoSourceMetaInfo = new ItemInfoSourceMetaInfo();
        itemInfoSourceMetaInfo.setSourceName("timeLabel");
        return itemInfoSourceMetaInfo;
    }

    private boolean isO2oScene(SgFrameworkContext sgFrameworkContext) {

        String contentType = MapUtil.getStringWithDefault(sgFrameworkContext.getRequestParams(), RequestKeyConstantApp.CONTENT_TYPE, RenderContentTypeEnum.b2cNormalContent.getType());
        return RenderContentTypeEnum.checkO2OContentType(contentType);
    }

    private boolean isBangdan(SgFrameworkContextItem sgFrameworkContextItem) {
        String contentType = MapUtil.getStringWithDefault(sgFrameworkContextItem.getRequestParams(), RequestKeyConstantApp.CONTENT_TYPE, RenderContentTypeEnum.b2cNormalContent.getType());
        return RenderContentTypeEnum.bangdanContent.getType().equals(contentType) || RenderContentTypeEnum.bangdanO2OContent.getType().equals(contentType);
    }
}
