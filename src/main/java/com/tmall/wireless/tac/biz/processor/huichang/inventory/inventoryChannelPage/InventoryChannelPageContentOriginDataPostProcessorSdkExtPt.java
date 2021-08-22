package com.tmall.wireless.tac.biz.processor.huichang.inventory.inventoryChannelPage;

import com.google.common.collect.Lists;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataPostProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataProcessRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.ContentEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextContent;
import com.tmall.txcs.gs.model.item.BizType;
import com.tmall.txcs.gs.model.item.O2oType;
import com.tmall.wireless.tac.biz.processor.huichang.common.utils.PageUrlUtil;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InventoryChannelPageContentOriginDataPostProcessorSdkExtPt implements ContentOriginDataPostProcessorSdkExtPt {
    @Override
    public OriginDataDTO<ContentEntity> process(ContentOriginDataProcessRequest contentOriginDataProcessRequest) {
        SgFrameworkContextContent sgFrameworkContextContent = Optional.of(contentOriginDataProcessRequest.getSgFrameworkContextContent()).orElse(new SgFrameworkContextContent());
        OriginDataDTO<ContentEntity> contentEntityOriginDataDTO = Optional.of(contentOriginDataProcessRequest.getContentEntityOriginDataDTO()).orElse(new OriginDataDTO<ContentEntity>());
        List<ContentEntity> contentEntityList =  Optional.of(contentEntityOriginDataDTO).map(OriginDataDTO::getResult).orElse(Lists.newArrayList());

        RequestContext4Ald requestContext4Ald = (RequestContext4Ald)(sgFrameworkContextContent.getTacContext());
        Map<String, Object> aldParams = requestContext4Ald.getParams();
        String itemRecommand = PageUrlUtil.getParamFromCurPageUrl(aldParams, null, "itemRecommand"); // 为你推荐商品

        int index = Optional.ofNullable(sgFrameworkContextContent.getCommonUserParams().getUserPageInfo().getIndex()).orElse(0);
        if(StringUtils.isNotBlank(itemRecommand) && index == 0) {
            ItemEntity itemRecommandEntity = new ItemEntity();
            itemRecommandEntity.setItemId(Long.valueOf(itemRecommand));
            String locType = PageUrlUtil.getParamFromCurPageUrl(aldParams, null, "locType");
            String detailLocType = getDetailLocType(locType, sgFrameworkContextContent);
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
        return contentEntityOriginDataDTO;
    }

    private String getDetailLocType(String locType, SgFrameworkContextContent sgFrameworkContextContent) {
        if("B2C".equals(locType) || locType == null) {
            return O2oType.B2C.name();
        } else {
            if(Optional.ofNullable(sgFrameworkContextContent.getCommonUserParams().getLocParams().getRt1HourStoreId()).orElse(0L) > 0) {
                return O2oType.O2OOneHour.name();
            } else if(Optional.ofNullable(sgFrameworkContextContent.getCommonUserParams().getLocParams().getRtHalfDayStoreId()).orElse(0L) > 0){
                return O2oType.O2OHalfDay.name();
            } else {
                return O2oType.O2O.name();
            }
        }
    }
}
