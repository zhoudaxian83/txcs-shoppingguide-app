package com.tmall.wireless.tac.biz.processor.huichang.inventory.InventoryEntranceModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.fastjson.JSON;

import com.ali.com.google.common.collect.Maps;
import com.google.common.collect.Lists;
import com.tmall.aselfcaptain.cloudrec.api.EntityRenderService;
import com.tmall.aselfcaptain.cloudrec.domain.Entity;
import com.tmall.aselfcaptain.cloudrec.domain.EntityId;
import com.tmall.aselfcaptain.cloudrec.domain.EntityQueryOption;
import com.tmall.aselfcaptain.item.model.ChannelDataDO;
import com.tmall.aselfcaptain.util.StackTraceUtil;
import com.tmall.aselfcommon.model.scene.domain.TairSceneDTO;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.contentinfo.ContentInfoQuerySdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.Response;
import com.tmall.tcls.gs.sdk.framework.model.context.ContentEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.ContentInfoDTO;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextContent;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.dataservice.TacOptLogger;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 查询场景信息扩展点
 *
 * @author wangguohui
 */
@SdkExtension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
    useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
    scenario = HallScenarioConstant.HALL_SCENARIO_SCENARIO_INVENTORY_ENTRANCE_MODULE)
public class InventoryEntranceModuleContentInfoQuerySdkExtPt extends Register implements ContentInfoQuerySdkExtPt {

    Logger logger = LoggerFactory.getLogger(InventoryEntranceModuleContentInfoQuerySdkExtPt.class);

    @Autowired
    TacLogger tacLogger;

    @Autowired
    private TacOptLogger tacOptLogger;

    private static final String ACTIVITY_SCENE_PREFIX = "tcls_ugc_scene_v1_";
    public static final String CHANNELNAME = "sceneLdb";

    @Resource
    EntityRenderService entityRenderService;

    @Override
    public Flowable<Response<Map<Long, ContentInfoDTO>>> process(SgFrameworkContextContent sgFrameworkContextContent) {
        logger.info("InventoryEntranceModuleContentInfoQuerySdkExtPt.start.sgFrameworkContextContent:{}", JSON.toJSONString(sgFrameworkContextContent));
        Map<Long, ContentInfoDTO> contentDTOMap = Maps.newHashMap();
        try {
            Map<String, Object> userParams = sgFrameworkContextContent.getUserParams();
            Object dealStaticData = userParams.get("dealStaticDataList");
            List<Map<String, String>> dealStaticDataList = new ArrayList<>();
            if (dealStaticDataList != null) {
                dealStaticDataList = (List<Map<String, String>>)dealStaticData;
            }
            Map<String, Map<String, String>> staticDataMap = new HashMap<>();
            for (Map<String, String> data : dealStaticDataList) {
                String contentSetId = data.get("contentSetId");
                staticDataMap.put(contentSetId, data);
            }
            List<ContentEntity> contentEntities = Optional.of(sgFrameworkContextContent).map(
                SgFrameworkContextContent::getContentEntityOriginDataDTO).map(
                OriginDataDTO::getResult).orElse(com.ali.com.google.common.collect.Lists.newArrayList());

            Map<String, List<ContentEntity>> contentEntitiesMap = contentEntities.stream().collect(Collectors.groupingBy(ContentEntity::getContentSetId));
            contentEntitiesMap.forEach((k, v) ->{
                if(v.size() > 1){
                    //todo 需要打印日志
                    HadesLogUtil.stream("InventoryEntranceModule")
                        .kv("InventoryEntranceModuleContentInfoQuerySdkExtPt","process")
                        .kv("scene size", String.valueOf(v.size()))
                        .kv("sceneSetId", k)
                        .error();
                }
            });

            List<String> sceneIdList = contentEntities.stream().map(ContentEntity::getContentId).map(String::valueOf)
                .collect(
                    Collectors.toList());
            List<TairSceneDTO> onlyScenesFromCaptainResult = getOnlyScenesFromCaptain(sceneIdList);
            if(CollectionUtils.isEmpty(onlyScenesFromCaptainResult)){
                HadesLogUtil.stream("InventoryEntranceModule")
                    .kv("InventoryEntranceModuleContentInfoQuerySdkExtPt", "process")
                    .kv("查询场景缓存为空", "onlyScenesFromCaptainResult is empty")
                    .kv("sceneIdList", JSON.toJSONString(sceneIdList))
                    .error();
            }
            //TODO 雾列 给的场景和查询出来的场景不一致的，需要打印个日志

            Map<String, TairSceneDTO> tairSceneDTOMap = onlyScenesFromCaptainResult.stream().collect(
                Collectors.toMap(TairSceneDTO::getId, a -> a, (k1, k2) -> k1));

            for (ContentEntity contentEntity : contentEntities) {
                Long contentId = contentEntity.getContentId();
                TairSceneDTO tairSceneDTO = tairSceneDTOMap.get(String.valueOf(contentId));
                if(tairSceneDTO == null){
                    continue;
                }
                ContentInfoDTO contentInfoDTO = new ContentInfoDTO();
                Map<String, Object> contentInfo = Maps.newHashMap();
                contentInfo.put("title", tairSceneDTO.getTitle());
                contentInfo.put("subtitle", tairSceneDTO.getSubtitle());
                contentInfo.put("sceneId", tairSceneDTO.getId());
                contentInfo.put("contentId", tairSceneDTO.getId());

                //补全场景集的信息
                String contentSetId = contentEntity.getContentSetId();
                String[] content = contentSetId.split("_");
                String contentSetIdStr = content[1];
                contentInfo.put("contentSetId", contentSetIdStr);
                Map<String, String> staticData = staticDataMap.get(contentSetIdStr);
                String contentSetTitle = staticData.get("contentSetTitle");
                contentInfo.put("contentSetTitle", contentSetTitle);
                String contentSetSubTitle = staticData.get("contentSetSubTitle");
                contentInfo.put("contentSetSubTitle", contentSetSubTitle);

                contentInfoDTO.setContentInfo(contentInfo);
                contentDTOMap.put(contentId, contentInfoDTO);
            }
            return Flowable.just(Response.success(contentDTOMap));
        } catch (Exception e) {
            logger.error("InventoryEntranceModuleContentInfoQuerySdkExtPt.error:{}", StackTraceUtil.stackTrace(e));
            return Flowable.just(Response.fail("query scene info error"));
        }
    }

    private List<TairSceneDTO> getOnlyScenesFromCaptain(List<String> sceneIdList) throws Exception {
        List<TairSceneDTO> scenesFromCaptain = Lists.newArrayList();
        List<EntityId> ids = new ArrayList<>();
        sceneIdList.forEach(e -> {
            EntityId entityId = EntityId.of(ACTIVITY_SCENE_PREFIX + e, "content");
            ids.add(entityId);
        });
        EntityQueryOption entityQueryOption = new EntityQueryOption();
        entityQueryOption.setSmAreaId(310100L);

        List<ChannelDataDO> channelDataDOList = new ArrayList<>();
        ChannelDataDO channelDataDO = new ChannelDataDO();
        channelDataDO.setDataKey("data");
        channelDataDO.setChannelField("data");
        channelDataDO.setChannelName(CHANNELNAME);
        channelDataDOList.add(channelDataDO);
        entityQueryOption.setChannelDataDOS(channelDataDOList);
        try {
            MultiResponse<Entity> render = entityRenderService.render(ids, entityQueryOption);
            if (render.isSuccess()) {
                scenesFromCaptain = render.getData().stream()
                    .map(
                        entity -> JSON.parseObject(JSON.toJSONString(entity.get("data")), TairSceneDTO.class)
                    ).collect(Collectors.toList());
            } else {
                throw new Exception("查询不成功");
            }
        } catch (Exception e) {
            throw new Exception("渲染场景数据异常", e);
        }
        return scenesFromCaptain;
    }

}
