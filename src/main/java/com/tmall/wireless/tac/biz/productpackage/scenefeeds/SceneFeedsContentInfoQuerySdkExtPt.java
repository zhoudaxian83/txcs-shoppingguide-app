package com.tmall.wireless.tac.biz.productpackage.scenefeeds;


import com.ali.com.google.common.collect.Lists;
import com.ali.com.google.common.collect.Maps;
import com.alibaba.common.logging.Logger;
import com.alibaba.common.logging.LoggerFactory;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.tmall.aselfcommon.model.gcs.enums.GcsMarketChannel;
import com.tmall.aselfcommon.model.lbs.constants.LocationConstants;
import com.tmall.aselfcommon.model.scene.domain.TairSceneDTO;
import com.tmall.aselfcommon.model.scene.enums.SceneType;
import com.tmall.aselfcommon.model.scene.valueobject.SceneDetailValue;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkPackage;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.contentinfo.ContentInfoQuerySdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.Response;
import com.tmall.tcls.gs.sdk.framework.model.context.*;
import com.tmall.txcs.gs.spi.recommend.TairFactorySpi;
import com.tmall.wireless.tac.biz.processor.common.PackageNameKey;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.FrontBackMapEnum;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.RenderContentTypeEnum;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.RenderErrorEnum;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.model.content.SubContentModel;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.utils.RenderCheckUtil;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.utils.RenderLangUtil;
import io.reactivex.Flowable;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

//@Service
@SdkPackage(packageName = PackageNameKey.CONTENT_FEEDS)
public class SceneFeedsContentInfoQuerySdkExtPt extends Register implements ContentInfoQuerySdkExtPt {
//    Logger LOGGER = LoggerFactory.getLogger(SceneFeedsContentInfoQuerySdkExtPt.class);

    @Autowired
    TairFactorySpi tairFactorySpi;
    private static final int labelSceneNamespace = 184;


    @Override
    public Flowable<Response<Map<Long, ContentInfoDTO>>> process(SgFrameworkContextContent sgFrameworkContextContent) {
        /*????????????????????????*/
        String pKey = "txcs_scene_detail_v2";
        Map<Long, ContentInfoDTO> contentDTOMap = Maps.newHashMap();
        try {
            List<ContentEntity> contentEntities  = Optional.of(sgFrameworkContextContent).map(SgFrameworkContextContent::getContentEntityOriginDataDTO).map(OriginDataDTO::getResult).orElse(Lists.newArrayList());
            List<String> sKeyList = new ArrayList<>();
            for (ContentEntity contentEntity : contentEntities) {
                sKeyList.add(pKey + "_" + contentEntity.getContentId());
            }
            HadesLogUtil.stream(ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_CONTENT)
                    .kv("userId",Optional.of(sgFrameworkContextContent).map(SgFrameworkContextContent::getCommonUserParams)
                            .map(CommonUserParams::getUserDO).map(UserDO::getUserId).map(
                            Objects::toString).orElse("0"))
                    .kv("FirstScreenMindContentInfoQueryExtPt","process")
                    .kv("sKeyList", JSON.toJSONString(sKeyList))
                    .info();
            Result<List<DataEntry>> mgetResult = tairFactorySpi.getOriginDataFailProcessTair().getMultiClusterTairManager().mget(labelSceneNamespace, sKeyList);
            if(mgetResult != null && mgetResult.getValue() != null){
                HadesLogUtil.stream(ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_CONTENT)
                        .kv("userId",Optional.of(sgFrameworkContextContent).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getUserDO).map(UserDO::getUserId).map(
                                Objects::toString).orElse("0"))
                        .kv("FirstScreenMindContentInfoQueryExtPt","process")
                        .kv("mgetResult.getValue().size()",JSON.toJSONString(mgetResult.getValue().size()))
                        .info();
            }
            if (mgetResult == null || CollectionUtils.isEmpty(mgetResult.getValue())) {
                return Flowable.just(Response.fail("READ_CONTENT_FROM_TAIR_RETURN_EMPTY"));
            }
            List<DataEntry> dataEntryList = mgetResult.getValue();
            Map<Long, TairSceneDTO> tairResult = Maps.newHashMap();
            //????????????????????????
            dataEntryList.forEach(dataEntry -> {
                Object tairKey = dataEntry.getKey();
                String tairKeyStr = String.valueOf(tairKey);
                String[] s = tairKeyStr.split("_");
                String contentId = s[s.length - 1];
                TairSceneDTO value = (TairSceneDTO) dataEntry.getValue();
                tairResult.put(Long.valueOf(contentId), value);
            });
            for(ContentEntity contentEntity : contentEntities){
                Long contentId = contentEntity.getContentId();
                TairSceneDTO tairSceneDTO = tairResult.get(contentId);
                /**???????????????????????????????????????????????????????????????????????????????????????????????????*/
                if(!tairResult.containsKey(contentId) || tairSceneDTO == null){
                    continue;
                }
                ContentInfoDTO contentDTO = new ContentInfoDTO();
                Map<String, Object> contentInfo = Maps.newHashMap();
                contentInfo.put("contentId",tairSceneDTO.getId());
                contentInfo.put("contentTitle",tairSceneDTO.getTitle());
                contentInfo.put("contentSubtitle",tairSceneDTO.getSubtitle());
                contentInfo.put("itemSetIds", getItemSetIds(tairSceneDTO));
                contentInfo.put("scm", contentEntity.getTrack_point());
                Map<String, Object> tairPropertyMap = tairSceneDTO.getProperty();
                //???????????????  ??????????????????????????????????????????
                for(FrontBackMapEnum frontBackMapEnum : FrontBackMapEnum.values()){
                    contentInfo.put(frontBackMapEnum.getFront(),tairPropertyMap.get(frontBackMapEnum.getBack()));
                }
                /**????????????*/
                String type = SceneType.of(tairSceneDTO.getType()).name();
                String marketChannel = GcsMarketChannel.of(tairSceneDTO.getMarketChannel()).name();
                /**????????????????????????????????????????????????????????????*/
                if(RenderCheckUtil.StringEmpty(type) || RenderCheckUtil.StringEmpty(marketChannel)){
                    contentInfo.put("contentType", RenderContentTypeEnum.getBottomContentType());
                }
                //b2c????????????
                if(marketChannel.equals(GcsMarketChannel.B2C.name()) && type.equals(SceneType.NORMAL.name())){
                    contentInfo.put("contentType",RenderContentTypeEnum.b2cNormalContent.getType());
                    //b2c????????????
                }else if(marketChannel.equals(GcsMarketChannel.B2C.name()) && type.equals(SceneType.COMBINE.name())){
                    contentInfo.put("contentType",RenderContentTypeEnum.b2cCombineContent.getType());
                    contentInfo.put("subContentModelList",buildSubContentBaseInfoV2(contentInfo,tairSceneDTO));
                    //b2c????????????
                }else if(marketChannel.equals(GcsMarketChannel.B2C.name()) && type.equals(SceneType.BRAND.name())){
                    contentInfo.put("contentType",RenderContentTypeEnum.b2cBrandContent.getType());
                    //o2o????????????
                }else if(marketChannel.equals(GcsMarketChannel.O2O.name()) && type.equals(SceneType.NORMAL.name())){
                    contentInfo.put("contentType",RenderContentTypeEnum.o2oNormalContent.getType());
                    //o2o????????????
                }else if(marketChannel.equals(GcsMarketChannel.O2O.name()) && type.equals(SceneType.COMBINE.name())){
                    contentInfo.put("contentType",RenderContentTypeEnum.o2oCombineContent.getType());
                    contentInfo.put("subContentModelList",buildSubContentBaseInfoV2(contentInfo,tairSceneDTO));
                    //o2o????????????
                }else if(marketChannel.equals(GcsMarketChannel.O2O.name()) && type.equals(SceneType.BRAND.name())){
                    contentInfo.put("contentType",RenderContentTypeEnum.o2oBrandContent.getType());
                } else if (type.equals(SceneType.RECIPE.name())) {
                    contentInfo.put("contentType",RenderContentTypeEnum.recipeContent.getType());
                } else if (type.equals(SceneType.MEDIA.name())) {
                    contentInfo.put("contentType",RenderContentTypeEnum.mediaContent.getType());
                } else if (marketChannel.equals(GcsMarketChannel.O2O.name()) && type.equals(SceneType.BOARD.name())) {
                    contentInfo.put("contentType",RenderContentTypeEnum.bangdanO2OContent.getType());
                } else if (type.equals(SceneType.BOARD.name())) {
                    contentInfo.put("contentType",RenderContentTypeEnum.bangdanContent.getType());
                } else {
                    //????????????-????????????
                    contentInfo.put("contentType",RenderContentTypeEnum.getBottomContentType());
                }

                contentDTO.setContentInfo(contentInfo);
                contentDTOMap.put(contentId,contentDTO);
            }
            /*HadesLogUtil.stream(ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_CONTENT)
                    .kv("userId",Optional.of(sgFrameworkContextContent).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getUserDO).map(UserDO::getUserId).map(
                            Objects::toString).orElse("0"))
                    .kv("FirstScreenMindContentInfoQueryExtPt","process")
                    .kv("contentDTOMap",JSON.toJSONString(contentDTOMap))
                    .info();*/
        }catch (Exception e){
            LOGGER.info(RenderErrorEnum.contentBatchTairExc.getCode() + "" + RenderErrorEnum.contentBatchTairExc.getMessage());
            return Flowable.just(Response.fail(RenderErrorEnum.contentBatchTairExc.getCode()));
        }
        return Flowable.just(Response.success(contentDTOMap));
    }


    private static String getItemSetIds(TairSceneDTO labelSceneContentInfo) {

        List<SceneDetailValue> sceneDetailValues = Optional.ofNullable(labelSceneContentInfo)
                .map(TairSceneDTO::getDetails)
                .orElse(com.google.common.collect.Lists.newArrayList());

        if (org.apache.commons.collections.CollectionUtils.isEmpty(sceneDetailValues)) {
            return "";
        }

        List<Long> itemSetIds = sceneDetailValues.stream().map(SceneDetailValue::getItemsetId).collect(Collectors.toList());
        return Joiner.on(",").join(itemSetIds);

    }
    private static List<SubContentModel> buildSubContentBaseInfoV2(Map<String, Object> contentInfo, TairSceneDTO labelSceneContentInfo){

        List<SceneDetailValue> details = labelSceneContentInfo.getDetails();

        List<SubContentModel> subContentModelList = new ArrayList<>();
        for (SceneDetailValue detail : details) {

            SubContentModel sub = new SubContentModel();
            sub.setSubContentId(detail.getDetailId());
            sub.setSubContentTitle(detail.getTitle());
            if(contentInfo.get("subContentType") != null && StringUtils.isNotEmpty(String.valueOf(contentInfo.get("subContentType")))){
                sub.setSubContentType(String.valueOf(contentInfo.get("subContentType")));
            }
            sub.setItemSetIds(RenderLangUtil.safeString(detail.getItemsetId()));
            subContentModelList.add(sub);
        }
        return subContentModelList;
    }

}
