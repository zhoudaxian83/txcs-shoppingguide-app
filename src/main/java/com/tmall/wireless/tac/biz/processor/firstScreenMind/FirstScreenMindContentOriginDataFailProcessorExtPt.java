package com.tmall.wireless.tac.biz.processor.firstScreenMind;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.taobao.tair.impl.mc.MultiClusterTairManager;
import com.tmall.aselfcommon.model.gcs.domain.GcsTairContentDTO;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.extensions.failprocessor.ContentFailProcessorRequest;
import com.tmall.txcs.gs.framework.extensions.failprocessor.ContentOriginDataFailProcessorExtPt;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.model.model.dto.ContentEntity;
import com.tmall.txcs.gs.model.model.dto.EntityDTO;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.txcs.gs.spi.recommend.TairFactorySpi;
import com.tmall.wireless.tac.biz.processor.common.RequestKeyConstantApp;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author guijian
 */
@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_CONTENT)
@Service
public class FirstScreenMindContentOriginDataFailProcessorExtPt implements ContentOriginDataFailProcessorExtPt {
    Logger LOGGER = LoggerFactory.getLogger(FirstScreenMindContentOriginDataFailProcessorExtPt.class);
    @Resource
    TairFactorySpi tairFactorySpi;
    @Autowired
    TacLogger tacLogger;

    /*场景内容兜底缓存前缀*/
    private static final String pKey = "txcs_scene_collection_v1";
    private static final int labelSceneNamespace = 184;


    @Override
    public OriginDataDTO<ContentEntity> process(ContentFailProcessorRequest contentFailProcessorRequest) {
        LOGGER.info("FirstScreenMindContentOriginDataFailProcessorExtPt contentFailProcessorRequest.getContentEntityOriginDataDTO():" + JSON.toJSONString(contentFailProcessorRequest.getContentEntityOriginDataDTO()));
        tacLogger.info("FirstScreenMindContentOriginDataFailProcessorExtPt contentFailProcessorRequest.getContentEntityOriginDataDTO():" + JSON.toJSONString(contentFailProcessorRequest.getContentEntityOriginDataDTO()));
        Map<String, Object> requestParams = contentFailProcessorRequest.getSgFrameworkContextContent().getRequestParams();
        OriginDataDTO<ContentEntity> originDataDTO = contentFailProcessorRequest.getContentEntityOriginDataDTO();
        /*boolean isSuccess = checkSuccess(originDataDTO);
        if(isSuccess){
            return originDataDTO;
        }*/
        List<String> sKeyList = Lists.newArrayList();
        sKeyList = getContentSetIdList(requestParams);
        MultiClusterTairManager multiClusterTairManager = tairFactorySpi.getOriginDataFailProcessTair().getMultiClusterTairManager();
        Result<Map<Object, Result<DataEntry>>> labelSceneResult = multiClusterTairManager.prefixGets(labelSceneNamespace, pKey,sKeyList);
        if(labelSceneResult == null || !labelSceneResult.isSuccess()){
            LOGGER.error("FirstScreenMindContentOriginDataFailProcessorExtPt sKeyList:"+sKeyList+",labelSceneResult:"+ JSON.toJSONString(labelSceneResult));
            tacLogger.info("FirstScreenMindContentOriginDataFailProcessorExtPt sKeyList:"+sKeyList+",labelSceneResult:"+ JSON.toJSONString(labelSceneResult));
            return originDataDTO;
        }
        Map<Object, Result<DataEntry>> resultMap = labelSceneResult.getValue();
        if(MapUtils.isEmpty(resultMap)){
            return originDataDTO;
        }
        OriginDataDTO<ContentEntity> baseOriginDataDTO = buildOriginDataDTO(resultMap);
        LOGGER.info("FirstScreenMindContentOriginDataFailProcessorExtPt baseOriginDataDTO:"+baseOriginDataDTO);
        tacLogger.info("FirstScreenMindContentOriginDataFailProcessorExtPt baseOriginDataDTO:"+baseOriginDataDTO);
        return baseOriginDataDTO;
    }
    public OriginDataDTO<ContentEntity> buildOriginDataDTO(Map<Object, Result<DataEntry>> resultMap){
        OriginDataDTO<ContentEntity> originDataDTO = new OriginDataDTO<>();
        //内容集list-圈品集list-商品
        for(Object sKey : resultMap.keySet()) {
            Result<DataEntry> result = resultMap.get(sKey);
            if (!result.isSuccess()) {
                LOGGER.error("FirstScreenMindContentOriginDataFailProcessorExtPt sKey:"+sKey+",result:"+ JSON.toJSONString(result));
                continue;
            }
            DataEntry dataEntry = result.getValue();
            if(dataEntry == null || dataEntry.getValue() == null){
                LOGGER.error("FirstScreenMindContentOriginDataFailProcessorExtPt dataEntry:"+ JSON.toJSONString(dataEntry));
                continue;
            }
            List<GcsTairContentDTO> gcsTairContentDTOList = (List<GcsTairContentDTO>) dataEntry.getValue();
            if(CollectionUtils.isEmpty(gcsTairContentDTOList)){
                LOGGER.error("FirstScreenMindContentOriginDataFailProcessorExtPt gcsTairContentDTOList:"+ JSON.toJSONString(gcsTairContentDTOList));
                continue;
            }
            ContentEntity contentEntity = new ContentEntity();
            contentEntity.setContentId((Long)sKey);
            gcsTairContentDTOList.forEach(gcsTairContentDTO -> {
                List<Long> items = gcsTairContentDTO.getItems();
                items.forEach(item -> {
                    ItemEntity itemEntity = new ItemEntity();
                    itemEntity.setItemId(item);
                    contentEntity.getItems().add(itemEntity);
                });

            });
            originDataDTO.getResult().add(contentEntity);
        }
        return originDataDTO;
    }

    private List<String> getContentSetIdList(Map<String, Object> requestParams) {

        List<String> result = Lists.newArrayList();
        result.add(MapUtil.getStringWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_RANKING, ""));
        result.add(MapUtil.getStringWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_RECIPE, ""));
        result.add(MapUtil.getStringWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_BRAND, ""));
        result.add(MapUtil.getStringWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_MIND, ""));
        result.add(MapUtil.getStringWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_O2O, ""));
        result.add(MapUtil.getStringWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_B2C, ""));

        return result.stream().filter(contentSetId -> !("".equals(contentSetId) || "0".equals(contentSetId))).collect(Collectors.toList());
    }
    protected <T extends EntityDTO> boolean checkSuccess(OriginDataDTO<T> originDataDTO) {
        return originDataDTO != null && CollectionUtils.isNotEmpty(originDataDTO.getResult());
    }
}
