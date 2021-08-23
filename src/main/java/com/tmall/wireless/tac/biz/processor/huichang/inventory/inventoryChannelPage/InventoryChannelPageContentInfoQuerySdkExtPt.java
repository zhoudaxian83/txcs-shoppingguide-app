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
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import io.reactivex.Flowable;
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
    private static final String CUSTOMER_SUM_PREFIX = "customerSum_";
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
                List<JSONObject> captainMaps = render.getData().stream()
                .map(
                        entity -> JSON.parseObject(JSON.toJSONString(entity.get("data")))
                ).collect(Collectors.toList());
                tacLogger.debug("请求Captain返回结果: " + JSONObject.toJSONString(captainMaps));
                captainMaps.forEach(
                        captainMap -> {
                            if(captainMap.containsKey("id")) {
                                captainsContent.put(captainMap.getLong("id"), parseCaptainResult(captainMap, contentEntityListMap, sgFrameworkContextContent));
                            }
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
    private ContentInfoDTO parseCaptainResult(JSONObject jsonObject, Map<Long, ContentEntity> contentEntityListMap, SgFrameworkContextContent sgFrameworkContextContent) {
        Context context = sgFrameworkContextContent.getTacContext();
        RequestContext4Ald requestContext4Ald = (RequestContext4Ald) context;
        Map<String, Object> aldParams = requestContext4Ald.getAldParam();

        Map<String, Object> contentMap = Maps.newHashMap();
        contentMap.put("id", jsonObject.getString("id"));
        contentMap.put("title",jsonObject.getString("title"));
        contentMap.put("subtitle",jsonObject.getString("subtitle"));
        contentMap.put("marketChannel",jsonObject.getString("marketChannel"));
        contentMap.put("setSource", jsonObject.getString("setSource"));
        contentMap.put("setId", (String) JSONArray.parseArray(jsonObject.getString("setIds")).get(0)); // 默认返回setId列表
        String urlParam = "";

        urlParam = PageUrlUtil.addParams(urlParam, "sceneId", String.valueOf(contentMap.get("id")));
        urlParam = PageUrlUtil.addParams(urlParam, "setId", String.valueOf(contentMap.get("setId")));
        String locType = PageUrlUtil.getParamFromCurPageUrl(aldParams, "locType", tacLogger);
        if("B2C".equals(locType) || locType == null){
            urlParam = PageUrlUtil.addParams(urlParam, "locType", "B2C");
        }else {
            urlParam = PageUrlUtil.addParams(urlParam, "locType", "O2O");
        }

        contentMap.put("urlParams", urlParam);
        ContentInfoDTO contentInfoDTO = new ContentInfoDTO();
        contentInfoDTO.setContentInfo(contentMap);
        return contentInfoDTO;
    }
}
