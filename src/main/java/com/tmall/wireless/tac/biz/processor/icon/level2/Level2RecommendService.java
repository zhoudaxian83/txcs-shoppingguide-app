package com.tmall.wireless.tac.biz.processor.icon.level2;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.taobao.mtop.api.agent.MtopContext;
import com.tmall.aself.shoppingguide.client.cat.model.LabelDTO;
import com.tmall.aself.shoppingguide.client.cat.model.LabelRankDTO;
import com.tmall.aself.shoppingguide.client.loc.domain.AddressDTO;
import com.tmall.aselfcommon.model.column.ColumnStatus;
import com.tmall.aselfcommon.model.column.MainColumnDTO;
import com.tmall.aselfcommon.model.column.MaterialDTO;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.framework.model.context.*;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkContentService;
import com.tmall.wireless.tac.biz.processor.browsrec.BrowseRecommendScene;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.icon.level2.ext.IconLevel2ContentInfoQuerySdkExtPt;
import com.tmall.wireless.tac.biz.processor.icon.model.IconTabDTO;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class Level2RecommendService {


    public static final String labelTypeActivity = "activity";
    public static final String labelTypeNormal = "normal";

    @Autowired
    ShoppingguideSdkContentService shoppingguideSdkContentService;

    Logger LOGGER = LoggerFactory.getLogger(Level2RecommendService.class);

    public static String level2Request = "level2Request";

    Flowable<List<LabelDTO>> recommend(Level2Request level2Request, Context context) {
        BizScenario b = BizScenario.valueOf(
                ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
                ScenarioConstantApp.LOC_TYPE_B2C,
                ScenarioConstantApp.ICON_CONTENT_LEVEL2
        );
        List<LabelDTO> empty = Lists.newArrayList();
        context.put("level2Request", level2Request);
        return shoppingguideSdkContentService.recommendWitchContext(context, b)
        .map(sgFrameworkContextContent -> {
            List<ContentDTO> contentDTOList = sgFrameworkContextContent.getContentDTOList();
            OriginDataDTO<ContentEntity> contentEntityOriginDataDTO = sgFrameworkContextContent.getContentEntityOriginDataDTO();
            if (CollectionUtils.isEmpty(contentDTOList) || contentEntityOriginDataDTO == null || CollectionUtils.isEmpty(contentEntityOriginDataDTO.getResult())) {
                return empty;
            }
            return buildIconTabList(contentDTOList, contentEntityOriginDataDTO.getResult(), sgFrameworkContextContent);
        }).onErrorReturn(throwable -> {
            LOGGER.error("shoppingguideSdkContentService.recommendWitchContext error", throwable);
            return empty;
        });

    }

    private List<LabelDTO> buildIconTabList(List<ContentDTO> contentDTOList, List<ContentEntity> contentEntityList, SgFrameworkContextContent sgFrameworkContextContent) {

        List<LabelDTO> labelDTOS = Lists.newArrayList();
        Map<Long, Integer> contentId2BackstageRank = Maps.newHashMap();
        Map<Long, Integer> content2RankFromTpp = contentEntityList.stream().collect(Collectors.toMap(ContentEntity::getContentId, ContentEntity::getRn));
        Map<Long, ContentDTO> contentDTOMap = contentDTOList.stream().collect(Collectors.toMap(ContentDTO::getContentId, c -> c));
        contentEntityList.forEach(contentEntity -> {
            try {
                LabelDTO labelDTO = new LabelDTO();
                Object mainColumnDtoObj = Optional.ofNullable(contentDTOMap.get(contentEntity.getContentId()))
                        .map(contentDTO -> contentDTO.getContentInfo())
                        .map(info -> info.get(IconLevel2ContentInfoQuerySdkExtPt.MainColumnDTOKey))
                        .orElse(null);
                if (mainColumnDtoObj == null) {
                    LOGGER.error("mainColumnDTO from tair is null:{}", contentEntity.getContentId());
                    return;
                }
                MainColumnDTO mainColumnDTO = (MainColumnDTO) mainColumnDtoObj;


                if (!checkMainColumnValid(mainColumnDTO)) {
                    LOGGER.error("mainColumnDTO is offerLine:{}", mainColumnDTO.getId());
                    return;
                }

                labelDTO.setId(contentEntity.getContentId());
                labelDTO.setText(mainColumnDTO.getName());
                labelDTO.setBusiness(getBusinessType(
                        Optional.of(sgFrameworkContextContent).map(SgFrameworkContextContent::getCommonUserParams).map(CommonUserParams::getLocParams).orElse(null)
                        , mainColumnDTO.getO2oBizType()));

                MaterialDTO materialDTOIsActivity = mainColumnDTO.getMaterialDTOMap().get("isActivity");
                MaterialDTO materialDTOIconText = mainColumnDTO.getMaterialDTOMap().get("iconText");
                MaterialDTO materialDTORank = mainColumnDTO.getMaterialDTOMap().get("rank");


                Integer rankFromBackStage = materialDTORank != null && StringUtils.isNumeric(materialDTORank.getExtValue())
                        ? Integer.valueOf(materialDTORank.getExtValue()) : null;

                boolean isActivity = materialDTOIsActivity != null && "1".equals(materialDTOIsActivity.getExtValue());
                labelDTO.setType(isActivity ? labelTypeActivity : labelTypeNormal);
                labelDTO.setActText(materialDTOIconText == null ? null : materialDTOIconText.getExtValue());

                contentId2BackstageRank.put(labelDTO.getId(), rankFromBackStage);
                labelDTOS.add(labelDTO);
            } catch (Exception e) {
                LOGGER.error("process one label error");
            }
        });

        labelDTOS.sort((o1, o2) -> {
            try {
                return rankByBackStageRank(o1, o2, contentId2BackstageRank, content2RankFromTpp);
            } catch (Exception e) {
                LOGGER.error("labelDTOS.sort error", e);
            }
            return 0;
        });

        return labelDTOS;
    }

    private Integer rankByBackStageRank(LabelDTO o1, LabelDTO o2, Map<Long, Integer> contentId2BackstageRank, Map<Long, Integer> content2RankFromTpp) {
        if (contentId2BackstageRank.get(o1.getId()) == null &&
                contentId2BackstageRank.get(o2.getId()) != null) {
            return 1;
        }

        if (contentId2BackstageRank.get(o1.getId()) != null &&
                contentId2BackstageRank.get(o2.getId()) == null) {
            return -1;
        }

        if (contentId2BackstageRank.get(o1.getId()) != null &&
                contentId2BackstageRank.get(o2.getId()) != null) {
            return contentId2BackstageRank.get(o1.getId()) - contentId2BackstageRank.get(o2.getId());
        }

        return content2RankFromTpp.get(o1.getId()) - content2RankFromTpp.get(o2.getId());

    }



    protected String getBusinessType(LocParams locParams, String o2oBizType) {
        return BusinessTypeUtil.processType(locParams, o2oBizType);
    }

    private boolean checkMainColumnValid(MainColumnDTO mainColumnDTO) {
        // 测试用，上线之前要修复
//        if (mainColumnDTO.getId().equals(2256L)) {
//            return true;
//        }
        return ColumnStatus.NORMAL.getStatus().equals(mainColumnDTO.getStatus())
                && validate(mainColumnDTO.getStartDate(), mainColumnDTO.getEndDate())
                && newCustomerTabCheck(mainColumnDTO);
    }

    static public boolean validate(Date start, Date end) {
        if (start != null && start.after(new Date())) {
            return false;
        }
        if (end != null && end.before(new Date())) {
            return false;
        }
        return true;
    }
    private boolean newCustomerTabCheck(MainColumnDTO mainColumnDTO) {
        boolean newCustomerTab = isNewCustomerTab(mainColumnDTO);

        // 不是新人优惠tab 直接返回成功
        if (!newCustomerTab) {
            return true;
        }

        return true;

        // 新人开关关掉了 返回false
//        if (!GrayChecker.checkGray(ICON_NEW_CUSTOMER_PRICE)) {
//            return false;
//        }

        // 检查用户是否属于新人人群
//        return tmallCustomerChecker.isNewCustomer(MtopContext.getUserId());
    }


    public static boolean isNewCustomerTab(MainColumnDTO column) {
        MaterialDTO materialDTO = column.getMaterialDTOMap().get("isNewCustomer");
        if (materialDTO == null) {
            return false;
        }

        return StringUtils.equals("1", materialDTO.getExtValue());

    }

}
