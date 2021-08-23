package com.tmall.wireless.tac.biz.processor.huichang.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.alibaba.aladdin.lamp.sdk.solution.context.SolutionContext;
import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.fastjson.JSON;

import com.ali.com.google.common.collect.Maps;
import com.google.common.collect.Lists;
import com.tmall.aselfcaptain.cloudrec.api.EntityRenderService;
import com.tmall.aselfcaptain.cloudrec.domain.Entity;
import com.tmall.aselfcaptain.cloudrec.domain.EntityId;
import com.tmall.aselfcaptain.cloudrec.domain.EntityQueryOption;
import com.tmall.aselfcaptain.item.model.ChannelDataDO;
import com.tmall.aselfcommon.model.scene.domain.TairSceneDTO;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.contentinfo.ContentInfoQuerySdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.Response;
import com.tmall.tcls.gs.sdk.framework.model.context.ContentEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.ContentInfoDTO;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextContent;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.RenderErrorEnum;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;

/**
 * 查询场景信息扩展点
 *
 * @author wangguohui
 */
@SdkExtension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
    useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
    scenario = HallScenarioConstant.HALL_SCENARIO_SCENARIO_INVENTORY_ENTRANCE_MODULE)
public class InventoryEntranceModuleContentInfoQuerySdkExtPt extends Register implements ContentInfoQuerySdkExtPt {

    private static final String ACTIVITY_SCENE_PREFIX = "tcls_ugc_scene_v1_";
    public static final String CHANNELNAME = "sceneLdb";

    @Resource
    EntityRenderService entityRenderService;

    @Override
    public Flowable<Response<Map<Long, ContentInfoDTO>>> process(SgFrameworkContextContent sgFrameworkContextContent) {
        Map<Long, ContentInfoDTO> contentDTOMap = Maps.newHashMap();
        try{
            Map<String, Object> userParams = sgFrameworkContextContent.getUserParams();
            Object dealStaticData = userParams.get("dealStaticDataList");
            List<Map<String, String>> dealStaticDataList = new ArrayList<>();
            if(dealStaticDataList != null){
                dealStaticDataList = (List<Map<String, String>>)dealStaticData;
            }
            Map<String, Map<String, String>> staticDataMap = new HashMap<>();
            for(Map<String, String> data : dealStaticDataList){
                String contentSetId = data.get("contentSetId");
                staticDataMap.put(contentSetId, data);
            }
            List<ContentEntity> contentEntities  = Optional.of(sgFrameworkContextContent).map(SgFrameworkContextContent::getContentEntityOriginDataDTO).map(
                OriginDataDTO::getResult).orElse(com.ali.com.google.common.collect.Lists.newArrayList());
            List<String> sceneIdList = contentEntities.stream().map(ContentEntity::getContentId).map(String::valueOf).collect(
                Collectors.toList());
            List<SceneDTO> onlyScenesFromCaptainResult = getOnlyScenesFromCaptain(sceneIdList);

            Map<String, SceneDTO> sceneDTOMap = onlyScenesFromCaptainResult.stream().collect(
                Collectors.toMap(SceneDTO::getId, a -> a, (k1, k2) -> k1));
            if(CollectionUtils.isNotEmpty(onlyScenesFromCaptainResult)){
                for(ContentEntity contentEntity : contentEntities){
                    Long contentId = contentEntity.getContentId();
                    SceneDTO sceneDTO = sceneDTOMap.get(String.valueOf(contentId));
                    ContentInfoDTO contentInfoDTO = new ContentInfoDTO();
                    Map<String, Object> contentInfo = Maps.newHashMap();
                    contentInfo.put("title", sceneDTO.getTitle());
                    contentInfo.put("subtitle", sceneDTO.getSubtitle());
                    contentInfo.put("sceneId", sceneDTO.getId());

                    //补全场景集的信息
                    String contentSetId = contentEntity.getContentSetId();
                    String[] content = contentSetId.split("_");
                    String contentSetIdStr = content[1];
                    Map<String, String> staticData = staticDataMap.get(contentSetIdStr);
                    String contentSetTitle = staticData.get("contentSetTitle");
                    contentInfo.put("contentSetTitle", contentSetTitle);
                    String contentSetSubTitle = staticData.get("contentSetSubTitle");
                    contentInfo.put("contentSetSubTitle", contentSetSubTitle);
                    String backgroundImage = staticData.get("backgroundImage");
                    contentInfo.put("backgroundImage", backgroundImage);
                    String backgroundColor = staticData.get("backgroundColor");
                    contentInfo.put("backgroundColor", backgroundColor);
                    String banner = staticData.get("banner");
                    contentInfo.put("banner", banner);

                    contentInfoDTO.setContentInfo(contentInfo);
                    contentDTOMap.put(contentId, contentInfoDTO);
                }
            }
            return Flowable.just(Response.success(contentDTOMap));
        }catch (Exception e){
            return Flowable.just(Response.fail("query scene info error"));
        }
    }

    private List<SceneDTO> getOnlyScenesFromCaptain(List<String> sceneIdList) throws Exception {
        List<SceneDTO> scenesFromCaptain = Lists.newArrayList();
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
                        entity -> JSON.parseObject(JSON.toJSONString(entity.get("data")), SceneDTO.class)
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
