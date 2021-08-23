package com.tmall.wireless.tac.biz.processor.huichang.inventory.inventoryChannelPage;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.tmall.tcls.gs.sdk.biz.uti.MapUtil;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataPostProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataProcessRequest;
import com.tmall.tcls.gs.sdk.framework.model.constant.RequestKeyConstant;
import com.tmall.tcls.gs.sdk.framework.model.context.*;
import com.tmall.txcs.gs.model.item.BizType;
import com.tmall.txcs.gs.model.item.O2oType;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.utils.PageUrlUtil;
import com.tmall.wireless.tac.biz.processor.huichang.common.utils.ParseCsa;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SdkExtension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
        useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
        scenario = HallScenarioConstant.HALL_SCENARIO_SCENARIO_INVENTORY_CHANNEL_PAGE)
public class InventoryChannelPageContentOriginDataPostProcessorSdkExtPt extends Register implements ContentOriginDataPostProcessorSdkExtPt {
    @Autowired
    TacLogger tacLogger;

    @Override
    public OriginDataDTO<ContentEntity> process(ContentOriginDataProcessRequest contentOriginDataProcessRequest) {
        tacLogger.debug("扩展点InventoryChannelPageContentOriginDataPostProcessorSdkExtPt");
        SgFrameworkContextContent sgFrameworkContextContent = Optional.of(contentOriginDataProcessRequest.getSgFrameworkContextContent()).orElse(new SgFrameworkContextContent());
        OriginDataDTO<ContentEntity> contentEntityOriginDataDTO = Optional.of(contentOriginDataProcessRequest.getContentEntityOriginDataDTO()).orElse(new OriginDataDTO<ContentEntity>());
        List<ContentEntity> contentEntityList =  Optional.of(contentEntityOriginDataDTO).map(OriginDataDTO::getResult).orElse(Lists.newArrayList());
        tacLogger.debug("调顺序之前 " + JSONObject.toJSONString(contentEntityList));
        RequestContext4Ald requestContext4Ald = (RequestContext4Ald)(sgFrameworkContextContent.getTacContext());
        Map<String, Object> aldParams = requestContext4Ald.getAldParam();
        String itemRecommand = PageUrlUtil.getParamFromCurPageUrl(aldParams, "itemRecommand", tacLogger); // 为你推荐商品

        String indexStr = PageUrlUtil.getParamFromCurPageUrl(aldParams, "index", tacLogger);
        int index = 0;
        if(StringUtils.isNotBlank(indexStr)) {
            index = Integer.valueOf(indexStr);
        } else {
            index = Optional.ofNullable(Integer.valueOf((String)aldParams.get("pageIndex"))).orElse(0);
        }

        if(StringUtils.isNotBlank(itemRecommand) && index == 0) {
            ItemEntity itemRecommandEntity = new ItemEntity();
            itemRecommandEntity.setItemId(Long.valueOf(itemRecommand));
            String locType = PageUrlUtil.getParamFromCurPageUrl(aldParams, "locType", tacLogger);
            String detailLocType = getDetailLocType(locType, aldParams);
            itemRecommandEntity.setO2oType(detailLocType);
            itemRecommandEntity.setBusinessType(detailLocType);
            itemRecommandEntity.setBizType(BizType.SM.getCode());
//            itemRecommandEntity.setTop(true); // Todo likunlin

            if(CollectionUtils.isNotEmpty(contentEntityList)) {
                List<ItemEntity> itemEntityList = Optional.ofNullable(contentEntityList.get(0).getItems()).orElse(new ArrayList<ItemEntity>());
                List<ItemEntity> newItemEntityList = Lists.newArrayList();
                newItemEntityList.add(itemRecommandEntity);
                for(ItemEntity itemEntity: itemEntityList) {
                    if(!itemEntity.getItemId().equals(itemRecommandEntity.getItemId())) {
                        newItemEntityList.add(itemEntity);
                    }
                }
                contentEntityList.get(0).setItems(newItemEntityList);
            }

            contentEntityOriginDataDTO.setResult(contentEntityList);
        }
        tacLogger.debug("调顺序之后 " + JSONObject.toJSONString(contentEntityOriginDataDTO.getResult()));
        return contentEntityOriginDataDTO;
    }

    private String getDetailLocType(String locType, Map<String, Object> aldParams) {
        if("B2C".equals(locType) || locType == null) {
            if(StringUtils.isBlank(locType)) {
                tacLogger.debug("locType是空");
            }
            return O2oType.B2C.name();
        } else {
            Long smAreaId = MapUtil.getLongWithDefault(aldParams, RequestKeyConstant.SMAREAID, 310100L);
            LocParams locParams = ParseCsa.parseCsaObj(aldParams.get(RequestKeyConstant.USER_PARAMS_KEY_CSA), smAreaId);
            if(Optional.ofNullable(locParams.getRt1HourStoreId()).orElse(0L) > 0) {
                return O2oType.O2OOneHour.name();
            } else if(Optional.ofNullable(locParams.getRtHalfDayStoreId()).orElse(0L) > 0){
                return O2oType.O2OHalfDay.name();
            } else {
                return O2oType.O2O.name();
            }
        }
    }
}
