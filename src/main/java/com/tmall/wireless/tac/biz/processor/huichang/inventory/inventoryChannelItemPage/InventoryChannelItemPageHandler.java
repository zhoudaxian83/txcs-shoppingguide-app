package com.tmall.wireless.tac.biz.processor.huichang.inventory.inventoryChannelItemPage;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.alibaba.aladdin.lamp.sdk.solution.context.SolutionContext;
import com.alibaba.cdo.tt.shaded.com.google.common.collect.Lists;
import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tmall.aselfcaptain.cloudrec.api.EntityRenderService;
import com.tmall.aselfcaptain.cloudrec.domain.Entity;
import com.tmall.aselfcaptain.cloudrec.domain.EntityId;
import com.tmall.aselfcaptain.cloudrec.domain.EntityQueryOption;
import com.tmall.aselfcaptain.item.model.ChannelDataDO;
import com.tmall.aselfcaptain.util.StackTraceUtil;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.framework.model.constant.RequestKeyConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.utils.PageUrlUtil;
import com.tmall.wireless.tac.biz.processor.huichang.inventory.SceneDTO;
import com.tmall.wireless.tac.biz.processor.huichang.service.HallCommonContentRequestProxy;
import com.tmall.wireless.tac.biz.processor.huichang.service.HallCommonItemRequestProxy;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import com.tmall.wireless.tac.client.handler.TacReactiveHandler4Ald;
import com.tmall.wireless.tac.dataservice.log.TacLogConsts;
import io.reactivex.Flowable;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
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
        bizScenario.addProducePackage("huichang");
        Flowable<TacResult<List<GeneralItem>>> itemVOs = hallCommonItemRequestProxy.recommend(requestContext4Ald, bizScenario);
        SceneDTO sceneDTO = getScenesInfoFromCaptain(requestContext4Ald, tacLogger);
        GeneralItem sceneGeneralItem = buildSceneGeneralItem(sceneDTO, tacLogger);
        sceneGeneralItem.put("itemList", itemVOs.blockingFirst().getData());
        List<GeneralItem> sceneWithItems = Lists.newArrayList(sceneGeneralItem);
        return Flowable.just(TacResult.newResult(sceneWithItems));
    }

    private SceneDTO getScenesInfoFromCaptain(RequestContext4Ald requestContext4Ald, TacLogger tacLogger) throws Exception {
        final String ACTIVITY_SCENE_PREFIX = "tcls_ugc_scene_v1_";
        final Long defaultSmAreaId = 310100L;
        final String CHANNELNAME = "sceneLdb";

        Map<String, Object> aldParams = requestContext4Ald.getAldParam();
        String contentId = PageUrlUtil.getParamFromCurPageUrl(aldParams, "contentId", tacLogger); // Todo likunlin
        if(StringUtils.isBlank(contentId)) {
            tacLogger.debug("contentId为空");
            throw new Exception("contentId为空");
        }
        if(!contentId.startsWith(ACTIVITY_SCENE_PREFIX)){
            contentId = ACTIVITY_SCENE_PREFIX + contentId;
        }
        List<EntityId> ids = Arrays.asList(EntityId.of(contentId, "content"));
        tacLogger.debug("EntityId:{}" + JSON.toJSONString(ids));
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
        tacLogger.debug("channelDataDOList:" + JSON.toJSONString(channelDataDOList));
        try{
            MultiResponse<Entity> render = entityRenderService.render(ids, entityQueryOption);
            if(render.isSuccess()) {
                tacLogger.debug("render:" + JSON.toJSONString(render.getData()));
                SceneDTO sceneDTO = JSON.parseObject(
                        JSON.toJSONString(render.getData().get(0).get("data")),
                        SceneDTO.class
                );
                if(sceneDTO == null) {
                    tacLogger.debug("场景信息没有渲染出来");
                    throw new Exception("场景信息没有渲染出来");
                }
                tacLogger.debug("渲染的场景信息:" + JSONObject.toJSONString(sceneDTO));
                return sceneDTO;
            }
            else {
                tacLogger.debug("查询失败");
                throw new Exception("查询不成功");
            }
        }
        catch (Exception e) {
            tacLogger.debug( "渲染场景数据异常:"+ StackTraceUtil.stackTrace(e));
            throw new Exception("渲染场景数据异常", e);
        }
    }
    private GeneralItem buildSceneGeneralItem(SceneDTO sceneDTO, TacLogger tacLogger){
        GeneralItem item = new GeneralItem();
        if(sceneDTO == null) {
            tacLogger.debug("场景渲染信息为空");
        }
        item.put("contentId", sceneDTO.getId());
        item.put("contentTitle",sceneDTO.getTitle());
        item.put("contentSubtitle",sceneDTO.getSubtitle());
        item.put("rankType", sceneDTO.getProperty().get("rankType"));
        item.put("shortTitle", sceneDTO.getProperty().get("shortTitle"));
        return item;
    }
}
