package com.tmall.wireless.tac.biz.processor.guessWhatYouLikeMenu.ext;

import com.ali.com.google.common.collect.Maps;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.taobao.tair.impl.mc.MultiClusterTairManager;

import com.tmall.aselfcommon.model.gcs.domain.GcsTairContentDTO;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataFailProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataProcessRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.*;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.spi.recommend.TairFactorySpi;
import com.tmall.wireless.tac.biz.processor.common.PackageNameKey;
import com.tmall.wireless.tac.biz.processor.common.RequestKeyConstantApp;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.guessWhatYouLikeMenu.constant.ConstantValue;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.Context;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Yushan
 * @date 2021/9/17 4:32 下午
 */
@SdkExtension(
        bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.CNXH_MENU_FEEDS
)
public class GulMenuContentOriginDataFailProcessorSdkExtPt extends Register implements ContentOriginDataFailProcessorSdkExtPt {

    @Resource
    TairFactorySpi tairFactorySpi;

    @Resource
    TacLogger tacLogger;

    /**
     * 场景内容兜底缓存前缀
     **/
    private static final String pKey = "txcs_scene_collection_v1";

    private static final int labelSceneNamespace = 184;

    /**
     * 打底内容最大数量
     **/
    private static int needSize = 8;

    /**
     * 打底商品最大数量
     **/
    private static int needSizeItems = 20;

    @Override
    public OriginDataDTO<ContentEntity> process(ContentOriginDataProcessRequest contentOriginDataProcessRequest) {

        return contentOriginDataProcessRequest.getContentEntityOriginDataDTO();
    }
}

//        Map<String, Object> requestParams = Optional.ofNullable(contentOriginDataProcessRequest)
//                .map(ContentOriginDataProcessRequest::getSgFrameworkContextContent)
//                .map(SgFrameworkContextContent::getTacContext)
//                .map(Context::getParams)
//                .orElse(Maps.newHashMap());
//
//        OriginDataDTO<ContentEntity> contentEntityOriginDataDTO = Optional.ofNullable(contentOriginDataProcessRequest)
//                .map(ContentOriginDataProcessRequest::getContentEntityOriginDataDTO)
//                .orElse(new OriginDataDTO<>());
//
//        needSize = Optional.ofNullable(contentOriginDataProcessRequest)
//                .map(ContentOriginDataProcessRequest::getSgFrameworkContextContent)
//                .map(SgFrameworkContext::getCommonUserParams)
//                .map(CommonUserParams::getUserPageInfo)
//                .map(PageInfoDO::getPageSize)
//                .orElse(needSize);
//
//        boolean isSuccess = checkSuccess(contentEntityOriginDataDTO);
//
//        HadesLogUtil.stream(ScenarioConstantApp.CNXH_MENU_FEEDS)
//                .kv("SceneFeedsContentOriginDataFailProcessorSDKExtPt", "process")
//                .kv("isSuccess", String.valueOf(isSuccess))
//                .info();
//        if (isSuccess) {
//            return contentEntityOriginDataDTO;
//        }
//
//        List<String> sKeyList = Lists.newArrayList();
//        sKeyList = getContentSetIdList(requestParams);
//        HadesLogUtil.stream(ScenarioConstantApp.CNXH_MENU_FEEDS)
//                .kv("SceneFeedsContentOriginDataFailProcessorSDKExtPt", "process")
//                .kv("labelSceneNamespace", String.valueOf(labelSceneNamespace))
//                .kv("pKey", pKey)
//                .kv("sKeyList", JSON.toJSONString(sKeyList))
//                .info();
//        MultiClusterTairManager multiClusterTairManager = tairFactorySpi.getOriginDataFailProcessTair().getMultiClusterTairManager();
//        Result<Map<Object, Result<DataEntry>>> labelSceneResult = multiClusterTairManager.prefixGets(labelSceneNamespace, pKey, sKeyList);
//        if (labelSceneResult != null && labelSceneResult.getValue() != null) {
//            Map<Object, Result<DataEntry>> resultMap = labelSceneResult.getValue();
//            if (MapUtils.isEmpty(resultMap)) {
//                return contentEntityOriginDataDTO;
//            }
//            OriginDataDTO<ContentEntity> baseOriginDataDTO = buildOriginDataDTO(resultMap, needSize, contentOriginDataProcessRequest);
//            return baseOriginDataDTO;
//        }
//        return contentEntityOriginDataDTO;
//    }
//
//    public OriginDataDTO<ContentEntity> buildOriginDataDTO(Map<Object, Result<DataEntry>> resultMap, int needSize, ContentOriginDataProcessRequest contentOriginDataProcessRequest) {
//
//        Map<String, Object> requestParams = Optional.ofNullable(contentOriginDataProcessRequest)
//                .map(ContentOriginDataProcessRequest::getSgFrameworkContextContent)
//                .map(SgFrameworkContextContent::getTacContext)
//                .map(Context::getParams)
//                .orElse(Maps.newHashMap());
//        Long oneHourStoreId = Optional.ofNullable(contentOriginDataProcessRequest)
//                .map(ContentOriginDataProcessRequest::getSgFrameworkContextContent)
//                .map(SgFrameworkContext::getCommonUserParams)
//                .map(CommonUserParams::getLocParams)
//                .map(LocParams::getRt1HourStoreId)
//                .orElse(0L);
//        Long halfDayStoreId = Optional.ofNullable(contentOriginDataProcessRequest)
//                .map(ContentOriginDataProcessRequest::getSgFrameworkContextContent)
//                .map(SgFrameworkContext::getCommonUserParams)
//                .map(CommonUserParams::getLocParams)
//                .map(LocParams::getRtHalfDayStoreId)
//                .orElse(0L);
//        String source = MapUtils.getString(requestParams, "source", ConstantValue.SOURCE_CHANNEL_GUL_RECIPE);
//
//        OriginDataDTO<ContentEntity> originDataDTO = new OriginDataDTO<>();
//        List<ContentEntity> contentEntities = Lists.newArrayList();
//        //内容集list-内容id-商品
//        for (Object sKey : resultMap.keySet()) {
//            Result<DataEntry> result = resultMap.get(sKey);
//            if (!result.isSuccess()) {
//                LOGGER.error("SceneFeedsContentOriginDataFailProcessorSDKExtPt sKey:" + sKey + ",result:" + JSON.toJSONString(result));
//                continue;
//            }
//            DataEntry dataEntry = result.getValue();
//            if (dataEntry == null || dataEntry.getValue() == null) {
//                LOGGER.error("SceneFeedsContentOriginDataFailProcessorSDKExtPt dataEntry:" + JSON.toJSONString(dataEntry));
//                continue;
//            }
//            List<GcsTairContentDTO> gcsTairContentDTOList = (List<GcsTairContentDTO>) dataEntry.getValue();
//            if (CollectionUtils.isEmpty(gcsTairContentDTOList)) {
//                LOGGER.error("SceneFeedsContentOriginDataFailProcessorSDKExtPt gcsTairContentDTOList:" + JSON.toJSONString(gcsTairContentDTOList));
//                continue;
//            }
//            List<GcsTairContentDTO> finalList = Lists.newArrayList();
//            if (gcsTairContentDTOList.size() > needSize) {
//                finalList.addAll(gcsTairContentDTOList.subList(0, needSize));
//            } else {
//                finalList.addAll(gcsTairContentDTOList);
//            }
//            finalList.forEach(gcsTairContentDTO -> {
//                ContentEntity contentEntity = new ContentEntity();
//                contentEntity.setContentId(Long.valueOf(gcsTairContentDTO.getSceneId()));
//                List<Long> items = gcsTairContentDTO.getItems();
//                List<ItemEntity> itemEntities = Lists.newArrayList();
//                items.forEach(item -> {
//                    ItemEntity itemEntity = new ItemEntity();
//                    itemEntity.setItemId(item);
//                    itemEntity.setBizType("sm");
//                    if (source.equals(ConstantValue.SOURCE_CHANNEL_GUL_RECIPE)) {
//                        if (oneHourStoreId > 0L) {
//                            itemEntity.setO2oType("O2OOneHour");
//                        } else if (halfDayStoreId > 0L) {
//                            itemEntity.setO2oType("O2OHalfDay");
//                        }
//                    } else if (source.equals(ConstantValue.SOURCE_CHANNEL_MMC_HALF_DAY)) {
//                        itemEntity.setO2oType("O2OHalfDay");
//                    }
//                    itemEntities.add(itemEntity);
//                });
//                if (itemEntities.size() > needSizeItems) {
//                    contentEntity.setItems(itemEntities.subList(0, needSizeItems));
//                } else {
//                    contentEntity.setItems(itemEntities);
//                }
//                HadesLogUtil.stream(ScenarioConstantApp.CNXH_MENU_FEEDS)
//                        .kv("SceneFeedsContentOriginDataFailProcessorSDKExtPt", "buildOriginDataDTO")
//                        .kv("sKey", String.valueOf(sKey))
//                        .kv("contentId", gcsTairContentDTO.getSceneId())
//                        .kv("contentEntity.getItems().size()", String.valueOf(contentEntity.getItems().size()))
//                        .info();
//                contentEntities.add(contentEntity);
//            });
//        }
//        originDataDTO.setResult(contentEntities);
//        return originDataDTO;
//    }
//
//    private List<String> getContentSetIdList(Map<String, Object> requestParams) {
//
//        List<String> result = Lists.newArrayList();
//        String contentSetIdRecipe = MapUtil.getStringWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_RECIPE, "");
//        if (StringUtils.isNotEmpty(contentSetIdRecipe)) {
//            result.add(contentSetIdRecipe);
//        }
//        return result.stream().filter(contentSetId -> !("".equals(contentSetId) || "0".equals(contentSetId))).collect(Collectors.toList());
//    }
//
//    protected <T extends EntityDTO> boolean checkSuccess(OriginDataDTO<T> originDataDTO) {
//        return originDataDTO != null && CollectionUtils.isNotEmpty(originDataDTO.getResult());
//    }
//}
