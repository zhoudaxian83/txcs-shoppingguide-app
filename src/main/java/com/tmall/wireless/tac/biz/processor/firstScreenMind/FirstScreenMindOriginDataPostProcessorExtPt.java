package com.tmall.wireless.tac.biz.processor.firstScreenMind;

import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.taobao.tair.json.Json;
import com.tmall.aselfcommon.model.scene.domain.TairSceneDTO;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataPostProcessorExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.model.biz.context.LocParams;
import com.tmall.txcs.gs.model.item.BizType;
import com.tmall.txcs.gs.model.item.ItemUniqueId;
import com.tmall.txcs.gs.model.item.O2oType;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.wireless.tac.biz.processor.common.RequestKeyConstantApp;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.common.ContentInfoSupport;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.RenderContentTypeEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author yangqing.byq
 * @date 2021/5/21
 */
@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_ITEM)
@Service
public class FirstScreenMindOriginDataPostProcessorExtPt implements OriginDataPostProcessorExtPt {

    @Autowired
    ContentInfoSupport contentInfoSupport;
    @Override
    public OriginDataDTO<ItemEntity> process(SgFrameworkContextItem contextItem) {
        OriginDataDTO<ItemEntity> originDataDTO = Optional.of(contextItem).map(SgFrameworkContextItem::getItemEntityOriginDataDTO).orElse(null);
        Map<String, Object> requestParam = contextItem.getRequestParams();
        /**榜单不需要所见及所得，无需特殊处理**/
        if(isBangdan(contextItem)){
            return originDataDTO;
        }
        List<ItemEntity> itemEntityList = getItemEntityList(contextItem);

        if(CollectionUtils.isEmpty(itemEntityList)){
            return originDataDTO;
        }

        if (originDataDTO == null || originDataDTO.getResult() == null) {
            originDataDTO = new OriginDataDTO<>();
            originDataDTO.setHasMore(false);
            originDataDTO.setIndex(itemEntityList.size());
            originDataDTO.setResult(Lists.newArrayList());
            contextItem.setItemEntityOriginDataDTO(originDataDTO);
        }
        List<ItemEntity> finalItemEntities = Lists.newArrayList();
        Set<String> itemUniqueKeySet = Sets.newHashSet();
        Long contentId = MapUtil.getLongWithDefault(requestParam,"moduleId",0L);
        Long itemSetIds = MapUtil.getLongWithDefault(requestParam,"itemSetIds",0L);
        /**视频场景才打标**/
        List<Long> topItemIds = Lists.newArrayList();
        if(isMedia(contextItem) && contentId > 0L){
            topItemIds = getTopItemIds(contentId,itemSetIds);
        }
        /**首页所见即所得置顶且去重，非首页去重**/
        for(ItemEntity itemEntity:itemEntityList){
            ItemUniqueId itemUniqueId = itemEntity.getItemUniqueId();
            if (itemUniqueKeySet.contains(itemUniqueId.toString())) {
                continue;
            }
            itemUniqueKeySet.add(itemEntity.getItemUniqueId().toString());
            if(isFirstPage(contextItem)){
                if(isMedia(contextItem) && CollectionUtils.isNotEmpty(topItemIds) && topItemIds.contains(itemEntity.getItemId())){
                    itemEntity.setTop(true);
                }
                finalItemEntities.add(itemEntity);
            }
        }

        originDataDTO.getResult().forEach(itemEntity -> {
            ItemUniqueId itemUniqueId = itemEntity.getItemUniqueId();
            if (itemUniqueKeySet.contains(itemUniqueId.toString())) {
                return;
            }
            itemUniqueKeySet.add(itemEntity.getItemUniqueId().toString());
            finalItemEntities.add(itemEntity);

        });
        originDataDTO.setResult(finalItemEntities);
        contextItem.setItemEntityOriginDataDTO(originDataDTO);
        return originDataDTO;

    }
    /**
     * 获取tair内容下置顶商品
     * @param contentId
     * @return
     */
    private List<Long> getTopItemIds(Long contentId,Long itemSetIds){
        List<Long> contentIds = Lists.newArrayList();
        if(contentId == null || contentId <= 0 ){
            return null;
        }
        contentIds.add(contentId);
        Map<Long, TairSceneDTO> tairResult = contentInfoSupport.getTairData(contentIds);
        if(MapUtils.isEmpty(tairResult)){
            return null;
        }
        TairSceneDTO tairSceneDTO = tairResult.get(contentId);
        Map<Long,List<Long>> topItemIdMap = contentInfoSupport.getTopItemIds(tairSceneDTO);
        HadesLogUtil.stream(ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_ITEM)
            .kv("FirstScreenMindOriginDataPostProcessorExtPt","getTopItemIds")
            .kv("topItemIdMap", JSON.toJSONString(topItemIdMap))
            .info();
        List<Long> itemIds = Lists.newArrayList();
        if(itemSetIds > 0L){
            itemIds = topItemIdMap.get(itemSetIds);
        }else{
            /**视频不存在货架，只有一个圈品集**/
            topItemIdMap.values().stream().findFirst().get();
        }
        return itemIds;
    }

    private boolean isBangdan(SgFrameworkContextItem sgFrameworkContextItem) {
        String contentType = MapUtil.getStringWithDefault(sgFrameworkContextItem.getRequestParams(), RequestKeyConstantApp.CONTENT_TYPE, RenderContentTypeEnum.b2cNormalContent.getType());
        return RenderContentTypeEnum.bangdanContent.getType().equals(contentType) || RenderContentTypeEnum.bangdanO2OContent.getType().equals(contentType);
    }
    private boolean isMedia(SgFrameworkContextItem sgFrameworkContextItem) {
        String contentType = MapUtil.getStringWithDefault(sgFrameworkContextItem.getRequestParams(), RequestKeyConstantApp.CONTENT_TYPE, RenderContentTypeEnum.b2cNormalContent.getType());
        return RenderContentTypeEnum.mediaContent.getType().equals(contentType);
    }

    /**
     * 获取所见所得商品
     * @param contextItem
     * @return
     */
    private List<ItemEntity> getItemEntityList(SgFrameworkContextItem contextItem){
        List<Long> itemIdList = getItemIdList(contextItem);

        if (CollectionUtils.isEmpty(itemIdList)) {
            return null;
        }

        boolean isO2oScene = isO2oScene(contextItem);

        String businessType;
        String bizType;
        String o2oType;

        if (isO2oScene) {
            businessType = O2oType.O2O.name();
            bizType = BizType.SM.getCode();
            if (isOneHour(contextItem)) {
                o2oType = O2oType.O2OOneHour.name();
            } else {
                o2oType = O2oType.O2OHalfDay.name();
            }
        } else {
            businessType = O2oType.B2C.name();
            bizType = BizType.SM.getCode();
            o2oType = O2oType.B2C.name();
        }
        List<ItemEntity> itemEntityList = itemIdList.stream().map(itemId -> {
            ItemEntity itemEntity = new ItemEntity();
            itemEntity.setItemId(itemId);
            itemEntity.setO2oType(o2oType);
            itemEntity.setBusinessType(businessType);
            itemEntity.setBizType(bizType);
            return itemEntity;
        }).collect(Collectors.toList());

        return itemEntityList;
    }

    private boolean isFirstPage(SgFrameworkContextItem contextItem) {
        Long index = MapUtil.getLongWithDefault(contextItem.getRequestParams(), "pageStartPosition", 0L);
        return index <= 0L;
    }

    private List<Long> getItemIdList(SgFrameworkContextItem contextItem) {

        String entryItemIds = Optional.of(contextItem).map(SgFrameworkContext::getRequestParams).map(map -> map.get("entryItemIds")).map(Object::toString).orElse("");
        if (StringUtils.isEmpty(entryItemIds)) {
            return Lists.newArrayList();
        }
        List<String> stringList = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(entryItemIds);
        return stringList.stream().filter(StringUtils::isNumeric).map(Long::valueOf).collect(Collectors.toList());
    }

    private boolean isOneHour(SgFrameworkContextItem contextItem) {
        Long oneHourStore = Optional.of(contextItem).map(SgFrameworkContext::getLocParams).map(LocParams::getRt1HourStoreId).orElse(0L);

        return oneHourStore > 0;
    }

    private boolean isO2oScene(SgFrameworkContextItem contextItem) {
        String contentType = MapUtil.getStringWithDefault(contextItem.getRequestParams(), RequestKeyConstantApp.CONTENT_TYPE, RenderContentTypeEnum.b2cNormalContent.getType());

        return RenderContentTypeEnum.checkO2OContentType(contentType);
    }
}
