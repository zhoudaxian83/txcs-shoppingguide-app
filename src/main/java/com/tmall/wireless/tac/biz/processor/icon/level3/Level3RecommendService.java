package com.tmall.wireless.tac.biz.processor.icon.level3;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tmall.aself.shoppingguide.client.cat.model.LabelDTO;
import com.tmall.aself.shoppingguide.client.cat.model.LabelRankDTO;
import com.tmall.aselfcommon.model.column.ColumnStatus;
import com.tmall.aselfcommon.model.column.MainColumnDTO;
import com.tmall.aselfcommon.model.column.MaterialDTO;
import com.tmall.aselfcommon.model.column.SubColumnDTO;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.framework.model.context.*;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkContentService;
import com.tmall.wireless.tac.biz.processor.common.PackageNameKey;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.icon.ColumnCacheService;
import com.tmall.wireless.tac.biz.processor.icon.level2.BusinessTypeUtil;
import com.tmall.wireless.tac.biz.processor.icon.level2.Level2RecommendService;
import com.tmall.wireless.tac.biz.processor.icon.level3.ext.IconLevel3ContentInfoQuerySdkExtPt;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class Level3RecommendService {


    public static final String labelTypeActivity = "activity";
    public static final String labelTypeNormal = "normal";

    @Autowired
    ShoppingguideSdkContentService shoppingguideSdkContentService;
    @Autowired
    ColumnCacheService columnCacheService;
    Logger LOGGER = LoggerFactory.getLogger(Level3RecommendService.class);

    public static String level3RequestKey = "level3Request";


    public Flowable<List<LabelDTO>> recommend(Level3Request level3Request, Context context) {


        LabelDTO DEFAULT_RECOMMEND_TAB;

        DEFAULT_RECOMMEND_TAB = new LabelDTO();
        DEFAULT_RECOMMEND_TAB.setBusiness(level3Request.getLevel2Business());
        DEFAULT_RECOMMEND_TAB.setText("推荐");
        DEFAULT_RECOMMEND_TAB.setId(0L);

        BizScenario b = BizScenario.valueOf(
                ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
                ScenarioConstantApp.LOC_TYPE_B2C,
                ScenarioConstantApp.ICON_CONTENT_LEVEL3
        );
        b.addProducePackage(PackageNameKey.OLD_RECOMMEND);
        List<LabelDTO> defaultTab = Lists.newArrayList(DEFAULT_RECOMMEND_TAB);
        context.put(level3RequestKey, level3Request);

        return shoppingguideSdkContentService.recommendWitchContext(context, b)
        .map(sgFrameworkContextContent -> {
            List<ContentDTO> contentDTOList = sgFrameworkContextContent.getContentDTOList();
            OriginDataDTO<ContentEntity> contentEntityOriginDataDTO = sgFrameworkContextContent.getContentEntityOriginDataDTO();
            if (CollectionUtils.isEmpty(contentDTOList) || contentEntityOriginDataDTO == null || CollectionUtils.isEmpty(contentEntityOriginDataDTO.getResult())) {
                return defaultTab;
            }
            return buildIconTabList(contentDTOList, contentEntityOriginDataDTO.getResult(), sgFrameworkContextContent, level3Request, DEFAULT_RECOMMEND_TAB);
        }).onErrorReturn(throwable -> {
            LOGGER.error("shoppingguideSdkContentService.recommendWitchContext error", throwable);
            return defaultTab;
        }).defaultIfEmpty(defaultTab);

    }

    private List<LabelDTO> buildIconTabList(List<ContentDTO> contentDTOList, List<ContentEntity> contentEntityList, SgFrameworkContextContent sgFrameworkContextContent, Level3Request level3Request, LabelDTO DEFAULT_RECOMMEND_TAB) {

        MainColumnDTO column = columnCacheService.getColumn(Long.valueOf(level3Request.getLevel2Id()));

        MaterialDTO materialDTOIsActivity = column.getMaterialDTOMap().get("isActivity");
        boolean isActivity = materialDTOIsActivity != null && "1".equals(materialDTOIsActivity.getExtValue());


        LocParams locParams = Optional.ofNullable(sgFrameworkContextContent).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getLocParams).orElse(null);
        if (CollectionUtils.isEmpty(contentDTOList) || CollectionUtils.isEmpty(contentEntityList)) {
            return Lists.newArrayList(DEFAULT_RECOMMEND_TAB);
        }

        Map<Long, ContentDTO> contentDTOMap = contentDTOList.stream().collect(Collectors.toMap(ContentDTO::getContentId, c -> c));

        Map<Long, SubColumnDTO> subColumnDTOMap = Maps.newHashMap();
        Set<String> businessTypeSet = Sets.newHashSet();
        List<LabelDTO> result = Lists.newArrayList();
        int i = 1;

        for (ContentEntity contentEntity : contentEntityList) {


            ContentDTO contentDTO = contentDTOMap.get(contentEntity.getContentId());
            Object subColumnObj = Optional.ofNullable(contentDTO).map(ContentDTO::getContentInfo).map(m -> m.get(IconLevel3ContentInfoQuerySdkExtPt.SUB_COLUMN_DTO_KEY)).orElse(null);

            if (!(subColumnObj instanceof SubColumnDTO)) {
                continue;
            }

            SubColumnDTO subColumn = (SubColumnDTO) subColumnObj;

            subColumnDTOMap.put(subColumn.getId(), subColumn);
            if (checkValid(subColumn)) {
                LabelDTO labelDTO = new LabelDTO();
                labelDTO.setRank(i ++);
                labelDTO.setText(subColumn.getName());
                labelDTO.setId(subColumn.getId());
                String businessType = BusinessTypeUtil.processType(locParams, subColumn.getO2oBizType());
                labelDTO.setBusiness(businessType);
                businessTypeSet.addAll(Lists.newArrayList(businessType.split(",")));
                result.add(labelDTO);
            }

        }

        sortByBackstageConfigAndRn(result, subColumnDTOMap);

        if (CollectionUtils.isEmpty(result)) {
            return Lists.newArrayList(DEFAULT_RECOMMEND_TAB);
        }

        if (!isActivity) {
            LabelDTO labelDTO = new LabelDTO();
            labelDTO.setText("推荐");
            labelDTO.setId(0L);
            labelDTO.setBusiness(Joiner.on(",").join(businessTypeSet));
            result.add(0, labelDTO);
        }

        return result;

    }

    private boolean checkValid(SubColumnDTO subColumn) {
        return subColumn.getStatus().equals(ColumnStatus.NORMAL.getStatus())
                && CollectionUtils.isNotEmpty(subColumn.getSubColumnItemSetVOS())
                && Level2RecommendService.validate(subColumn.getStarTime(), subColumn.getEndTime());
    }


    private List<LabelDTO> sortByBackstageConfigAndRn(List<LabelDTO> labelRankDTOS, Map<Long, SubColumnDTO> subColumnDTOMap) {

        labelRankDTOS.sort((o1, o2) -> {
            Integer rn1 = getRank(subColumnDTOMap.get(o1.getId()));
            Integer rn2 = getRank(subColumnDTOMap.get(o2.getId()));
            return rn1 - rn2 == 0 ? o1.getRank() - o2.getRank() : rn1 - rn2;
        });
        return labelRankDTOS;
    }
    protected int getRank(SubColumnDTO subColumnDTO) {
        try {
            if (subColumnDTO == null ||
                    MapUtils.isEmpty(subColumnDTO.getMaterialDTOMap()) ||
                    subColumnDTO.getMaterialDTOMap().get("rank") == null ||
                    !StringUtils.isNumeric(subColumnDTO.getMaterialDTOMap().get("rank").getExtValue())) {
                return Integer.MAX_VALUE;
            }
            return Integer.valueOf(subColumnDTO.getMaterialDTOMap().get("rank").getExtValue());
        } catch (Exception e) {
            LOGGER.error("getRank error", e);
        }
        return Integer.MAX_VALUE;
    }


}
