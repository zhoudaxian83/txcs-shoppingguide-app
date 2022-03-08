package com.tmall.wireless.tac.biz.processor.brandclub.bangdan;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.tmall.aselfcaptain.cloudrec.api.EntityRenderService;
import com.tmall.aselfcaptain.cloudrec.domain.Entity;
import com.tmall.aselfcaptain.cloudrec.domain.EntityId;
import com.tmall.aselfcaptain.cloudrec.domain.EntityQueryOption;
import com.tmall.aselfcaptain.item.model.ChannelDataDO;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.framework.extensions.content.vo.ContentFilterSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ContentVO;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.model.context.CommonUserParams;
import com.tmall.tcls.gs.sdk.framework.model.context.LocParams;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContext;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextContent;
import com.tmall.wireless.tac.biz.processor.brandclub.bangdan.model.FuzzyUtil;
import com.tmall.wireless.tac.biz.processor.brandclub.bangdan.model.ItemCustomerDTO;
import com.tmall.wireless.tac.biz.processor.brandclub.fp.BrandClubFirstPageContentFilterSdkExtPt;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


@SdkExtension(
        bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.BRAND_CLUB_BANGDAN
)
public class BrandClubBangdanContentFilterSdkExtPt extends BrandClubFirstPageContentFilterSdkExtPt implements ContentFilterSdkExtPt {
    private static final String ACTIVITY_SCENE_PREFIX = "tcls_ugc_scene_v1_";
    public static final String CHANNELNAME = "sceneLdb";
    @Resource
    EntityRenderService entityRenderService;

    @Override
    public SgFrameworkResponse<ContentVO> process(SgFrameworkContextContent sgFrameworkContextContent) {
        SgFrameworkResponse<ContentVO> response = super.process(sgFrameworkContextContent);
        List<ContentVO> itemAndContentList = response.getItemAndContentList();
        for (ContentVO contentVO : itemAndContentList) {
            String boardId = contentVO.getString("contentId");
            List<ItemEntityVO> items = (List<ItemEntityVO>)contentVO.get("items");
            List<Long> itemIdList = items.stream().map(ItemEntityVO::getItemId).collect(Collectors.toList());
            Map<Long, ItemCustomerDTO> longItemCustomerDTOMap = queryItemCustomer(sgFrameworkContextContent, itemIdList, boardId);
            LOGGER.debug("longItemCustomerDTOMap:{}", longItemCustomerDTOMap);
            for (ItemEntityVO item : items) {
                if(longItemCustomerDTOMap.get(item.getItemId()) != null) {
                    item.put("rankValue", longItemCustomerDTOMap.get(item.getItemId()).getItemRankValue());
                    item.put("fuzzyRankValue", FuzzyUtil.fuzzy(longItemCustomerDTOMap.get(item.getItemId()).getItemRankValue()));
                }
            }
        }
        return response;
    }

    private Map<Long, ItemCustomerDTO> queryItemCustomer(SgFrameworkContextContent sgFrameworkContextContent, List<Long> itemIds, String sceneId) {
        Map<Long, ItemCustomerDTO> itemCustomerFromCaptain = Maps.newHashMap();
        try{
            Long smAreaId = Optional.of(sgFrameworkContextContent).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getLocParams)
                    .map(LocParams::getSmAreaId).orElse(330110L);
            List<EntityId> ids = new ArrayList<>();
            String dataKeyPrefix = ACTIVITY_SCENE_PREFIX + sceneId + "_";
            itemIds.forEach(e -> {
                EntityId entityId =  EntityId.of(dataKeyPrefix + e, "content");
                ids.add(entityId);
            });
            LOGGER.debug("ids:{}", ids);
            EntityQueryOption entityQueryOption = new EntityQueryOption();
            entityQueryOption.setSmAreaId(smAreaId);
            List<ChannelDataDO> channelDataDOList = new ArrayList<>();
            List<String> paramsName = Arrays.asList("itemId", "itemRankValue");
            for(String paramName : paramsName){
                ChannelDataDO channelDataDO = new ChannelDataDO();
                channelDataDO.setDataKey(paramName);
                channelDataDO.setChannelField(paramName);
                channelDataDO.setChannelName(CHANNELNAME);
                channelDataDOList.add(channelDataDO);
            }
            entityQueryOption.setChannelDataDOS(channelDataDOList);
            LOGGER.debug("channelDataDOList:{}", channelDataDOList);
            try{
                MultiResponse<Entity> render = entityRenderService.render(ids, entityQueryOption);
                LOGGER.debug("queryItemCustomer:{}", render.getData());
                if(render.isSuccess()) {
                    itemCustomerFromCaptain = render.getData().stream()
                            .map(
                                    entity -> JSON.parseObject(JSON.toJSONString(entity), ItemCustomerDTO.class)
                            ).collect(Collectors.toMap(
                                    e -> e.getItemId(),
                                    e -> e,
                                    (a, b) -> a
                            ));
                    LOGGER.debug("itemCustomerFromCaptain:{}", itemCustomerFromCaptain);
                }
                else {
                    LOGGER.debug("queryItemCustomer请求tair失败");
                }
            } catch (Exception e) {
                LOGGER.error("queryItemCustomer, 请求tair出错", e);
            }
            return itemCustomerFromCaptain;
        }
        catch(Exception e) {
            LOGGER.error("查询渲染商品客户信息数据异常", e);
            return itemCustomerFromCaptain;
        }
    }
}
