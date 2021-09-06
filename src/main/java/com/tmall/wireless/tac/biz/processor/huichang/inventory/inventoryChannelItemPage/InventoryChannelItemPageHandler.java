package com.tmall.wireless.tac.biz.processor.huichang.inventory.inventoryChannelItemPage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Resource;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.tmall.aselfcaptain.cloudrec.api.EntityRenderService;
import com.tmall.aselfcaptain.cloudrec.domain.Entity;
import com.tmall.aselfcaptain.cloudrec.domain.EntityId;
import com.tmall.aselfcaptain.cloudrec.domain.EntityQueryOption;
import com.tmall.aselfcaptain.item.model.ChannelDataDO;
import com.tmall.aselfcaptain.util.StackTraceUtil;
import com.tmall.aselfcommon.model.scene.domain.TairSceneDTO;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.framework.model.constant.RequestKeyConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.utils.PageUrlUtil;
import com.tmall.wireless.tac.biz.processor.huichang.service.HallCommonItemRequestProxy;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import com.tmall.wireless.tac.client.handler.TacReactiveHandler4Ald;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 清单商品承接页(三跳页)
 */
@Component
public class InventoryChannelItemPageHandler extends TacReactiveHandler4Ald {
    @Autowired
    HallCommonItemRequestProxy hallCommonItemRequestProxy;
    @Autowired
    TacLogger tacLogger;
    @Resource
    EntityRenderService entityRenderService;

    @Override
    public Flowable<TacResult<List<GeneralItem>>> executeFlowable(RequestContext4Ald requestContext4Ald) throws Exception {
        BizScenario bizScenario = BizScenario.valueOf(HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
                HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
                HallScenarioConstant.HALL_SCENARIO_SCENARIO_INVENTORY_CHANNEL_ITEM_PAGE);
        bizScenario.addProducePackage(HallScenarioConstant.HALL_ITEM_SDK_PACKAGE);
        Flowable<TacResult<List<GeneralItem>>> itemVOs = hallCommonItemRequestProxy.recommend(requestContext4Ald, bizScenario);
        TairSceneDTO sceneDTO = getScenesInfoFromCaptain(requestContext4Ald);
        GeneralItem sceneGeneralItem = buildSceneGeneralItem(sceneDTO);
        List<GeneralItem> generalItemList = itemVOs.blockingFirst().getData();
        if(CollectionUtils.isNotEmpty(generalItemList)){
            generalItemList.get(0).put("extInfos", sceneGeneralItem);
        }
        return Flowable.just(TacResult.newResult(generalItemList));
    }

    private TairSceneDTO getScenesInfoFromCaptain(RequestContext4Ald requestContext4Ald) throws Exception {
        tacLogger.debug("组装场景信息");
        final String ACTIVITY_SCENE_PREFIX = "tcls_ugc_scene_v1_";
        final Long defaultSmAreaId = 310100L;
        final String CHANNELNAME = "sceneLdb";

        Map<String, Object> aldParams = requestContext4Ald.getAldParam();
        if(MapUtils.isEmpty(aldParams)) {
            tacLogger.debug("aldParams为空");
            HadesLogUtil.stream("InventoryChannelItemPage")
                    .kv("InventoryChannelItemPageHandler", "getScenesInfoFromCaptain")
                    .kv("数据缺失", "aldParams")
                    .error();
            throw new Exception("aldParams为空");
        }
        HadesLogUtil.stream("inventoryChannelItemPage")
                .kv("InventoryChannelItemPageHandler", "getScenesInfoFromCaptain")
                .kv("aldParams", JSONObject.toJSONString(aldParams))
                .info();
        String contentId = PageUrlUtil.getParamFromCurPageUrl(aldParams, "contentId"); // Todo likunlin
        if(StringUtils.isBlank(contentId)) {
            tacLogger.debug("contentId为空");
            HadesLogUtil.stream("InventoryChannelItemPage")
                    .kv("InventoryChannelItemPageHandler", "getScenesInfoFromCaptain")
                    .kv("url参数缺失", "contentId")
                    .error();
            throw new Exception("contentId为空");
        }
        if(!contentId.startsWith(ACTIVITY_SCENE_PREFIX)){
            contentId = ACTIVITY_SCENE_PREFIX + contentId;
        }
        List<EntityId> ids = Arrays.asList(EntityId.of(contentId, "content"));
        tacLogger.debug("EntityId: " + JSON.toJSONString(ids));
        HadesLogUtil.stream("InventoryChannelItemPage")
                .kv("InventoryChannelItemPageHandler", "getScenesInfoFromCaptain")
                .kv("EntityId", JSON.toJSONString(ids))
                .info();
        EntityQueryOption entityQueryOption = new EntityQueryOption();
        Long smAreaId = Optional.ofNullable((String)aldParams.get(RequestKeyConstant.SMAREAID)).map(Long::valueOf).orElse(defaultSmAreaId);
        entityQueryOption.setSmAreaId(smAreaId);

        List<ChannelDataDO> channelDataDOList = new ArrayList<>();
        ChannelDataDO channelDataDO = new ChannelDataDO();
        channelDataDO.setDataKey("data");
        channelDataDO.setChannelField("data");
        channelDataDO.setChannelName(CHANNELNAME);
        channelDataDOList.add(channelDataDO);
        entityQueryOption.setChannelDataDOS(channelDataDOList);
        tacLogger.debug("channelDataDOList:" + JSON.toJSONString(channelDataDOList));
        HadesLogUtil.stream("InventoryChannelItemPage")
                .kv("InventoryChannelItemPageHandler", "getScenesInfoFromCaptain")
                .kv("channelDataDOList", JSON.toJSONString(channelDataDOList))
                .info();
        try{
            MultiResponse<Entity> render = entityRenderService.render(ids, entityQueryOption);
            if(render.isSuccess()) {
                tacLogger.debug("render:" + JSON.toJSONString(render.getData()));
                HadesLogUtil.stream("InventoryChannelItemPage")
                        .kv("InventoryChannelItemPageHandler", "getScenesInfoFromCaptain")
                        .kv("render", JSON.toJSONString(render.getData()))
                        .info();
                TairSceneDTO sceneDTO = JSON.parseObject(
                        JSON.toJSONString(render.getData().get(0).get("data")),
                        TairSceneDTO.class
                );
                if(sceneDTO == null) {
                    tacLogger.debug("场景信息没有渲染出来");
                    HadesLogUtil.stream("InventoryChannelItemPage")
                            .kv("InventoryChannelItemPageHandler", "getScenesInfoFromCaptain")
                            .kv("场景信息没有渲染出来","sceneDTO为空")
                            .error();
                    throw new Exception("场景信息没有渲染出来");
                }
                tacLogger.debug("渲染的场景信息:" + JSONObject.toJSONString(sceneDTO));
                HadesLogUtil.stream("InventoryChannelItemPage")
                        .kv("InventoryChannelItemPageHandler", "getScenesInfoFromCaptain")
                        .kv("渲染的场景信息", JSONObject.toJSONString(sceneDTO))
                        .info();
                return sceneDTO;
            }
            else {
                tacLogger.debug("查询失败");
                HadesLogUtil.stream("InventoryChannelItemPage")
                        .kv("InventoryChannelItemPageHandler", "getScenesInfoFromCaptain")
                        .kv("查询失败", "render.isSuccess() == false")
                        .error();
                throw new Exception("查询不成功");
            }
        }
        catch (Exception e) {
            tacLogger.debug( "查询异常:"+ StackTraceUtil.stackTrace(e));
            HadesLogUtil.stream("InventoryChannelItemPage")
                    .kv("InventoryChannelItemPageHandler", "getScenesInfoFromCaptain")
                    .kv("查询异常",  StackTraceUtil.stackTrace(e))
                    .error();
            throw new Exception("渲染场景数据异常", e);
        }
    }
    private GeneralItem buildSceneGeneralItem(TairSceneDTO sceneDTO) throws Exception {
        GeneralItem item = new GeneralItem();
        if(sceneDTO == null) {
            tacLogger.debug("场景渲染信息为空");
            throw new Exception("场景渲染信息为空");
        }
        item.put("contentId", sceneDTO.getId());
        item.put("contentTitle",sceneDTO.getTitle());
        item.put("contentSubtitle",sceneDTO.getSubtitle());
        item.put("contentBackgroundPic", Optional.ofNullable(sceneDTO.getProperty()).map(property -> property.get("bannerUrl")).orElse(""));
        item.put("contentPic", Optional.ofNullable(sceneDTO.getProperty()).map(property -> property.get("avatarUrl")).orElse(""));
        item.put("contentType", sceneDTO.getType());
        item.put("itemSetIds", Optional.ofNullable(sceneDTO.getItemsetIds()).map(itemSetIds -> itemSetIds.get(0)).orElse(0L));
//        item.put("rankType", sceneDTO.getProperty().get("rankType"));
//        item.put("shortTitle", sceneDTO.getProperty().get("shortTitle"));
        GeneralItem contentModel = new GeneralItem();
        contentModel.put("contentModel", item);
        return contentModel;
    }
}
