package com.tmall.wireless.tac.biz.processor.newproduct.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.fastjson.JSON;

import com.ali.com.google.common.collect.Maps;
import com.google.common.collect.Lists;
import com.tmall.aselfcaptain.item.model.ChannelDataDO;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.txcs.biz.supermarket.scene.UserParamsKeyConstant;
import com.tmall.txcs.biz.supermarket.scene.util.CsaUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.model.EntityVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.txcs.gs.framework.model.meta.DataTubeKey;
import com.tmall.txcs.gs.framework.model.meta.DataTubeMateInfo;
import com.tmall.txcs.gs.framework.model.meta.ItemGroupMetaInfo;
import com.tmall.txcs.gs.framework.model.meta.ItemInfoSourceMetaInfo;
import com.tmall.txcs.gs.framework.model.meta.ItemMetaInfo;
import com.tmall.txcs.gs.framework.model.meta.ItemRecommendMetaInfo;
import com.tmall.txcs.gs.framework.service.impl.SgFrameworkServiceItem;
import com.tmall.txcs.gs.model.biz.context.EntitySetParams;
import com.tmall.txcs.gs.model.biz.context.PageInfoDO;
import com.tmall.txcs.gs.model.biz.context.SceneInfo;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.txcs.gs.spi.recommend.ChannelQuerySpi;
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
    ChannelQuerySpi channelQuerySpi;

    /**
     * ab??????????????????
     **/
    private final static String AB_TEST_RESULT = "abTestVariationsResult";
    /**
     * ????????????
     **/
    private final static String ARTIFICIAL = "Artificial";
    /**
     * ????????????-????????????
     **/
    private final static String ARTIFICIAL_ALGORITHM = "Artificial-Algorithm";

    static List<Pair<String, String>> dataTubeKeyList = Lists.newArrayList(
        Pair.of("recommendWords", "recommendWords"),
        Pair.of("videoUrl", "videoUrl"),
        Pair.of("type", "type"),
        Pair.of("atmosphereImageUrl", "atmosphereImageUrl"),
        Pair.of("sellingPointDesc", "sellingPointDesc"),
        Pair.of("newItemAttribute", "newItemAttribute")
    );

    public Flowable<TacResult<SgFrameworkResponse<EntityVO>>> recommend(Context context) {

        Long smAreaId = MapUtil.getLongWithDefault(context.getParams(), "smAreaId", 330100L);
        /**?????????????????????id??????**/
        Long itemSetIdSw = Long.valueOf(SxlSwitch.SXL_ITEMSET_ID);
        /**???????????????id??????**/
        Long itemSetIdAlgSw = Long.valueOf(SxlSwitch.SXL_ALG_ITEMSET_ID);
        /**????????????????????????id**/
        Long itemSetId = MapUtil.getLongWithDefault(context.getParams(), RequestKeyConstantApp.ITEMSET_ID, 0L);
        /**???????????????id-??????tair key**/
        String activityId = MapUtil.getStringWithDefault(context.getParams(), RequestKeyConstantApp.SXL_MAIN_ACTIVITY_ID, String.valueOf(itemSetIdSw));
        HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM)
            .kv("SxlItemRecService itemSetIdSw", JSON.toJSONString(itemSetIdSw))
            .kv("SxlItemRecService itemSetIdAlgSw", JSON.toJSONString(itemSetIdAlgSw))
            .kv("SxlItemRecService itemSetId", JSON.toJSONString(itemSetId))
            .kv("SxlItemRecService activityId", activityId)
            .info();

        String topItemIds = MapUtil.getStringWithDefault(context.getParams(), "itemIds", "");

        SgFrameworkContextItem sgFrameworkContextItem = new SgFrameworkContextItem();
        EntitySetParams entitySetParams = new EntitySetParams();
        entitySetParams.setItemSetSource("crm");
        String abTestType = "";
        String hasTrialMoudle = "";
        /**???????????????????????????**/
        if (!StringUtils.isBlank(activityId) && itemSetId > 0) {
            entitySetParams.setItemSetIdList(Lists.newArrayList(itemSetId));
        } else {
            /**??????????????????ab??????**/
            String itemSetIdType = getAbData(context);
            hasTrialMoudle = getSxlTrialMergeAbData(context);
            if (!StringUtils.isBlank(itemSetIdType)) {
                if ("old".equals(itemSetIdType)) {
                    entitySetParams.setItemSetIdList(Lists.newArrayList(itemSetIdSw));
                    abTestType = ARTIFICIAL;
                } else if ("new".equals(itemSetIdType)) {
                    List<Long> itemSetIds = Lists.newArrayList();
                    itemSetIds.add(itemSetIdSw);
                    itemSetIds.add(itemSetIdAlgSw);
                    entitySetParams.setItemSetIdList(itemSetIds);
                    abTestType = ARTIFICIAL_ALGORITHM;
                } else {
                    entitySetParams.setItemSetIdList(Lists.newArrayList(itemSetIdSw));
                    abTestType = ARTIFICIAL;
                }
            } else {
                /**?????????????????????????????????ab?????????????????????????????????????????????**/
                abTestType = ARTIFICIAL;
                activityId = String.valueOf(itemSetIdSw);
                entitySetParams.setItemSetIdList(Lists.newArrayList(itemSetIdSw));
            }
            HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM)
                .kv("SxlItemRecService", "recommend")
                .kv("abTestType", abTestType)
                .kv("itemSetIdType", itemSetIdType)
                .kv("hasTrialMoudle", hasTrialMoudle)
                .info();
        }
        sgFrameworkContextItem.setItemMetaInfo(getItemMetaInfo(activityId));
        sgFrameworkContextItem.setRequestParams(context.getParams());
        sgFrameworkContextItem.setEntitySetParams(entitySetParams);
        HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM)
            .kv("activityId", activityId)
            .kv("SxlItemRecService entitySetParams.getItemSetIdList()", JSON.toJSONString(entitySetParams.getItemSetIdList()))
            .info();

        sgFrameworkContextItem.setSceneInfo(getSceneInfo(context));
        sgFrameworkContextItem.setUserDO(getUserDO(context));

        sgFrameworkContextItem.setLocParams(CsaUtil
            .parseCsaObj(context.get(UserParamsKeyConstant.USER_PARAMS_KEY_CSA), smAreaId));
        sgFrameworkContextItem.setUserPageInfo(getPageInfoDO(context));

        if (StringUtils.isNotBlank(topItemIds)) {
            sgFrameworkContextItem.getUserParams().put(Constant.SXL_TOP_ITEM_IDS, topItemIds);
        }

        String finalAbTestType = abTestType;
        Boolean finalHasTrialMoudle = Boolean.TRUE.equals(Boolean.parseBoolean(hasTrialMoudle)) ? Boolean.TRUE : Boolean.FALSE;
        return sgFrameworkServiceItem.recommend(sgFrameworkContextItem)
            .map(response -> {
                if (StringUtils.isNotBlank(finalAbTestType)) {
                    response.getExtInfos().put("abTestType", finalAbTestType);
                }
                response.getExtInfos().put("hasTrialMoudle", finalHasTrialMoudle);
                HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM)
                    .kv("SxlItemRecService finalAbTestType", finalAbTestType)
                    .info();
                /**????????????????????????????????????**/
                if (StringUtils.isNotBlank(finalAbTestType) && ARTIFICIAL_ALGORITHM.equals(finalAbTestType)
                    && itemSetIdAlgSw != null) {
                    getChannelDate(String.valueOf(itemSetIdAlgSw), response);
                }
                return response;
            })
            .map(TacResult::newResult)
            .map(tacResult -> {
                if (tacResult == null) {
                    HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM)
                        .kv("SxlItemRecService", "recommend")
                        .kv("tacResult", "tacResult is null!")
                        .info();
                    tacResult.getBackupMetaData().setUseBackup(true);
                    return tacResult;
                }
                if (tacResult.getData() == null || tacResult.getData().getItemAndContentList() == null || tacResult.getData().getItemAndContentList().isEmpty()) {
                    tacResult = TacResult.errorResult("tac Backup!");
                    HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM)
                        .kv("SxlItemRecService", "recommend")
                        .kv("tacResult", JSON.toJSONString(tacResult))
                        .info();
                }
                tacResult.getBackupMetaData().setUseBackup(true);
                return tacResult;
            })
            .onErrorReturn(r -> TacResult.errorResult(""));

    }

    public SgFrameworkResponse<EntityVO> getChannelDate(String activityId, SgFrameworkResponse<EntityVO> response) {
        if (response == null || response.getItemAndContentList() == null || CollectionUtils.isEmpty(response.getItemAndContentList())) {
            return response;
        }
        List<String> itemIds = Lists.newArrayList();
        response.getItemAndContentList().forEach(entityVO -> {
            Object itemId = entityVO.get("itemId");
            if (itemId != null) {
                itemIds.add(itemId.toString());
            }
        });
        List<ChannelDataDO> channelDataDOS = dataTubeKeyList.stream().map(k -> {
            ChannelDataDO channelDataDO = new ChannelDataDO();
            channelDataDO.setChannelName("itemExtLdb");
            channelDataDO.setDataKey(k.getLeft());
            channelDataDO.setChannelField(k.getRight());
            return channelDataDO;
        }).collect(Collectors.toList());
        Map<String, String> extraMap = Maps.newHashMap();
        extraMap.put("activityId", activityId);
        HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM)
            .kv("channelDataDOS", JSON.toJSONString(channelDataDOS))
            .kv("itemIds", JSON.toJSONString(itemIds))
            .kv("extraMap", JSON.toJSONString(extraMap))
            .info();
        SingleResponse<Map<String, Map<String, Object>>> singleResponse = channelQuerySpi.query(channelDataDOS, itemIds, extraMap);

        if (singleResponse == null || !singleResponse.isSuccess() || singleResponse.getData() == null || singleResponse.getData().isEmpty()) {
            HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM)
                .kv("itemIds", JSON.toJSONString(itemIds))
                .kv("extraMap", JSON.toJSONString(extraMap))
                .kv("singleResponse", "isEmpty")
                .info();
            return response;
        }
        HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM)
            .kv("itemIds", JSON.toJSONString(itemIds))
            .kv("singleResponse", JSON.toJSONString(singleResponse))
            .info();
        Map<String, Map<String, Object>> channelMap = singleResponse.getData();
        response.getItemAndContentList().forEach(entityVO -> {
            Object itemId = entityVO.get("itemId");
            Map<String, Object> itemChannelData = channelMap.get(itemId.toString());
            if (itemId == null || itemChannelData == null || itemChannelData.isEmpty()) {
                return;
            }
            Object sellingPointDesc = itemChannelData.get("sellingPointDesc");
            Object recommendWords = itemChannelData.get("recommendWords");
            Object type = itemChannelData.get("type");
            if (sellingPointDesc != null && StringUtils.isNotBlank(sellingPointDesc.toString())) {
                entityVO.put("sellingPointDesc", sellingPointDesc);
            }
            if (recommendWords != null && StringUtils.isNotBlank(recommendWords.toString())) {
                entityVO.put("recommendWords", recommendWords);
            }
            if (type != null && StringUtils.isNotBlank(type.toString())) {
                entityVO.put("type", type);
            }
        });
        return response;
    }

    public PageInfoDO getPageInfoDO(Context context) {
        PageInfoDO pageInfoDO = new PageInfoDO();
        String index = MapUtil.getStringWithDefault(context.getParams(), RequestKeyConstantApp.INDEX, "0");
        String pageSize = MapUtil.getStringWithDefault(context.getParams(), RequestKeyConstantApp.PAGE_SIZE, "20");
        pageInfoDO.setIndex(Integer.valueOf(index));
        pageInfoDO.setPageSize(Integer.valueOf(pageSize));
        return pageInfoDO;
    }

    public SceneInfo getSceneInfo(Context context) {
        SceneInfo sceneInfo = new SceneInfo();
        sceneInfo.setBiz(ScenarioConstantApp.BIZ_TYPE_SUPERMARKET);
        sceneInfo.setSubBiz(ScenarioConstantApp.LOC_TYPE_B2C);
        sceneInfo.setScene(ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM);
        return sceneInfo;
    }

    public UserDO getUserDO(Context context) {
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
        return userDO;
    }

    private static ItemMetaInfo getItemMetaInfo(String activityId) {
        ItemMetaInfo itemMetaInfo = new ItemMetaInfo();
        List<ItemGroupMetaInfo> itemGroupMetaInfoList = Lists.newArrayList();
        List<ItemInfoSourceMetaInfo> itemInfoSourceMetaInfoList = Lists.newArrayList();
        ItemGroupMetaInfo itemGroupMetaInfo1 = new ItemGroupMetaInfo();
        itemGroupMetaInfoList.add(itemGroupMetaInfo1);
        itemGroupMetaInfo1.setGroupName("sm_B2C");
        itemGroupMetaInfo1.setItemInfoSourceMetaInfos(itemInfoSourceMetaInfoList);
        ItemInfoSourceMetaInfo itemInfoSourceMetaInfoCaptain = new ItemInfoSourceMetaInfo();
        itemInfoSourceMetaInfoCaptain.setSourceName("captain");
        itemInfoSourceMetaInfoCaptain.setSceneCode("shoppingguide.newLauch.common");
        itemInfoSourceMetaInfoCaptain.setDataTubeMateInfo(buildDataTubeMateInfo(activityId));
        itemInfoSourceMetaInfoList.add(itemInfoSourceMetaInfoCaptain);
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
     * ??????ab??????
     *
     * @param context
     * @return
     */
    private String getAbData(Context context) {
        StringBuilder itemSetIdType = new StringBuilder();
        try {
            if (context.getParams().get(AB_TEST_RESULT) == null
                || StringUtils.isBlank(context.getParams().get(AB_TEST_RESULT).toString())) {
                HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM)
                    .kv("SxlItemRecService context.getParams()", JSON.toJSONString(context.getParams()))
                    .info();
                return itemSetIdType.toString();
            }
            List<Map<String, Object>> abTestRest = (List<Map<String, Object>>)context.getParams().get(AB_TEST_RESULT);
            if (CollectionUtils.isEmpty(abTestRest)) {
                HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM)
                    .kv("SxlItemRecService context.getParams().get(AB_TEST_RESULT)", JSON.toJSONString(context.getParams()))
                    .info();
                return itemSetIdType.toString();
            }
            HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM)
                .kv("SxlItemRecService abTestRest", JSON.toJSONString(abTestRest))
                .info();
            abTestRest.forEach(variation -> {
                String smNewArrival = SxlSwitch.SM_NEW_ARRIVAL;
                String sxlAlgItemsetIdAb = SxlSwitch.SXL_ALG_ITEMSET_ID_AB;
                HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM)
                    .kv("SxlItemRecService", "getAbData")
                    .kv("smNewArrival", smNewArrival)
                    .kv("sxlAlgItemsetIdAb", sxlAlgItemsetIdAb)
                    .info();
                if (smNewArrival.equals(variation.get("bizType")) &&
                    sxlAlgItemsetIdAb.equals(variation.get("tclsExpId"))) {
                    if (variation.get("itemSetId") != null) {
                        itemSetIdType.append(variation.get("itemSetId"));
                    }
                }
            });
        } catch (Exception e) {
            HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM)
                .kv("SxlItemRecService getAbData", JSON.toJSONString(context.getParams()))
                .kv("e.getMessage()", JSON.toJSONString(e))
                .info();
        }
        HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM)
            .kv("SxlItemRecService itemSetIdType", itemSetIdType.toString())
            .info();
        return itemSetIdType.toString();
    }

    /**
     * ????????????????????????ab??????
     *
     * @param context
     * @return
     */
    private String getSxlTrialMergeAbData(Context context) {
        StringBuilder hasTrialMoudle = new StringBuilder();
        try {
            if (context.getParams().get(AB_TEST_RESULT) == null
                || StringUtils.isBlank(context.getParams().get(AB_TEST_RESULT).toString())) {
                HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM)
                    .kv("SxlItemRecService context.getParams()", JSON.toJSONString(context.getParams()))
                    .info();
                return hasTrialMoudle.toString();
            }
            List<Map<String, Object>> abTestRest = (List<Map<String, Object>>)context.getParams().get(AB_TEST_RESULT);
            if (CollectionUtils.isEmpty(abTestRest)) {
                HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM)
                    .kv("SxlItemRecService context.getParams().get(AB_TEST_RESULT)", JSON.toJSONString(context.getParams()))
                    .info();
                return hasTrialMoudle.toString();
            }
            HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM)
                .kv("SxlItemRecService abTestRest", JSON.toJSONString(abTestRest))
                .info();
            abTestRest.forEach(variation -> {
                String smNewArrival = SxlSwitch.SM_NEW_ARRIVAL;
                String sxlTrialMergeIdAb = SxlSwitch.SXL_TRIAL_MERGE_ID_AB;
                HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM)
                    .kv("SxlItemRecService", "getSxlTrialMergeAbData")
                    .kv("smNewArrival", smNewArrival)
                    .kv("sxlAlgItemsetIdAb", sxlTrialMergeIdAb)
                    .info();
                if (smNewArrival.equals(variation.get("bizType")) &&
                    sxlTrialMergeIdAb.equals(variation.get("tclsExpId"))) {
                    if (variation.get("hasTrialMoudle") != null) {
                        hasTrialMoudle.append(variation.get("hasTrialMoudle"));
                    }
                }
            });
        } catch (Exception e) {
            HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM)
                .kv("SxlItemRecService getAbData", JSON.toJSONString(context.getParams()))
                .kv("e.getMessage()", JSON.toJSONString(e))
                .info();
        }
        HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM)
            .kv("SxlItemRecService itemSetIdType", hasTrialMoudle.toString())
            .info();
        return hasTrialMoudle.toString();
    }
}
