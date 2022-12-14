package com.tmall.wireless.tac.biz.processor.brandclub.bangdan;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
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
import com.tmall.tmallwireless.tac.spi.context.SPIResult;
import com.tmall.wireless.store.spi.tair.TairSpi;
import com.tmall.wireless.tac.biz.processor.brandclub.bangdan.model.BrandBasicInfo;
import com.tmall.wireless.tac.biz.processor.brandclub.bangdan.model.FuzzyUtil;
import com.tmall.wireless.tac.biz.processor.brandclub.bangdan.model.ItemCustomerDTO;
import com.tmall.wireless.tac.biz.processor.brandclub.fp.BrandClubFirstPageContentFilterSdkExtPt;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.domain.Context;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


@SdkExtension(
        bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.BRAND_CLUB_BANGDAN
)
public class BrandClubBangdanContentFilterSdkExtPt extends BrandClubFirstPageContentFilterSdkExtPt implements ContentFilterSdkExtPt {
    private static final Logger logger = LoggerFactory.getLogger(BrandClubBangdanContentFilterSdkExtPt.class);
    private static final String ACTIVITY_SCENE_PREFIX = "tcls_ugc_scene_v1_";
    public static final String CHANNELNAME = "sceneLdb";
    private static final int NAME_SPACE = 6508;
    private static final String TAIR_USER_NAME = "72d8759c62674088";
    private static final String BRAND_INFO_KEY_PREFIX = "brandPavilion_";

    private static final Map<String, String> rankTypeFormatterMap = new HashMap<String, String>(){{
        put("SALE", "???30?????????%s???");
        put("COMMENT", "?????????%s?????????");
        put("REPURCHASE", "??????%s?????????");
    }};

    @Resource
    EntityRenderService entityRenderService;

    @Resource
    TairSpi tairSpi;

    @Override
    public SgFrameworkResponse<ContentVO> process(SgFrameworkContextContent sgFrameworkContextContent) {
        SgFrameworkResponse<ContentVO> response = super.process(sgFrameworkContextContent);
        String brandId = Optional.of(sgFrameworkContextContent)
                .map(SgFrameworkContext::getTacContext)
                .map(Context::getParams)
                .map(m -> m.get("brandId"))
                .map(Object::toString).orElse(null);
        BrandBasicInfo brandBasicInfo = queryBrandBasicInfo(brandId);

        List<ContentVO> itemAndContentList = response.getItemAndContentList();
        for (ContentVO contentVO : itemAndContentList) {
            String boardId = contentVO.getString("contentId");
            List<ItemEntityVO> items = (List<ItemEntityVO>)contentVO.get("items");
            List<Long> itemIdList = items.stream().map(ItemEntityVO::getItemId).collect(Collectors.toList());
            Map<Long, ItemCustomerDTO> longItemCustomerDTOMap = queryItemCustomer(sgFrameworkContextContent, itemIdList, boardId);
            String rankType = contentVO.getString("rankType");
            for (ItemEntityVO item : items) {
                if(longItemCustomerDTOMap.get(item.getItemId()) != null) {
                    item.put("rankValue", longItemCustomerDTOMap.get(item.getItemId()).getItemRankValue());
                    if(StringUtils.isNotBlank(rankTypeFormatterMap.get(rankType))) {
                        item.put("fuzzyRankValue", String.format(rankTypeFormatterMap.get(rankType), FuzzyUtil.fuzzy(longItemCustomerDTOMap.get(item.getItemId()).getItemRankValue())));
                    }
                }
            }
            if(brandBasicInfo != null) {
                contentVO.put("brandName", brandBasicInfo.name());
                contentVO.put("brandId", brandId);
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
            try{
                MultiResponse<Entity> render = entityRenderService.render(ids, entityQueryOption);
                if(render.isSuccess()) {
                    itemCustomerFromCaptain = render.getData().stream()
                            .map(
                                    entity -> JSON.parseObject(JSON.toJSONString(entity), ItemCustomerDTO.class)
                            ).collect(Collectors.toMap(
                                    e -> e.getItemId(),
                                    e -> e,
                                    (a, b) -> a
                            ));
                }
                else {
                    logger.warn("queryItemCustomer??????tair??????");
                }
            } catch (Exception e) {
                logger.error("queryItemCustomer, ??????tair??????", e);
            }
            return itemCustomerFromCaptain;
        }
        catch(Exception e) {
            logger.error("??????????????????????????????????????????", e);
            return itemCustomerFromCaptain;
        }
    }

    public BrandBasicInfo queryBrandBasicInfo(String brandId) {
        if(brandId == null) {
            return null;
        }
        BrandBasicInfo brandBasicInfo = null;
        String tairKey = "online" + "_" + BRAND_INFO_KEY_PREFIX + brandId;
        try {
            SPIResult<Result<DataEntry>> resultSPIResult = tairSpi.get(TAIR_USER_NAME, NAME_SPACE, tairKey);
            if(resultSPIResult != null && resultSPIResult.isSuccess()) {
                Result<DataEntry> dataEntryResult = resultSPIResult.getData();
                if (dataEntryResult.isSuccess() && dataEntryResult.getValue() != null && dataEntryResult.getValue().getValue() != null) {
                    DataEntry value = dataEntryResult.getValue();
                    Object data = value.getValue();
                    brandBasicInfo = JSON.parseObject((String) data, BrandBasicInfo.class);
                }
            }
        } catch (Exception e) {
            logger.error("queryBrandBasicInfo", e);
        }
        return brandBasicInfo;
    }
}
