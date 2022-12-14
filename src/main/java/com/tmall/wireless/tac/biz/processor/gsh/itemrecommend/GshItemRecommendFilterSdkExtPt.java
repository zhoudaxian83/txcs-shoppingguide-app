package com.tmall.wireless.tac.biz.processor.gsh.itemrecommend;

import java.util.List;
import java.util.Objects;

import com.google.common.collect.Lists;
import com.tcls.mkt.atmosphere.model.response.ItemPromotionResp;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.filter.ItemFilterRequest;
import com.tmall.tcls.gs.sdk.framework.extensions.item.filter.ItemFilterSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ErrorCode;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.config.SupermarketHallSwitch;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import org.apache.commons.collections.CollectionUtils;

/**
 * @author wangguohui
 * 爆款专区不过滤售罄的商品，只返回是否售罄
 */
@SdkExtension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
    useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_GSH,
    scenario = HallScenarioConstant.HALL_SCENARIO_SCENARIO_GSH_ITEM_RECOMMEND)
public class GshItemRecommendFilterSdkExtPt extends Register implements ItemFilterSdkExtPt {

    public static final List<String> CHECK_FIELD = Lists.newArrayList("itemImg");

    @Override
    public SgFrameworkResponse<ItemEntityVO> process(ItemFilterRequest itemFilterRequest) {
        SgFrameworkResponse<ItemEntityVO> entityVOSgFrameworkResponse = itemFilterRequest
            .getEntityVOSgFrameworkResponse();

        List<ItemEntityVO> itemAndContentList = entityVOSgFrameworkResponse.getItemAndContentList();

        if (CollectionUtils.isEmpty(itemAndContentList)) {
            return entityVOSgFrameworkResponse;
        }

        List<ItemEntityVO> itemAndContentListAfterFilter = Lists.newArrayList();

        for (ItemEntityVO entityVO : itemAndContentList) {
            if (SupermarketHallSwitch.openGshPriceFilter && !checkPrice(entityVO)) {
                continue;
            }
            if (entityVO != null) {
                if (checkField(entityVO)) {
                    itemAndContentListAfterFilter.add(entityVO);
                }
            }
        }

        entityVOSgFrameworkResponse.setItemAndContentList(itemAndContentListAfterFilter);

        return entityVOSgFrameworkResponse;
    }

    protected boolean checkField(ItemEntityVO entityVO) {
        List<String> checkField = getFieldList();
        if (CollectionUtils.isEmpty(checkField)) {
            return true;
        }
        //if (itemDTO != null && itemDTO.getItemPromotionResp() != null
        //    && itemDTO.getItemPromotionResp().getUnifyPrice() != null
        //    && itemDTO.getItemPromotionResp().getUnifyPrice().getShowPrice() != null
        //    && itemDTO.getItemPromotionResp().getUnifyPrice().getShowPrice().getCent() < 0) {
        //    continue;
        //}
        for (String field : checkField) {
            if (Objects.isNull(entityVO.get(field))) {
                LOGGER.error("itemFilter,{}, itemId:{},field:{}", ErrorCode.ITEM_FILTER_BY_FIELD_ERROR, entityVO.getString("itemId"), field);
                return false;
            }
        }
        return true;
    }

    private boolean checkPrice(ItemEntityVO entityVO) {
        try {
            if (entityVO.containsKey("itemPromotionResp")) {
                ItemPromotionResp resp = (ItemPromotionResp)entityVO.get("itemPromotionResp");
                if (resp != null
                    && resp.getUnifyPrice() != null
                    && resp.getUnifyPrice().getShowPrice() != null
                    && resp.getUnifyPrice().getShowPrice().getCent() < 0) {
                    LOGGER.error("checkPrice is minus " + entityVO.getItemId());
                    return false;
                }
            }
        } catch (Exception e) {
            return true;
        }
        return true;
    }

    protected List<String> getFieldList() {
        return CHECK_FIELD;
    }

}
