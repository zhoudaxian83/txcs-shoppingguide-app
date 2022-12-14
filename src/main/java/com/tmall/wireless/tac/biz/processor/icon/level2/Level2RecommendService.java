package com.tmall.wireless.tac.biz.processor.icon.level2;

import com.alibaba.common.lang.StringUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.aself.shoppingguide.client.cat.model.LabelDTO;
import com.tmall.aselfcommon.model.column.ColumnStatus;
import com.tmall.aselfcommon.model.column.MainColumnDTO;
import com.tmall.aselfcommon.model.column.MaterialDTO;
import com.tmall.promotiontag.client.service.unify.TmallCrowdUnifyReadSimplifyCacheClient;
import com.tmall.promotiontag.common.result.ResultBase;
import com.tmall.promotiontag.crowd.common.AppInfo;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.framework.model.context.*;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkContentService;
import com.tmall.wireless.tac.biz.processor.common.PackageNameKey;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.icon.level2.ext.IconLevel2ContentInfoQuerySdkExtPt;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class Level2RecommendService {
    private static String newCustomerTag01 = "PTN_10295005";

    private static final AppInfo defaultAppInfo = new AppInfo("supermarket");

    @Autowired
    private TmallCrowdUnifyReadSimplifyCacheClient tmallCrowdUnifyReadSimplifyCacheClient;

    public static final String labelTypeActivity = "activity";
    public static final String labelTypeNormal = "normal";

    @Autowired
    ShoppingguideSdkContentService shoppingguideSdkContentService;

    Logger LOGGER = LoggerFactory.getLogger(Level2RecommendService.class);

    public static String level2RequestKey = "level2Request";

    public Flowable<List<LabelDTO>> recommend(Level2Request level2Request, Context context) {
        BizScenario b = BizScenario.valueOf(
                ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
                ScenarioConstantApp.LOC_TYPE_B2C,
                ScenarioConstantApp.ICON_CONTENT_LEVEL2
        );
        b.addProducePackage(PackageNameKey.OLD_RECOMMEND);
        List<LabelDTO> empty = Lists.newArrayList();
        context.put(level2RequestKey, level2Request);
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
        }).defaultIfEmpty(empty);

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


                if (!checkMainColumnValid(mainColumnDTO, sgFrameworkContextContent)) {
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

    private boolean checkMainColumnValid(MainColumnDTO mainColumnDTO, SgFrameworkContextContent sgFrameworkContextContent) {
        // ?????????????????????????????????
//        if (mainColumnDTO.getId().equals(2256L)) {
//            return true;
//        }
        return ColumnStatus.NORMAL.getStatus().equals(mainColumnDTO.getStatus())
                && validate(mainColumnDTO.getStartDate(), mainColumnDTO.getEndDate())
                && newCustomerTabCheck(mainColumnDTO, sgFrameworkContextContent);
    }

    public boolean hasTag(String tagId, long buyerId) {
        if(StringUtil.isEmpty(tagId) || buyerId <= 0) {
            return false;
        }

        try {
            ResultBase<Boolean> result = tmallCrowdUnifyReadSimplifyCacheClient.isMatch(buyerId, tagId, defaultAppInfo);
            if(result.isSuccess() && result.getValue()) {
                return true;
            }
        } catch (Exception e) {
            LOGGER.error("check tag error, userId:" + buyerId + ",tagId:" + tagId, e);
        }
        return false;
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
    private boolean newCustomerTabCheck(MainColumnDTO mainColumnDTO, SgFrameworkContextContent sgFrameworkContextContent) {
        boolean newCustomerTab = isNewCustomerTab(mainColumnDTO);

        // ??????????????????tab ??????????????????
        if (!newCustomerTab) {
            return true;
        }

        // ????????????????????? ??????false
//        if (!GrayChecker.checkGray(ICON_NEW_CUSTOMER_PRICE)) {
//            return false;
//        }

        // ????????????????????????????????????
        Long userId = Optional.of(sgFrameworkContextContent).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getUserDO).map(UserDO::getUserId).orElse(0L);

        return isNewCustomer(userId);

    }

    private boolean isNewCustomer(Long userId) {
        return hasTag(newCustomerTag01, userId);
    }


    public static boolean isNewCustomerTab(MainColumnDTO column) {
        MaterialDTO materialDTO = column.getMaterialDTOMap().get("isNewCustomer");
        if (materialDTO == null) {
            return false;
        }

        return StringUtils.equals("1", materialDTO.getExtValue());

    }

}
