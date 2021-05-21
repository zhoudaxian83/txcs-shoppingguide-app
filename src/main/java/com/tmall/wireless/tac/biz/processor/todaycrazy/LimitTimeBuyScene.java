package com.tmall.wireless.tac.biz.processor.todaycrazy;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.tmall.txcs.biz.supermarket.scene.UserParamsKeyConstant;
import com.tmall.txcs.biz.supermarket.scene.util.CsaUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.model.EntityVO;
import com.tmall.txcs.gs.framework.model.ItemEntityVO;
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
import com.tmall.wireless.tac.biz.processor.todaycrazy.model.AldVO;
import com.tmall.wireless.tac.biz.processor.todaycrazy.model.LimitBuyDto;
import com.tmall.wireless.tac.biz.processor.todaycrazy.utils.AldInfoUtil;
import com.tmall.wireless.tac.biz.processor.wzt.constant.Constant;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.UserInfo;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

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

    @Autowired
    TacLogger tacLogger;

    private static final String SceneCode = "superMarket_todayCrazy";

    public Flowable<TacResult<List<AldVO>>> recommend(Context context) {
        tacLogger.info("***LimitTimeBuyScene context.getParams()****:"+context.getParams());
        LOGGER.info("***LimitTimeBuyScene context.getParams()****:"+context.getParams());

        Long smAreaId = MapUtil.getLongWithDefault(context.getParams(), "smAreaId", 330100L);
        SgFrameworkContextItem sgFrameworkContextItem = new SgFrameworkContextItem();

        sgFrameworkContextItem.setRequestParams(context.getParams());

        SceneInfo sceneInfo = new SceneInfo();
        sceneInfo.setBiz(ScenarioConstantApp.BIZ_TYPE_SUPERMARKET);
        sceneInfo.setSubBiz(ScenarioConstantApp.LOC_TYPE_B2C);
        sceneInfo.setScene(ScenarioConstantApp.SCENARIO_TODAY_CRAZY_LIMIT_TIME_BUY);
        sgFrameworkContextItem.setSceneInfo(sceneInfo);

        UserDO userDO = new UserDO();
        userDO.setUserId(Optional.of(context).map(Context::getUserInfo).map(UserInfo::getUserId).orElse(0L));
        userDO.setNick(Optional.of(context).map(Context::getUserInfo).map(UserInfo::getNick).orElse(""));
        sgFrameworkContextItem.setUserDO(userDO);

        sgFrameworkContextItem.setLocParams(CsaUtil.parseCsaObj(context.get(UserParamsKeyConstant.USER_PARAMS_KEY_CSA), smAreaId));
        sgFrameworkContextItem.setItemMetaInfo(getItemMetaInfo());


        PageInfoDO pageInfoDO = new PageInfoDO();
        pageInfoDO.setIndex(0);
        pageInfoDO.setPageSize(20);
        sgFrameworkContextItem.setUserPageInfo(pageInfoDO);

        return sgFrameworkServiceItem.recommend(sgFrameworkContextItem)
                .map(response ->
                    buildAldVO(response,sgFrameworkContextItem)
                ).map(TacResult::newResult)
                .onErrorReturn(r -> TacResult.errorResult(""));

    }
    public List<AldVO> buildAldVO(SgFrameworkResponse sgFrameworkResponse,SgFrameworkContextItem sgFrameworkContextItem){
        perfect(sgFrameworkResponse,sgFrameworkContextItem);
        List<AldVO> aldVOS = new ArrayList<>();
        Map<String, Object> params = sgFrameworkContextItem.getRequestParams();
        //第几个时间段
        int index = aldInfoUtil.getIndex(params);
        //ald排期信息
        Map<String,String> map = aldInfoUtil.getAldInfo(params);
        LinkedHashMap<Long,Long> linkedHashMap = aldInfoUtil.buildTime(map);
        List<LimitBuyDto> limitBuyDtos = Lists.newArrayList();
        //打标命中的时间段
        aldInfoUtil.buildNowTime(linkedHashMap,index,limitBuyDtos);
        AtomicInteger i = new AtomicInteger();
        limitBuyDtos.forEach(limitBuyDto -> {
            AldVO aldVO = new AldVO();
            aldVO.setIsHit(limitBuyDto.getIsHit());
            aldVO.setStartTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(limitBuyDto.getStartTime()*1000)));
            aldVO.set__pos__(i.getAndIncrement());
            if(limitBuyDto.getIsHit()){
                aldVO.setItemAndContentList(sgFrameworkResponse.getItemAndContentList());
            }
            aldVOS.add(aldVO);
        });
        LOGGER.info("***LimitTimeBuyScene sgFrameworkContextItem.getUserParams()****:"+sgFrameworkContextItem.getUserParams().get(Constant.ITEM_LIMIT_RESULT));
        LOGGER.info("***LimitTimeBuyScene aldVOS****:"+aldVOS);
        return aldVOS;
    }

    /**
     * 完善限购信息
     * @param sgFrameworkResponse
     */
    public void perfect(SgFrameworkResponse sgFrameworkResponse,SgFrameworkContextItem sgFrameworkContextItem){
        LOGGER.info("***LimitTimeBuyScene itemAndContentList****:"+ JSON.toJSONString(sgFrameworkResponse.getItemAndContentList()));
        List<ItemEntityVO> itemAndContentList = sgFrameworkResponse.getItemAndContentList();
        Map<String,Object> userParams = sgFrameworkContextItem.getUserParams();
        if(CollectionUtils.isEmpty(itemAndContentList) || MapUtils.isEmpty(userParams)){
            return;
        }
        itemAndContentList.forEach(itemEntityVO -> {
            Long itemId = itemEntityVO.getItemId();
            LOGGER.info("***LimitTimeBuyScene itemEntityVO****:"+itemEntityVO);
            LOGGER.info("***LimitTimeBuyScene itemId****:"+itemId);
            if(itemId != null){
                LOGGER.info("***LimitTimeBuyScene userParams****:"+userParams);
                Object object = userParams.get(String.valueOf(itemId));
                LOGGER.info("***LimitTimeBuyScene object****:"+object);
                if(itemEntityVO != null && itemEntityVO instanceof Map && object != null){
                    itemEntityVO.putAll(JSONObject.parseObject(object.toString()));
                }
            }
        });

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
