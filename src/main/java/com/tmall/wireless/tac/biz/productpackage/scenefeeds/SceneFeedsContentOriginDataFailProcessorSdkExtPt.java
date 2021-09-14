package com.tmall.wireless.tac.biz.productpackage.scenefeeds;


import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.taobao.tair.impl.mc.MultiClusterTairManager;
import com.tmall.aselfcommon.model.gcs.domain.GcsTairContentDTO;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkPackage;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataFailProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataProcessRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.ContentEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContext;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.spi.recommend.TairFactorySpi;
import com.tmall.wireless.tac.biz.processor.common.PackageNameKey;
import com.tmall.wireless.tac.biz.processor.common.RequestKeyConstantApp;
import com.tmall.wireless.tac.biz.processor.huichang.inventory.InventoryEntranceModule.InventoryEntranceModuleContentFilterSdkExtPt;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.List;
import java.util.Map;
import java.util.Optional;

@SdkPackage(packageName = PackageNameKey.CONTENT_FEEDS)
public class SceneFeedsContentOriginDataFailProcessorSdkExtPt extends Register implements ContentOriginDataFailProcessorSdkExtPt {

    private static final String pKey = "txcs_scene_collection_v1";
    private static final int labelSceneNamespace = 184;
    Logger LOGGER = LoggerFactory.getLogger(SceneFeedsContentOriginDataFailProcessorSdkExtPt.class);
    /**打底商品最大数量**/
    private static int needSizeItems = 20;
    @Autowired
    TairFactorySpi tairFactorySpi;
    @Override
    public OriginDataDTO<ContentEntity> process(ContentOriginDataProcessRequest contentOriginDataProcessRequest) {

        Map<String, Object> requestMap = Optional.of(contentOriginDataProcessRequest).map(ContentOriginDataProcessRequest::getSgFrameworkContextContent).map(SgFrameworkContext::getRequestParams).orElse(Maps.newHashMap());
        List<String> contentSetIdList = getContentSetIdList(requestMap);
        LOGGER.info("SceneFeedsContentOriginDataFailProcessorSdkExtPt,contentSetId:{}", JSON.toJSONString(contentSetIdList));
        if (CollectionUtils.isEmpty(contentSetIdList)) {
            return contentOriginDataProcessRequest.getContentEntityOriginDataDTO();
        }
        MultiClusterTairManager multiClusterTairManager = tairFactorySpi.getOriginDataFailProcessTair().getMultiClusterTairManager();
        Result<Map<Object, Result<DataEntry>>> labelSceneResult = multiClusterTairManager.prefixGets(labelSceneNamespace, pKey, contentSetIdList);
        Map<Object, Result<DataEntry>> resultMap = labelSceneResult.getValue();
        if(MapUtils.isEmpty(resultMap)){
            LOGGER.error("SceneFeedsContentOriginDataFailProcessorSdkExtPt,tairReturnError:{},{}", pKey, JSON.toJSONString(contentSetIdList));
            return contentOriginDataProcessRequest.getContentEntityOriginDataDTO();
        };
        OriginDataDTO<ContentEntity> contentEntityOriginDataDTO = buildOriginDataDTO(resultMap, 8);

        LOGGER.error("SceneFeedsContentOriginDataFailProcessorSdkExtPt,result:{}", JSON.toJSONString(contentEntityOriginDataDTO));

        return contentEntityOriginDataDTO;
    }

    public OriginDataDTO<ContentEntity> buildOriginDataDTO(Map<Object, Result<DataEntry>> resultMap,int needSize){
        OriginDataDTO<ContentEntity> originDataDTO = new OriginDataDTO<>();
        List<ContentEntity> contentEntities = Lists.newArrayList();
        //内容集list-内容id-商品
        for(Object sKey : resultMap.keySet()) {
            Result<DataEntry> result = resultMap.get(sKey);
            if (!result.isSuccess()) {
                LOGGER.error("SceneFeedsContentOriginDataFailProcessorSdkExtPt sKey:"+sKey+",result:"+ JSON.toJSONString(result));
                continue;
            }
            DataEntry dataEntry = result.getValue();
            if(dataEntry == null || dataEntry.getValue() == null){
                LOGGER.error("SceneFeedsContentOriginDataFailProcessorSdkExtPt dataEntry:"+ JSON.toJSONString(dataEntry));
                continue;
            }
            List<GcsTairContentDTO> gcsTairContentDTOList = (List<GcsTairContentDTO>) dataEntry.getValue();
            if(CollectionUtils.isEmpty(gcsTairContentDTOList)){
                LOGGER.error("SceneFeedsContentOriginDataFailProcessorSdkExtPt gcsTairContentDTOList:"+ JSON.toJSONString(gcsTairContentDTOList));
                continue;
            }
            List<GcsTairContentDTO> finalList = Lists.newArrayList();
            if(gcsTairContentDTOList.size() > needSize){
                finalList.addAll(gcsTairContentDTOList.subList(0,needSize));
            }else{
                finalList.addAll(gcsTairContentDTOList);
            }
            finalList.forEach(gcsTairContentDTO -> {
                ContentEntity contentEntity = new ContentEntity();
                contentEntity.setContentId(Long.valueOf(gcsTairContentDTO.getSceneId()));
                List<Long> items = gcsTairContentDTO.getItems();
                List<ItemEntity> itemEntities = Lists.newArrayList();
                items.forEach(item -> {
                    ItemEntity itemEntity = new ItemEntity();
                    itemEntity.setItemId(item);
                    itemEntity.setBizType("sm");
                    itemEntity.setO2oType(gcsTairContentDTO.getMarketChannel());
                    /*itemEntity.setBusinessType(gcsTairContentDTO.getMarketChannel());*/
                    itemEntities.add(itemEntity);
                });
                if(itemEntities.size() > needSizeItems){
                    contentEntity.setItems(itemEntities.subList(0,needSizeItems));
                }else{
                    contentEntity.setItems(itemEntities);
                }
                contentEntities.add(contentEntity);
            });
        }
        originDataDTO.setResult(contentEntities);
        return originDataDTO;
    }




    protected List<String> getContentSetIdList(Map<String, Object> requestParams) {

        if (MapUtils.isEmpty(requestParams)) {
            return Lists.newArrayList();
        }

        // 优先用B2C品牌场景打底
        String contentSetIdB2c = MapUtil.getStringWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_B2C, "");
        if (StringUtils.isNotEmpty(contentSetIdB2c)) {
            return Lists.newArrayList(contentSetIdB2c);
        }

        // 榜单打底
        String rankingContentSet = MapUtil.getStringWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_RANKING, "");
        if (StringUtils.isNotEmpty(rankingContentSet)) {
            return Lists.newArrayList(rankingContentSet);
        }

        // 品牌内容集打底
        String brandContentSetId = MapUtil.getStringWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_BRAND, "");
        if (StringUtils.isNotEmpty(brandContentSetId)) {
            return Lists.newArrayList(brandContentSetId);
        }

        // 视频内容
        String mediaContentSet = MapUtil.getStringWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_MIND, "");
        if (StringUtils.isNotEmpty(mediaContentSet)) {
            return Lists.newArrayList(mediaContentSet);
        }

        // 心智场景打底
        String mindContentSet = MapUtil.getStringWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_MIND, "");
        if (StringUtils.isNotEmpty(mindContentSet)) {
            return Lists.newArrayList(mindContentSet);
        }
        // o2o场景打底
        String o2oContentSetId = MapUtil.getStringWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_O2O, "");
        if (StringUtils.isNotEmpty(o2oContentSetId)) {
            return Lists.newArrayList(o2oContentSetId);
        }

        // 菜谱打底
        String recipeContentSetId = MapUtil.getStringWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_RECIPE, "");
        if (StringUtils.isNotEmpty(recipeContentSetId)) {
            return Lists.newArrayList(recipeContentSetId);
        }

        return Lists.newArrayList();
    }
}
