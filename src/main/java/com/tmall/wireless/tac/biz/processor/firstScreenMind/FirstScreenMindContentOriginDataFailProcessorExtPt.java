package com.tmall.wireless.tac.biz.processor.firstScreenMind;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.taobao.tair.impl.mc.MultiClusterTairManager;
import com.tmall.aselfcommon.model.gcs.domain.GcsTairContentDTO;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.extensions.failprocessor.ContentFailProcessorRequest;
import com.tmall.txcs.gs.framework.extensions.failprocessor.ContentOriginDataFailProcessorExtPt;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.txcs.gs.model.biz.context.PageInfoDO;
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

    /**场景内容兜底缓存前缀**/
    private static final String pKey = "txcs_scene_collection_v1";
    private static final int labelSceneNamespace = 184;
    /**打底商品最大数量**/
    private static int needSize = 10;


    @Override
    public OriginDataDTO<ContentEntity> process(ContentFailProcessorRequest contentFailProcessorRequest) {
        Map<String, Object> requestParams = contentFailProcessorRequest.getSgFrameworkContextContent().getRequestParams();
        OriginDataDTO<ContentEntity> originDataDTO = contentFailProcessorRequest.getContentEntityOriginDataDTO();
        needSize = Optional.ofNullable(contentFailProcessorRequest.getSgFrameworkContextContent()).map(
            SgFrameworkContext::getUserPageInfo).map(
            PageInfoDO::getPageSize).orElse(needSize);
        boolean isSuccess = checkSuccess(originDataDTO);
        HadesLogUtil.stream(ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_CONTENT)
            .kv("FirstScreenMindContentOriginDataFailProcessorExtPt","process")
            .kv("isSuccess",String.valueOf(isSuccess))
            .info();
        if(isSuccess){
            return originDataDTO;
        }
        List<String> sKeyList = Lists.newArrayList();
        sKeyList = getContentSetIdList(requestParams);
        MultiClusterTairManager multiClusterTairManager = tairFactorySpi.getOriginDataFailProcessTair().getMultiClusterTairManager();
        Result<Map<Object, Result<DataEntry>>> labelSceneResult = multiClusterTairManager.prefixGets(labelSceneNamespace, pKey,sKeyList);
        if(labelSceneResult != null && labelSceneResult.getValue() !=null){
            Map<Object, Result<DataEntry>> resultMap = labelSceneResult.getValue();
            HadesLogUtil.stream(ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_CONTENT)
                .kv("FirstScreenMindContentOriginDataFailProcessorExtPt","process")
                .kv("resultMap.size()",String.valueOf(resultMap.size()))
                .info();
            if(MapUtils.isEmpty(resultMap)){
                return originDataDTO;
            };
            OriginDataDTO<ContentEntity> baseOriginDataDTO = buildOriginDataDTO(resultMap,needSize);
            return baseOriginDataDTO;
        }
        return originDataDTO;

    }
    public OriginDataDTO<ContentEntity> buildOriginDataDTO(Map<Object, Result<DataEntry>> resultMap,int needSize){
        OriginDataDTO<ContentEntity> originDataDTO = new OriginDataDTO<>();
        List<ContentEntity> contentEntities = Lists.newArrayList();
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


            gcsTairContentDTOList.forEach(gcsTairContentDTO -> {
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
                if(itemEntities.size() > needSize){
                    contentEntity.setItems(itemEntities.subList(0,needSize));
                }else{
                    contentEntity.setItems(itemEntities);
                }
                contentEntities.add(contentEntity);
            });
        }
        if(contentEntities.size() > needSize){
            originDataDTO.setResult(contentEntities.subList(0,needSize));
        }else{
            originDataDTO.setResult(contentEntities);
        }
        return originDataDTO;
    }

    private List<String> getContentSetIdList(Map<String, Object> requestParams) {

        List<String> result = Lists.newArrayList();
//        result.add(MapUtil.getStringWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_RANKING, ""));
//        result.add(MapUtil.getStringWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_RECIPE, ""));
//        result.add(MapUtil.getStringWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_BRAND, ""));
//        result.add(MapUtil.getStringWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_MIND, ""));
//        result.add(MapUtil.getStringWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_O2O, ""));
        result.add(MapUtil.getStringWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_B2C, ""));

        return result.stream().filter(contentSetId -> !("".equals(contentSetId) || "0".equals(contentSetId))).collect(Collectors.toList());
    }
    protected <T extends EntityDTO> boolean checkSuccess(OriginDataDTO<T> originDataDTO) {
        return originDataDTO != null && CollectionUtils.isNotEmpty(originDataDTO.getResult());
    }
}
