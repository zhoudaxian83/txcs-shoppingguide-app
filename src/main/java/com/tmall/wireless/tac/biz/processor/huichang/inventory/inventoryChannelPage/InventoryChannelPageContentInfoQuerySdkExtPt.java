package com.tmall.wireless.tac.biz.processor.huichang.inventory.inventoryChannelPage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.google.common.collect.Maps;
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
import com.tmall.tcls.gs.sdk.framework.model.constant.RequestKeyConstant;
import com.tmall.tcls.gs.sdk.framework.model.context.ContentEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.ContentInfoDTO;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextContent;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.utils.PageUrlUtil;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 场景请求captain渲染
 */
@SdkExtension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
        useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
        scenario = HallScenarioConstant.HALL_SCENARIO_SCENARIO_INVENTORY_CHANNEL_PAGE)
public class InventoryChannelPageContentInfoQuerySdkExtPt extends Register implements ContentInfoQuerySdkExtPt {
    @Autowired
    TacLogger tacLogger;

    @Resource
    EntityRenderService entityRenderService;
    public static final String CHANNELNAME = "sceneLdb";
    public static final Long DEFAULT_SMAREAID = 310100L;

    private static final String ACTIVITY_SCENE_PREFIX = "tcls_ugc_scene_v1_";
    @Override
    public Flowable<Response<Map<Long, ContentInfoDTO>>> process(SgFrameworkContextContent sgFrameworkContextContent) {
        tacLogger.debug("扩展点InventoryChannelPageContentInfoQuerySdkExtPt");
        try{
            List<ContentEntity> contentEntityList = sgFrameworkContextContent.getContentEntityOriginDataDTO().getResult();
            RequestContext4Ald requestContext4Ald = (RequestContext4Ald)(sgFrameworkContextContent.getTacContext());
            Map<String, Object> aldParams = requestContext4Ald.getAldParam();
            Map<Long, ContentEntity> contentEntityListMap = contentEntityList.stream().collect(Collectors.toMap(contentEntity -> contentEntity.getContentId(),contentEntity -> contentEntity, (key1, key2) -> key1));

            Map<Long, ContentInfoDTO> captainsContent = Maps.newHashMap();
            List<EntityId> ids = new ArrayList<>();
            contentEntityList.forEach(e -> {
                EntityId entityId =  EntityId.of(ACTIVITY_SCENE_PREFIX + e.getContentId(), "content");
                ids.add(entityId);
            });
            EntityQueryOption entityQueryOption = new EntityQueryOption();
            Long smAreaId = Optional.ofNullable((String)aldParams.get(RequestKeyConstant.SMAREAID)).map(Long::valueOf).orElse(DEFAULT_SMAREAID);
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
                    List<TairSceneDTO> sceneDTOList = render.getData().stream()
                            .map(
                                    entity -> JSON.parseObject(JSON.toJSONString(entity.get("data")), TairSceneDTO.class)
                            ).collect(Collectors.toList());
                    tacLogger.debug("请求Captain返回结果: " + JSONObject.toJSONString(sceneDTOList));
                    HadesLogUtil.stream("InventoryChannelPage")
                            .kv("InventoryChannelPageContentInfoQuerySdkExtPt", "process")
                            .kv("请求场景Captain返回结果", JSONObject.toJSONString(sceneDTOList))
                            .info();
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
                    HadesLogUtil.stream("InventoryChannelPage")
                            .kv("InventoryChannelPageContentInfoQuerySdkExtPt", "process")
                            .kv("查询不成功","render.isSuccess() == false")
                            .error();
                    return Flowable.just(Response.fail("captain fail"));
                }
            }
            catch (Exception e) {
                tacLogger.debug("查询异常");
                HadesLogUtil.stream("InventoryChannelPage")
                        .kv("InventoryChannelPageContentInfoQuerySdkExtPt", "process")
                        .kv("查询异常", StackTraceUtil.stackTrace(e))
                        .error();
                return Flowable.just(Response.fail("captain error"));
            }
        } catch (Exception e) {
            tacLogger.debug("查询异常"+ StackTraceUtil.stackTrace(e));
            HadesLogUtil.stream("InventoryChannelPage")
                    .kv("InventoryChannelPageContentInfoQuerySdkExtPt", "process")
                    .kv("查询异常", StackTraceUtil.stackTrace(e))
                    .error();
            return Flowable.just(Response.fail("captain error"));
        }
    }
    private ContentInfoDTO parseCaptainResult(TairSceneDTO sceneDTO, Map<Long, ContentEntity> contentEntityListMap, SgFrameworkContextContent sgFrameworkContextContent) {
        Context context = sgFrameworkContextContent.getTacContext();
        RequestContext4Ald requestContext4Ald = (RequestContext4Ald) context;
        Map<String, Object> aldParams = requestContext4Ald.getAldParam();
        ContentEntity contentEntity = contentEntityListMap.get(Long.valueOf(sceneDTO.getId()));

        Map<String, Object> contentMap = Maps.newHashMap();
        contentMap.put("contentId", sceneDTO.getId());
        contentMap.put("contentTitle",sceneDTO.getTitle());
        contentMap.put("contentSubTitle",sceneDTO.getSubtitle());
        contentMap.put("itemSetIds", sceneDTO.getItemsetIds()); // 默认返回setId列表
        contentMap.put("contentPic", sceneDTO.getProperty().get("avatarUrl")); //二跳页用小图
        contentMap.put("contentType",sceneDTO.getType());
        contentMap.put("scm", contentEntity.getTrack_point());
        contentMap.put("marketChannel",sceneDTO.getMarketChannel());
        contentMap.put("setSource", sceneDTO.getMarketChannel());
        // 场景承接页(三跳页)的url参数
        String urlParam = "";
        urlParam = PageUrlUtil.addParams(urlParam, "contentId", String.valueOf(sceneDTO.getId()));
        urlParam = PageUrlUtil.addParams(urlParam, "itemSetId", String.valueOf(sceneDTO.getItemsetIds().get(0)));
        String locType = PageUrlUtil.getParamFromCurPageUrl(aldParams, "locType");
        if("B2C".equals(locType) || locType == null){
            urlParam = PageUrlUtil.addParams(urlParam, "locType", "B2C");
        }else {
            urlParam = PageUrlUtil.addParams(urlParam, "locType", "O2O");
        }
        // 不用在url塞为你推荐商品
//        String itemRecommand = PageUrlUtil.getParamFromCurPageUrl(aldParams, "itemRecommand", tacLogger);
//        if(StringUtils.isNotBlank(itemRecommand)) {
//            urlParam = PageUrlUtil.addParams(urlParam, "itemRecommand", itemRecommand);
//        }
        contentMap.put("urlParams", urlParam);
        ContentInfoDTO contentInfoDTO = new ContentInfoDTO();
        contentInfoDTO.setContentInfo(contentMap);
        return contentInfoDTO;
    }
}
