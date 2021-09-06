package com.tmall.wireless.tac.biz.processor.newproduct.service;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.tcls.experiment.client.router.HyperlocalRetailABTestClient;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.aselfcaptain.item.api.ChannelQueryService;
import com.tmall.aselfcaptain.item.model.ChannelDataDO;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.txcs.biz.supermarket.scene.UserParamsKeyConstant;
import com.tmall.txcs.biz.supermarket.scene.util.CsaUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.model.*;
import com.tmall.txcs.gs.framework.model.meta.*;
import com.tmall.txcs.gs.framework.service.impl.SgFrameworkServiceItem;
import com.tmall.txcs.gs.model.biz.context.EntitySetParams;
import com.tmall.txcs.gs.model.biz.context.PageInfoDO;
import com.tmall.txcs.gs.model.biz.context.SceneInfo;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.wireless.tac.biz.processor.common.RequestKeyConstantApp;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.config.SxlSwitch;
import com.tmall.wireless.tac.biz.processor.newproduct.constant.Constant;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.UserInfo;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author haixiao.zhang
 * @date 2021/6/9
 */
@Service
public class SxlItemRecService {

    Logger LOGGER = LoggerFactory.getLogger(SxlItemRecService.class);

    @Autowired
    TacLogger tacLogger;

    @Autowired
    SgFrameworkServiceItem sgFrameworkServiceItem;
    @Autowired
    HyperlocalRetailABTestClient hyperlocalRetailABTestClient;
    /*@Autowired
    ChannelQueryService channelQueryService;*/

    /**ab实验分桶结果**/
    private final static String AB_TEST_RESULT = "abTestVariationsResult";
    /**人工选品**/
    private final static String ARTIFICIAL = "Artificial";
    /**人工选品-算法选品**/
    private final static String ARTIFICIAL_ALGORITHM = "Artificial-Algorithm";

    static List<Pair<String, String>> dataTubeKeyList = Lists.newArrayList(
        Pair.of("recommendWords","recommendWords"),
        Pair.of("videoUrl","videoUrl"),
        Pair.of("type","type"),
        Pair.of("atmosphereImageUrl","atmosphereImageUrl"),
        Pair.of("sellingPointDesc","sellingPointDesc"),
        Pair.of("newItemAttribute","newItemAttribute")
    );

    public Flowable<TacResult<SgFrameworkResponse<EntityVO>>> recommend(Context context) {

        Long smAreaId = MapUtil.getLongWithDefault(context.getParams(), "smAreaId", 330100L);
        /**招商人工选品集id集合**/
        Long itemSetIdSw = Long.valueOf(SxlSwitch.SXL_ITEMSET_ID);
        /**算法选品集id集合**/
        Long itemSetIdAlgSw = Long.valueOf(SxlSwitch.SXL_ALG_ITEMSET_ID);
        /**主题承接页圈品集id**/
        Long itemSetId = MapUtil.getLongWithDefault(context.getParams(), RequestKeyConstantApp.ITEMSET_ID,0L);
        /**招商主活动id-管道tair key**/
        String activityId = MapUtil.getStringWithDefault(context.getParams(), RequestKeyConstantApp.SXL_MAIN_ACTIVITY_ID,"");
        HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM)
            .kv("SxlItemRecService itemSetIdSw",JSON.toJSONString(itemSetIdSw))
            .kv("SxlItemRecService itemSetIdAlgSw",JSON.toJSONString(itemSetIdAlgSw))
            .kv("SxlItemRecService itemSetId",JSON.toJSONString(itemSetId))
            .kv("SxlItemRecService activityId",activityId)
            .info();

        String topItemIds = MapUtil.getStringWithDefault(context.getParams(), "itemIds","");

        SgFrameworkContextItem sgFrameworkContextItem = new SgFrameworkContextItem();
        EntitySetParams entitySetParams = new EntitySetParams();
        entitySetParams.setItemSetSource("crm");
        String abTestType = "";
        /**主题系列新品承接页**/
        if(!StringUtils.isBlank(activityId) && itemSetId > 0){
            entitySetParams.setItemSetIdList(Lists.newArrayList(itemSetId));
            sgFrameworkContextItem.setItemMetaInfo(getItemMetaInfo(Lists.newArrayList(activityId)));
        }else {
            /**算法选品接入ab实验**/
            String itemSetIdType = getAbData(context);
            if(!StringUtils.isBlank(itemSetIdType)){
                if("old".equals(itemSetIdType)){
                    entitySetParams.setItemSetIdList(Lists.newArrayList(itemSetIdSw));
                    sgFrameworkContextItem.setItemMetaInfo(getItemMetaInfo(Lists.newArrayList(String.valueOf(itemSetIdSw))));
                    abTestType = ARTIFICIAL;
                }else if("new".equals(itemSetIdType)){
                    List<Long> itemSetIds = Lists.newArrayList();
                    itemSetIds.add(itemSetIdSw);
                    itemSetIds.add(itemSetIdAlgSw);
                    entitySetParams.setItemSetIdList(itemSetIds);
                    List<String> activityIds = Lists.newArrayList();
                    activityIds.add(String.valueOf(itemSetIdSw));
                    activityIds.add(String.valueOf(itemSetIdAlgSw));
                    sgFrameworkContextItem.setItemMetaInfo(getItemMetaInfo(activityIds));
                    abTestType = ARTIFICIAL_ALGORITHM;
                }else{
                    entitySetParams.setItemSetIdList(Lists.newArrayList(itemSetIdSw));
                    sgFrameworkContextItem.setItemMetaInfo(getItemMetaInfo(Lists.newArrayList(String.valueOf(itemSetIdSw))));
                    abTestType = ARTIFICIAL;
                }
            }else{
                /**格物不支持未登录用户的ab能力，未登录用户默认走人工选品**/
                abTestType = ARTIFICIAL;
                activityId = String.valueOf(itemSetIdSw);
                entitySetParams.setItemSetIdList(Lists.newArrayList(itemSetIdSw));
                sgFrameworkContextItem.setItemMetaInfo(getItemMetaInfo(Lists.newArrayList(String.valueOf(itemSetIdSw))));
            }
            HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM)
                .kv("SxlItemRecService","recommend")
                .kv("abTestType",abTestType)
                .kv("itemSetIdType",itemSetIdType)
                .info();
        }
        sgFrameworkContextItem.setRequestParams(context.getParams());
        sgFrameworkContextItem.setEntitySetParams(entitySetParams);
        HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM)
            .kv("activityId",activityId)
            .kv("SxlItemRecService entitySetParams.getItemSetIdList()",JSON.toJSONString(entitySetParams.getItemSetIdList()))
            .info();
        SceneInfo sceneInfo = new SceneInfo();
        sceneInfo.setBiz(ScenarioConstantApp.BIZ_TYPE_SUPERMARKET);
        sceneInfo.setSubBiz(ScenarioConstantApp.LOC_TYPE_B2C);
        sceneInfo.setScene(ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM);
        sgFrameworkContextItem.setSceneInfo(sceneInfo);
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
        sgFrameworkContextItem.setUserDO(userDO);

        sgFrameworkContextItem.setLocParams(CsaUtil
            .parseCsaObj(context.get(UserParamsKeyConstant.USER_PARAMS_KEY_CSA), smAreaId));

        PageInfoDO pageInfoDO = new PageInfoDO();
        String index = MapUtil.getStringWithDefault(context.getParams(), RequestKeyConstantApp.INDEX, "0");
        String pageSize = MapUtil.getStringWithDefault(context.getParams(), RequestKeyConstantApp.PAGE_SIZE, "20");
        pageInfoDO.setIndex(Integer.valueOf(index));
        pageInfoDO.setPageSize(Integer.valueOf(pageSize));
        sgFrameworkContextItem.setUserPageInfo(pageInfoDO);

        if(StringUtils.isNotBlank(topItemIds)){
            sgFrameworkContextItem.getUserParams().put(Constant.SXL_TOP_ITEM_IDS,topItemIds);
        }

        String finalAbTestType = abTestType;
        return sgFrameworkServiceItem.recommend(sgFrameworkContextItem)
            .map(response -> {
                if(StringUtils.isNotBlank(finalAbTestType)){
                    response.getExtInfos().put("abTestType", finalAbTestType);
                }
                HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM)
                    .kv("SxlItemRecService finalAbTestType",finalAbTestType)
                    .info();
                return response;
            })
            .map(TacResult::newResult)
            .onErrorReturn(r -> TacResult.errorResult(""));

    }

    private static ItemMetaInfo getItemMetaInfo(List<String> activityIds) {
        ItemMetaInfo itemMetaInfo = new ItemMetaInfo();
        List<ItemGroupMetaInfo> itemGroupMetaInfoList = Lists.newArrayList();
        List<ItemInfoSourceMetaInfo> itemInfoSourceMetaInfoList = Lists.newArrayList();
        ItemGroupMetaInfo itemGroupMetaInfo1 = new ItemGroupMetaInfo();
        itemGroupMetaInfoList.add(itemGroupMetaInfo1);
        itemGroupMetaInfo1.setGroupName("sm_B2C");
        itemGroupMetaInfo1.setItemInfoSourceMetaInfos(itemInfoSourceMetaInfoList);
        for(int i=0;i<activityIds.size();i++){
            ItemInfoSourceMetaInfo itemInfoSourceMetaInfoCaptain = new ItemInfoSourceMetaInfo();
            itemInfoSourceMetaInfoCaptain.setSourceName("captain");
            itemInfoSourceMetaInfoCaptain.setSceneCode("shoppingguide.newLauch.common");
            itemInfoSourceMetaInfoCaptain.setDataTubeMateInfo(buildDataTubeMateInfo(activityIds.get(i)));
            itemInfoSourceMetaInfoList.add(itemInfoSourceMetaInfoCaptain);
        }
        itemMetaInfo.setItemGroupRenderInfoList(itemGroupMetaInfoList);
        ItemInfoSourceMetaInfo itemInfoSourceMetaInfoTpp = new ItemInfoSourceMetaInfo();
        itemInfoSourceMetaInfoTpp.setSourceName("tpp");
        itemInfoSourceMetaInfoList.add(itemInfoSourceMetaInfoTpp);


        ItemRecommendMetaInfo itemRecommendMetaInfo = new ItemRecommendMetaInfo();

        itemRecommendMetaInfo.setAppId(25385L);
        itemMetaInfo.setItemRecommendMetaInfo(itemRecommendMetaInfo);
        itemMetaInfo.setItemGroupRenderInfoList(itemGroupMetaInfoList);
        itemRecommendMetaInfo.setUseRecommendSpiV2(true);
        return itemMetaInfo;
    }


    private static DataTubeMateInfo buildDataTubeMateInfo(String activityId) {

        DataTubeMateInfo dataTubeMateInfo = new DataTubeMateInfo();
        dataTubeMateInfo.setActivityId(activityId);
        dataTubeMateInfo.setChannelName("itemExtLdb");
        dataTubeMateInfo.setDataKeyList(dataTubeKeyList.stream().map(k -> {
            DataTubeKey dataTubeKey = new DataTubeKey();
            dataTubeKey.setDataKey(k.getRight());
            dataTubeKey.setVoKey(k.getLeft());
            return dataTubeKey;
        }).collect(Collectors.toList()));
        return dataTubeMateInfo;
    }

    /**
     * 获取ab数据
     * @param context
     * @return
     */
    private String getAbData(Context context){
        /*getChannelDate();*/
        StringBuilder itemSetIdType = new StringBuilder();
        try {
            if(context.getParams().get(AB_TEST_RESULT) == null
                || StringUtils.isBlank(context.getParams().get(AB_TEST_RESULT).toString())){
                HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM)
                    .kv("SxlItemRecService context.getParams()",JSON.toJSONString(context.getParams()))
                    .info();
                return itemSetIdType.toString();
            }
            List<Map<String,Object>> abTestRest = (List<Map<String, Object>>)context.getParams().get(AB_TEST_RESULT);
            if(CollectionUtils.isEmpty(abTestRest)){
                HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM)
                    .kv("SxlItemRecService context.getParams().get(AB_TEST_RESULT)",JSON.toJSONString(context.getParams()))
                    .info();
                return itemSetIdType.toString();
            }
            HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM)
                .kv("SxlItemRecService abTestRest",JSON.toJSONString(abTestRest))
                .info();
            abTestRest.forEach(variation ->{
                String smNewArrival = SxlSwitch.SM_NEW_ARRIVAL;
                String sxlAlgItemsetIdAb = SxlSwitch.SXL_ALG_ITEMSET_ID_AB;
                HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM)
                    .kv("SxlItemRecService","getAbData")
                    .kv("smNewArrival",smNewArrival)
                    .kv("sxlAlgItemsetIdAb",sxlAlgItemsetIdAb)
                    .info();
                if(smNewArrival.equals(variation.get("bizType")) &&
                    sxlAlgItemsetIdAb.equals(variation.get("tclsExpId"))){
                    if(variation.get("itemSetId") != null){
                        itemSetIdType.append(variation.get("itemSetId"));
                    }
                }
            });
        }catch (Exception e){
            HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM)
                .kv("SxlItemRecService getAbData",JSON.toJSONString(context.getParams()))
                .kv("e.getMessage()",JSON.toJSONString(e))
                .info();
        }
        HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM)
            .kv("SxlItemRecService itemSetIdType",itemSetIdType.toString())
            .info();
        return itemSetIdType.toString();
    }
/*    public void getChannelDate(){
        List<ChannelDataDO> channelDataDOS = dataTubeKeyList.stream().map(k -> {
            ChannelDataDO channelDataDO = new ChannelDataDO();
            channelDataDO.setChannelName("itemExtLdb");
            channelDataDO.setDataKey(k.getLeft());
            channelDataDO.setChannelField(k.getRight());
            return channelDataDO;
        }).collect(Collectors.toList());
        Map<String, String> extraMap = Maps.newHashMap();
        extraMap.put("extraMap","387450");
        List<String> ids = Lists.newArrayList();
        ids.add("645680838597");
        ids.add("646347368790");
        ids.add("645302542674");
        ids.add("645372039727");
        ids.add("645659627308");
        ids.add("644965793165");
        SingleResponse<Map<String, Map<String, Object>>> singleResponse = channelQueryService.query(channelDataDOS,ids,extraMap);
        HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM)
            .kv("singleResponse",JSON.toJSONString(singleResponse))
            .info();
    }*/
}
