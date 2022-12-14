package com.tmall.wireless.tac.biz.processor.firstScreenMind;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.taobao.tair.json.Json;
import com.tmall.aselfcommon.model.gcs.enums.GcsMarketChannel;
import com.tmall.aselfcommon.model.scene.domain.TairSceneDTO;
import com.tmall.aselfcommon.model.scene.enums.SceneType;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.txcs.gs.framework.extensions.origindata.ContentOriginDataPostProcessorExtPt;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextContent;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.txcs.gs.model.item.ItemUniqueId;
import com.tmall.txcs.gs.model.model.dto.ContentEntity;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.common.ContentInfoSupport;
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
public class FirstScreenMindContentOriginDataPostProcessorExtPt implements ContentOriginDataPostProcessorExtPt {
    Logger LOGGER = LoggerFactory.getLogger(FirstScreenMindContentOriginDataPostProcessorExtPt.class);

    @Autowired
    ContentInfoSupport contentInfoSupport;
    @Autowired
    TacLogger tacLogger;

    @Override
    public OriginDataDTO<ContentEntity> process(SgFrameworkContextContent sgFrameworkContextContent) {

        List<ContentEntity> contentEntities  = Optional.of(sgFrameworkContextContent).map(SgFrameworkContextContent::getContentEntityOriginDataDTO).map(OriginDataDTO::getResult).orElse(
            Lists.newArrayList());

        if(CollectionUtils.isEmpty(contentEntities)){
            return sgFrameworkContextContent.getContentEntityOriginDataDTO();
        }
        List<Long> contentIds = contentEntities.stream().map(ContentEntity::getContentId).collect(Collectors.toList());
        Map<Long, TairSceneDTO> tairResult = contentInfoSupport.getTairData(contentIds);
        if(MapUtils.isEmpty(tairResult)){
            return sgFrameworkContextContent.getContentEntityOriginDataDTO();
        }
        for(ContentEntity contentEntity : contentEntities){
            Long contentId = contentEntity.getContentId();
            TairSceneDTO tairSceneDTO = tairResult.get(contentId);
            /**???????????????????????????????????????????????????????????????????????????????????????????????????*/
            if(!tairResult.containsKey(contentId) || tairSceneDTO == null){
                tacLogger.info("??????????????????????????????????????????contentId:" + contentId +",tairResult:"+tairResult);
                continue;
            }
            String type = SceneType.of(tairSceneDTO.getType()).name();
            /**???????????????????????????????????????**/
            if(!type.equals(SceneType.MEDIA.name())){
                continue;
            }
            /**itemSetId,list**/
            Map<Long,List<Long>> topItemIdMap = contentInfoSupport.getTopItemIds(tairSceneDTO);
            HadesLogUtil.stream(ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_CONTENT)
                .kv("userId",Optional.of(sgFrameworkContextContent).map(SgFrameworkContext::getUserDO).map(UserDO::getUserId).map(
                    Objects::toString).orElse("0"))
                .kv("contentId", JSON.toJSONString(contentId))
                .kv("topItemIdMap",JSON.toJSONString(topItemIdMap))
                .info();
            /**?????????????????????????????????????????????**/
            List<Long> itemIds = topItemIdMap.values().stream().findFirst().get();
            if(CollectionUtils.isEmpty(itemIds)){
                continue;
            }
            String marketChannel = GcsMarketChannel.of(tairSceneDTO.getMarketChannel()).name();
            List<ItemEntity> topItemEntitys = contentInfoSupport.buildTopItemEntityList(itemIds,marketChannel);
            List<ItemEntity> finalItemEntitys = Lists.newArrayList();
            Set<String> itemUniqueKeySet = Sets.newHashSet();
            topItemEntitys.forEach(itemEntity -> {
                ItemUniqueId itemUniqueId = itemEntity.getItemUniqueId();
                if (itemUniqueKeySet.contains(itemUniqueId.toString())) {
                    return;
                }
                itemUniqueKeySet.add(itemEntity.getItemUniqueId().toString());
                finalItemEntitys.add(itemEntity);
            });
            contentEntity.getItems().forEach(itemEntity -> {
                ItemUniqueId itemUniqueId = itemEntity.getItemUniqueId();
                if (itemUniqueKeySet.contains(itemUniqueId.toString())) {
                    return;
                }
                itemUniqueKeySet.add(itemEntity.getItemUniqueId().toString());
                finalItemEntitys.add(itemEntity);
            });
            contentEntity.setItems(finalItemEntitys);
        }
        sgFrameworkContextContent.getContentEntityOriginDataDTO().setResult(contentEntities);
        return sgFrameworkContextContent.getContentEntityOriginDataDTO();
    }
}
