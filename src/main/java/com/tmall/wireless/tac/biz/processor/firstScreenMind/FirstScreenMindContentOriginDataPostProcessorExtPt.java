package com.tmall.wireless.tac.biz.processor.firstScreenMind;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import com.alibaba.cola.extension.Extension;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.tmall.aselfcommon.model.gcs.enums.GcsMarketChannel;
import com.tmall.aselfcommon.model.scene.domain.TairSceneDTO;
import com.tmall.aselfcommon.model.scene.enums.SceneType;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.extensions.origindata.ContentOriginDataPostProcessorExtPt;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextContent;
import com.tmall.txcs.gs.model.item.ItemUniqueId;
import com.tmall.txcs.gs.model.model.dto.ContentEntity;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.wireless.tac.biz.processor.common.RequestKeyConstantApp;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.common.ContentInfoSupport;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.RenderContentTypeEnum;
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
            LOGGER.info("FirstScreenMindContentOriginDataPostProcessorExtPt contentId:"+contentId);
            tacLogger.info("FirstScreenMindContentOriginDataPostProcessorExtPt contentId:"+contentId);
            TairSceneDTO tairSceneDTO = tairResult.get(contentId);
            /**如果内容后台返回的补全内容为空，那么把这个内容过滤掉，并且日志记录*/
            if(!tairResult.containsKey(contentId) || tairSceneDTO == null){
                tacLogger.info("批量补全内容中心信息返回为空contentId:" + contentId +",tairResult:"+tairResult);
                continue;
            }
            String type = SceneType.of(tairSceneDTO.getType()).name();
            /**非视频内容类型，则不做处理**/
            if(type.equals(SceneType.MEDIA.name())){
                continue;
            }
            /**itemSetId,list**/
            Map<Long,List<Long>> topItemIdMap = contentInfoSupport.getTopItemIds(tairSceneDTO);
            LOGGER.info("FirstScreenMindContentOriginDataPostProcessorExtPt topItemIdMap:"+topItemIdMap);
            tacLogger.info("FirstScreenMindContentOriginDataPostProcessorExtPt topItemIdMap:"+topItemIdMap);
            /**视频不存在货架，只有一个圈品集**/
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
            LOGGER.info("FirstScreenMindContentOriginDataPostProcessorExtPt contentEntity:"+contentEntity);
            tacLogger.info("FirstScreenMindContentOriginDataPostProcessorExtPt contentEntity:"+contentEntity);
        }
        LOGGER.info("FirstScreenMindContentOriginDataPostProcessorExtPt contentEntities:"+contentEntities);
        tacLogger.info("FirstScreenMindContentOriginDataPostProcessorExtPt contentEntities:"+contentEntities);
        sgFrameworkContextContent.getContentEntityOriginDataDTO().setResult(contentEntities);
        return sgFrameworkContextContent.getContentEntityOriginDataDTO();
    }
    private boolean isMedia(SgFrameworkContextContent sgFrameworkContextContent) {
        String contentType = MapUtil.getStringWithDefault(sgFrameworkContextContent.getRequestParams(), RequestKeyConstantApp.CONTENT_TYPE, RenderContentTypeEnum.b2cNormalContent.getType());
        return RenderContentTypeEnum.mediaContent.getType().equals(contentType);
    }
}
