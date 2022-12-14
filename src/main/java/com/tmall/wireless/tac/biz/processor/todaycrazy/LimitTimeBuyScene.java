package com.tmall.wireless.tac.biz.processor.todaycrazy;

import com.google.common.collect.Lists;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.txcs.biz.supermarket.scene.UserParamsKeyConstant;
import com.tmall.txcs.biz.supermarket.scene.util.CsaUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.model.EntityVO;
import com.tmall.txcs.gs.framework.model.ItemEntityVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.txcs.gs.framework.model.meta.ItemGroupMetaInfo;
import com.tmall.txcs.gs.framework.model.meta.ItemInfoSourceMetaInfo;
import com.tmall.txcs.gs.framework.model.meta.ItemMetaInfo;
import com.tmall.txcs.gs.framework.service.impl.SgFrameworkServiceItem;
import com.tmall.txcs.gs.model.biz.context.PageInfoDO;
import com.tmall.txcs.gs.model.biz.context.SceneInfo;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.todaycrazy.model.LimitBuyDto;
import com.tmall.wireless.tac.biz.processor.todaycrazy.utils.AldInfoUtil;
import com.tmall.wireless.tac.biz.processor.wzt.constant.Constant;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import com.tmall.wireless.tac.client.domain.UserInfo;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.alibaba.fastjson.JSON;

/**
 * @author guijian
 */
@Service
public class LimitTimeBuyScene {
    Logger LOGGER = LoggerFactory.getLogger(LimitTimeBuyScene.class);
    @Autowired
    SgFrameworkServiceItem sgFrameworkServiceItem;

    @Autowired
    AldInfoUtil aldInfoUtil;

    private static final String SceneCode = "superMarket_todayCrazy";

    public Flowable<TacResult<List<GeneralItem>>> recommend(RequestContext4Ald requestContext4Ald) {

        Long smAreaId = MapUtil.getLongWithDefault(requestContext4Ald.getParams(), "smAreaId", 330100L);
        SgFrameworkContextItem sgFrameworkContextItem = new SgFrameworkContextItem();
        sgFrameworkContextItem.setRequestParams(requestContext4Ald.getParams());

        SceneInfo sceneInfo = new SceneInfo();
        sceneInfo.setBiz(ScenarioConstantApp.BIZ_TYPE_SUPERMARKET);
        sceneInfo.setSubBiz(ScenarioConstantApp.LOC_TYPE_B2C);
        sceneInfo.setScene(ScenarioConstantApp.SCENARIO_TODAY_CRAZY_LIMIT_TIME_BUY);
        sgFrameworkContextItem.setSceneInfo(sceneInfo);

        UserDO userDO = new UserDO();
        userDO.setUserId(Optional.of(requestContext4Ald).map(Context::getUserInfo).map(UserInfo::getUserId).orElse(0L));
        userDO.setNick(Optional.of(requestContext4Ald).map(Context::getUserInfo).map(UserInfo::getNick).orElse(""));
        sgFrameworkContextItem.setUserDO(userDO);

        sgFrameworkContextItem.setLocParams(CsaUtil.parseCsaObj(requestContext4Ald.get(UserParamsKeyConstant.USER_PARAMS_KEY_CSA), smAreaId));
        sgFrameworkContextItem.setItemMetaInfo(getItemMetaInfo());


        PageInfoDO pageInfoDO = new PageInfoDO();
        pageInfoDO.setIndex(0);
        pageInfoDO.setPageSize(20);
        sgFrameworkContextItem.setUserPageInfo(pageInfoDO);

        return sgFrameworkServiceItem.recommend(sgFrameworkContextItem)
                .defaultIfEmpty(new SgFrameworkResponse<EntityVO>())
                .map(response ->{
                        return buildGeneralItemse(response,sgFrameworkContextItem);
                    }
                ).map(TacResult::newResult)
                .onErrorReturn(r -> {
                    HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_TODAY_CRAZY_LIMIT_TIME_BUY)
                        .kv("generalItemse is","empty")
                        .info();
                    List<GeneralItem> generalItemse = new ArrayList<>();
                    TacResult tacResult = TacResult.newResult(generalItemse);
                    return tacResult;
                    });
    }
    public List<GeneralItem> buildGeneralItemse(SgFrameworkResponse sgFrameworkResponse,SgFrameworkContextItem sgFrameworkContextItem){
        /*perfect(sgFrameworkResponse,sgFrameworkContextItem);*/
        List<GeneralItem> generalItemse = new ArrayList<>();
        Map<String, Object> params = sgFrameworkContextItem.getRequestParams();
        //??????????????????
        int index = aldInfoUtil.getIndex(params);
        //ald????????????
        Map<String,String> map = aldInfoUtil.getAldInfo(params);
        LinkedHashMap<Long,Long> linkedHashMap = aldInfoUtil.buildTime(map);
        List<LimitBuyDto> limitBuyDtos = Lists.newArrayList();
        //????????????????????????
        aldInfoUtil.buildNowTime(linkedHashMap,index,limitBuyDtos);
        AtomicInteger i = new AtomicInteger();
        limitBuyDtos.forEach(limitBuyDto -> {
            GeneralItem generalItem = new GeneralItem();
            generalItem.put("isHit",limitBuyDto.getIsHit());
            generalItem.put("startTime",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(limitBuyDto.getStartTime()*1000)));
            generalItem.put("__pos__",i.getAndIncrement());
            if(limitBuyDto.getIsHit()){
                if(CollectionUtils.isNotEmpty(sgFrameworkResponse.getItemAndContentList())){
                    generalItem.put("itemAndContentList",soltOutSort(sgFrameworkResponse.getItemAndContentList()));
                }
            }
            generalItemse.add(generalItem);


        });
        return generalItemse;
    }

    /**
     * ???????????????????????????
     * @param itemEntityVOS
     * @return
     */
    public List<ItemEntityVO> soltOutSort(List<ItemEntityVO> itemEntityVOS){
        if(CollectionUtils.isEmpty(itemEntityVOS)){
            return itemEntityVOS;
        }
        List<ItemEntityVO> frontList = new ArrayList<>();
        List<ItemEntityVO> backList = new ArrayList<>();
        itemEntityVOS.forEach(itemEntityVO -> {
            if(null != itemEntityVO.get("soldOut") && (Boolean)itemEntityVO.get("soldOut")){
                backList.add(itemEntityVO);
            }else{
                frontList.add(itemEntityVO);
            }
        });
        frontList.addAll(backList);
        return frontList;

    }


    /**
     * ??????????????????
     * @param sgFrameworkResponse
     */
    public void perfect(SgFrameworkResponse sgFrameworkResponse,SgFrameworkContextItem sgFrameworkContextItem){
        LOGGER.info("***LimitTimeBuyScene sgFrameworkContextItem****:"+sgFrameworkContextItem);
        if(sgFrameworkResponse == null || sgFrameworkResponse.getItemAndContentList() == null
            || sgFrameworkContextItem == null && sgFrameworkContextItem.getUserParams() == null){
            return;
        }
        List<ItemEntityVO> itemEntityVOS = sgFrameworkResponse.getItemAndContentList();
        Map<String, Object> userParams = sgFrameworkContextItem.getUserParams();
        itemEntityVOS.forEach(itemEntityVO -> {
            String itemId = itemEntityVO.getString("itemId");
            Object itemLimitResult = userParams.get("itemLimitResult");
            if(StringUtils.isNotEmpty(itemId) && itemLimitResult != null && itemLimitResult instanceof Map){
                Object itemIdLists = ((Map<String, Object>)itemLimitResult).get(itemId);
                if(itemIdLists != null && itemIdLists instanceof List){
                    ((List<?>)itemIdLists).forEach(itemIdMap -> {
                        if(itemIdMap != null && itemIdMap instanceof Map){
                            itemEntityVO.putAll((Map<String,Object>)itemIdMap);
                        }
                    });


                }
            }
        });
        LOGGER.info("***LimitTimeBuyScene itemEntityVOS****:"+JSON.toJSONString(itemEntityVOS));

    }
    public static ItemMetaInfo getItemMetaInfo() {
        ItemMetaInfo itemMetaInfo = new ItemMetaInfo();
        List<ItemGroupMetaInfo> itemGroupMetaInfoList = Lists.newArrayList();
        List<ItemInfoSourceMetaInfo> itemInfoSourceMetaInfoList = Lists.newArrayList();
        ItemGroupMetaInfo itemGroupMetaInfo = new ItemGroupMetaInfo();
        itemGroupMetaInfoList.add(itemGroupMetaInfo);
        itemGroupMetaInfo.setGroupName("sm_B2C");
        itemGroupMetaInfo.setItemInfoSourceMetaInfos(itemInfoSourceMetaInfoList);
        ItemGroupMetaInfo itemGroupMetaInfo1 = new ItemGroupMetaInfo();
        itemGroupMetaInfoList.add(itemGroupMetaInfo1);
        itemGroupMetaInfo1.setGroupName("sm_O2OOneHour");
        itemGroupMetaInfo1.setItemInfoSourceMetaInfos(itemInfoSourceMetaInfoList);
        ItemGroupMetaInfo itemGroupMetaInfo2 = new ItemGroupMetaInfo();
        itemGroupMetaInfoList.add(itemGroupMetaInfo2);
        itemGroupMetaInfo2.setGroupName("sm_O2OHalfDay");
        itemGroupMetaInfo2.setItemInfoSourceMetaInfos(itemInfoSourceMetaInfoList);
        ItemGroupMetaInfo itemGroupMetaInfo3 = new ItemGroupMetaInfo();
        itemGroupMetaInfoList.add(itemGroupMetaInfo3);
        itemGroupMetaInfo3.setGroupName("sm_O2ONextDay");
        itemGroupMetaInfo3.setItemInfoSourceMetaInfos(itemInfoSourceMetaInfoList);
        ItemInfoSourceMetaInfo itemInfoSourceMetaInfoCaptain = new ItemInfoSourceMetaInfo();
        itemInfoSourceMetaInfoCaptain.setSourceName("captain");
        itemInfoSourceMetaInfoCaptain.setSceneCode(SceneCode);
        itemInfoSourceMetaInfoList.add(itemInfoSourceMetaInfoCaptain);
        itemMetaInfo.setItemGroupRenderInfoList(itemGroupMetaInfoList);
        ItemInfoSourceMetaInfo itemInfoSourceMetaInfoTpp = new ItemInfoSourceMetaInfo();
        itemInfoSourceMetaInfoTpp.setSourceName("tpp");
        itemInfoSourceMetaInfoList.add(itemInfoSourceMetaInfoTpp);

        ItemInfoSourceMetaInfo itemInfoSourceMetaInfoInv = new ItemInfoSourceMetaInfo();
        itemInfoSourceMetaInfoInv.setSourceName("inventory");
        itemInfoSourceMetaInfoList.add(itemInfoSourceMetaInfoInv);

        itemMetaInfo.setItemGroupRenderInfoList(itemGroupMetaInfoList);
        return itemMetaInfo;
    }
}
