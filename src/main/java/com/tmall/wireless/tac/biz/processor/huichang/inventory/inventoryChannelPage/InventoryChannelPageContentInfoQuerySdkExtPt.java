package com.tmall.wireless.tac.biz.processor.huichang.inventory.inventoryChannelPage;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.tmall.aselfcaptain.cloudrec.api.EntityRenderService;
import com.tmall.aselfcaptain.cloudrec.domain.Entity;
import com.tmall.aselfcaptain.cloudrec.domain.EntityId;
import com.tmall.aselfcaptain.cloudrec.domain.EntityQueryOption;
import com.tmall.aselfcaptain.item.model.ChannelDataDO;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.contentinfo.ContentInfoQuerySdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.Response;
import com.tmall.tcls.gs.sdk.framework.model.constant.RequestKeyConstant;
import com.tmall.tcls.gs.sdk.framework.model.context.ContentEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.ContentInfoDTO;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextContent;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.utils.PageUrlUtil;
import com.tmall.wireless.tac.biz.processor.huichang.inventory.SceneDTO;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import io.reactivex.Flowable;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@SdkExtension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
        useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
        scenario = HallScenarioConstant.HALL_SCENARIO_SCENARIO_INVENTORY_CHANNEL_PAGE)
public class InventoryChannelPageContentInfoQuerySdkExtPt extends Register implements ContentInfoQuerySdkExtPt {
    @Autowired
    TacLogger tacLogger;

    @Resource
    EntityRenderService entityRenderService;
    public static final String CHANNELNAME = "sceneLdb";
    public static final Long defaultSmAreaId = 310100L;
    public static final String defaultLogAreaId = "107";

    private static final String ACTIVITY_SCENE_PREFIX = "tcls_ugc_scene_v1_";
    @Override
    public Flowable<Response<Map<Long, ContentInfoDTO>>> process(SgFrameworkContextContent sgFrameworkContextContent) {
        // Todo likunlin
        tacLogger.debug("扩展点InventoryChannelPageContentInfoQuerySdkExtPt");
        Map<Long, ContentInfoDTO> captainsContent = Maps.newHashMap();
        List<EntityId> ids = new ArrayList<>();
        List<ContentEntity> contentEntityList = sgFrameworkContextContent.getContentEntityOriginDataDTO().getResult();
        RequestContext4Ald requestContext4Ald = (RequestContext4Ald)(sgFrameworkContextContent.getTacContext());
        Map<String, Object> aldParams = requestContext4Ald.getAldParam();
        Map<Long, ContentEntity> contentEntityListMap = contentEntityList.stream().collect(Collectors.toMap(contentEntity -> contentEntity.getContentId(),contentEntity -> contentEntity, (key1, key2) -> key1));

        contentEntityList.forEach(e -> {
            EntityId entityId =  EntityId.of(ACTIVITY_SCENE_PREFIX + e.getContentId(), "content");
            ids.add(entityId);
        });
        EntityQueryOption entityQueryOption = new EntityQueryOption();
        Long smAreaId = Optional.ofNullable(Long.valueOf((String)aldParams.get(RequestKeyConstant.SMAREAID))).orElse(defaultSmAreaId);
        entityQueryOption.setSmAreaId(smAreaId);

        List<ChannelDataDO> channelDataDOList = new ArrayList<>();
        ChannelDataDO channelDataDO = new ChannelDataDO();
        channelDataDO.setDataKey("data");
        channelDataDO.setChannelField("data");
        channelDataDO.setChannelName(CHANNELNAME);
        channelDataDOList.add(channelDataDO);
        entityQueryOption.setChannelDataDOS(channelDataDOList);
        try{
            MultiResponse<Entity> render = entityRenderService.render(ids, entityQueryOption);
            if(render.isSuccess()) {
                List<SceneDTO> sceneDTOList = render.getData().stream()
                .map(
                        entity -> JSON.parseObject(JSON.toJSONString(entity.get("data")), SceneDTO.class)
                ).collect(Collectors.toList());
                tacLogger.debug("请求Captain返回结果: " + JSONObject.toJSONString(sceneDTOList));
                sceneDTOList.forEach(
                        sceneDTO -> {
                            captainsContent.put(Long.valueOf(sceneDTO.getId()), parseCaptainResult(sceneDTO, contentEntityListMap, sgFrameworkContextContent));

                        }
                );
                tacLogger.debug("请求Captain结果整理：" + JSONObject.toJSONString(captainsContent));
                return Flowable.just(Response.success(captainsContent));
            }
            else {
                // throw new Exception("查询不成功");
                tacLogger.debug("查询不成功");
                return Flowable.just(Response.fail("captain fail"));
            }
        }
        catch (Exception e) {
            tacLogger.debug("查询异常");
            return Flowable.just(Response.fail("captain error"));
        }
    }
    private ContentInfoDTO parseCaptainResult(SceneDTO sceneDTO, Map<Long, ContentEntity> contentEntityListMap, SgFrameworkContextContent sgFrameworkContextContent) {
        Context context = sgFrameworkContextContent.getTacContext();
        RequestContext4Ald requestContext4Ald = (RequestContext4Ald) context;
        Map<String, Object> aldParams = requestContext4Ald.getAldParam();
        ContentEntity contentEntity = contentEntityListMap.get(Long.valueOf(sceneDTO.getId()));

        Map<String, Object> contentMap = Maps.newHashMap();
        contentMap.put("contentId", sceneDTO.getId());
        contentMap.put("contentTitle",sceneDTO.getTitle());
        contentMap.put("contentSubTitle",sceneDTO.getSubtitle());
        contentMap.put("itemSetIds", sceneDTO.getSetIds()); // 默认返回setId列表
//        contentMap.put("contentPic", ) //Todo 是avatarUrl还是bannerUrl
//        contentMap.put("contentType",) //Todo
        contentMap.put("scm", contentEntity.getTrack_point()); //Todo
        contentMap.put("marketChannel",sceneDTO.getMarketChannel());
        contentMap.put("setSource", sceneDTO.getMarketChannel());
        String urlParam = "";

        urlParam = PageUrlUtil.addParams(urlParam, "sceneId", String.valueOf(sceneDTO.getId()));
        urlParam = PageUrlUtil.addParams(urlParam, "setId", String.valueOf(sceneDTO.getSetIds().get(0)));
        String locType = PageUrlUtil.getParamFromCurPageUrl(aldParams, "locType", tacLogger);
        if("B2C".equals(locType) || locType == null){
            urlParam = PageUrlUtil.addParams(urlParam, "locType", "B2C");
        }else {
            urlParam = PageUrlUtil.addParams(urlParam, "locType", "O2O");
        }
        String itemRecommand = PageUrlUtil.getParamFromCurPageUrl(aldParams, "itemRecommand", tacLogger);
        if(StringUtils.isNotBlank(itemRecommand)) {
            urlParam = PageUrlUtil.addParams(urlParam, "itemRecommand", itemRecommand);
        }

        contentMap.put("urlParams", urlParam);
        ContentInfoDTO contentInfoDTO = new ContentInfoDTO();
        contentInfoDTO.setContentInfo(contentMap);
        return contentInfoDTO;
    }
}
