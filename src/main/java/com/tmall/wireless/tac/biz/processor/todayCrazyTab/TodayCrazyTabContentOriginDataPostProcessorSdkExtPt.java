package com.tmall.wireless.tac.biz.processor.todayCrazyTab;

import com.google.common.collect.Lists;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataPostProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataProcessRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.ContentEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallCommonAldConstant;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created from template by 进舟 on 2021-09-22 16:00:05.
 */

@SdkExtension(
        bizId = "supermarket",
        useCase = "b2c",
        scenario = "todayCrazyTab"
)
public class TodayCrazyTabContentOriginDataPostProcessorSdkExtPt extends Register implements ContentOriginDataPostProcessorSdkExtPt {




    @Override
    public OriginDataDTO<ContentEntity> process(ContentOriginDataProcessRequest contentOriginDataProcessRequest) {

        Context tacContext = contentOriginDataProcessRequest.getSgFrameworkContextContent().getTacContext();
        RequestContext4Ald requestContext4Ald = (RequestContext4Ald) tacContext;

        Map<String, Object> aldContext = requestContext4Ald.getAldContext();
        Object staticScheduleData = aldContext.get(HallCommonAldConstant.STATIC_SCHEDULE_DATA);

        List<Map<String, Object>> staticScheduleDataList = (List<Map<String, Object>>)staticScheduleData;


        List<ContentEntity> contentEntityList = Optional.of(contentOriginDataProcessRequest)
                .map(ContentOriginDataProcessRequest::getContentEntityOriginDataDTO)
                .map(OriginDataDTO::getResult)
                .orElse(Lists.newArrayList());

        Map<Long, ContentEntity> itemSetIdToContent = contentEntityList.stream().collect(Collectors.toMap(ContentEntity::getContentId, c -> c));

//        Long contentId;
//        String contentSetId;
//        String unique_id;
//        int rn;
//        int contentType;
//        String track_point;
//        List<String> itemSets;
//        List<ItemEntity> items;
//        Map<String, Object> extInfo;
        List<ContentEntity> contentEntityListFixed = Lists.newArrayList();
        List<ContentEntity> contentEntityListNotFixed = Lists.newArrayList();
        for (Map<String, Object> stringObjectMap : staticScheduleDataList) {
            Long id = Long.valueOf(String.valueOf(stringObjectMap.get("default_contentId")));
            String itemSetId = String.valueOf(stringObjectMap.get("default_datasetId"));
            ContentEntity contentEntity = null;
            if (!StringUtils.isNumeric(itemSetId) || itemSetIdToContent.get(Long.valueOf(itemSetId)) == null) {
                contentEntity = new ContentEntity();
                contentEntity.setContentId(id);
                contentEntity.setRn(Integer.MAX_VALUE);
                contentEntity.setContentSetId("0");
                contentEntity.setUnique_id("");
                contentEntity.setTrack_point("");
                contentEntity.setItemSets(Lists.newArrayList());
                contentEntity.setItems(Lists.newArrayList());
                contentEntity.setExtInfo(stringObjectMap);
            } else {
                contentEntity = itemSetIdToContent.get(Long.valueOf(itemSetId));
                contentEntity.setContentId(id);
                contentEntity.setExtInfo(stringObjectMap);
            }

            Integer position = getPosition(contentEntity);

            if (position > 0) {
                contentEntityListFixed.add(contentEntity);
            } else {
                contentEntityListNotFixed.add(contentEntity);
            }
        }

        contentEntityListNotFixed.sort((o1, o2) -> o2.getRn() - o1.getRn());

        if (CollectionUtils.isNotEmpty(contentEntityListFixed)) {

            contentEntityListFixed.sort((o1, o2) -> getPosition(o2) - getPosition(o1));

            for (ContentEntity contentEntity : contentEntityListFixed) {
                contentEntityListNotFixed.add(Math.min(getPosition(contentEntity) - 1, contentEntityListNotFixed.size()), contentEntity);
            }
        }

        OriginDataDTO<ContentEntity> contentEntityOriginDataDTO = contentOriginDataProcessRequest.getContentEntityOriginDataDTO();
        contentEntityOriginDataDTO.setResult(contentEntityListNotFixed);

        return contentEntityOriginDataDTO;
    }

    private Integer getPosition(ContentEntity contentEntity) {
        return Optional.of(contentEntity)
                .map(ContentEntity::getExtInfo)
                .map(m -> m.get("position"))
                .filter(o -> StringUtils.isNumeric(String.valueOf(o)))
                .map(o -> Integer.valueOf(String.valueOf(o)))
                .orElse(-1);
    }
}
