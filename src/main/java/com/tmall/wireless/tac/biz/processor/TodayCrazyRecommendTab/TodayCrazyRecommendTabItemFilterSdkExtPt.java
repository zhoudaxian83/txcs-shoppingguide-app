package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.filter.ItemFilterRequest;
import com.tmall.tcls.gs.sdk.framework.extensions.item.filter.ItemFilterSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ErrorCode;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkItemService;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.constant.CommonConstant;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.wzt.model.ItemLimitDTO;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;

/**
 * Created from template by 罗俊冲 on 2021-09-25 13:40:47.
 */

@SdkExtension(
        bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.TODAY_CRAZY_RECOMMEND_TAB
)
public class TodayCrazyRecommendTabItemFilterSdkExtPt extends Register implements ItemFilterSdkExtPt {
    @Autowired
    TacLoggerImpl tacLogger;

    private static final Logger LOGGER = LoggerFactory.getLogger(ShoppingguideSdkItemService.class);
    public static final List<String> CHECK_FIELD = Lists.newArrayList(
            "itemImg"
    );

    @Override
    public SgFrameworkResponse<ItemEntityVO> process(ItemFilterRequest itemFilterRequest) {


        SgFrameworkResponse<ItemEntityVO> entityVOSgFrameworkResponse = itemFilterRequest.getEntityVOSgFrameworkResponse();

        List<ItemEntityVO> itemAndContentList = entityVOSgFrameworkResponse.getItemAndContentList();

        if (CollectionUtils.isEmpty(itemAndContentList)) {
            return entityVOSgFrameworkResponse;
        }
        List<ItemEntityVO> itemAndContentListAfterFilter = Lists.newArrayList();

        for (ItemEntityVO entityVO : itemAndContentList) {
            if (entityVO != null) {
                if (!this.canBuy(entityVO)||!this.noLimitBuy(entityVO)) {
                    LOGGER.error("itemFilter,{}, itemId:{}", ErrorCode.ITEM_FILTER_BY_CAN_BUY, entityVO.getString("itemId"));
                } else {
                    if (checkField(entityVO)) {
                        itemAndContentListAfterFilter.add(entityVO);
                    }
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
        for (String field : checkField) {
            if (Objects.isNull(entityVO.get(field))) {
                LOGGER.error("itemFilter,{}, itemId:{},field:{}", ErrorCode.ITEM_FILTER_BY_FIELD_ERROR, entityVO.getString("itemId"), field);
                return false;
            }
        }
        return true;
    }

    protected List<String> getFieldList() {
        return CHECK_FIELD;
    }


    private boolean canBuy(ItemEntityVO item) {
        Boolean canBuy = item.getBoolean("canBuy");
        Boolean sellOut = item.getBoolean("sellOut");
        return (canBuy == null || canBuy) && (sellOut == null || !sellOut);
    }

    private boolean noLimitBuy(ItemEntityVO itemEntityVO) {
        if(CommonConstant.LIMIT_BUY_SWITCH){
            return true;
        }
        tacLogger.info("noLimitBuy_1" + JSON.toJSONString(itemEntityVO));
        ItemLimitDTO itemLimitDTO = (ItemLimitDTO) itemEntityVO.get("itemLimit");
        tacLogger.info("itemLimitDTO_" + JSON.toJSONString(itemLimitDTO));
        if (itemLimitDTO == null || itemLimitDTO.getSkuId() == null) {
            return true;
        }
        //兼容只有总限购或个人限购的情况
        boolean totalLimit = itemLimitDTO.getUsedCount() != null && itemLimitDTO.getTotalLimit() != null;
        boolean userLimit = itemLimitDTO.getUserUsedCount() != null && itemLimitDTO.getUserLimit() != null;
        if (!totalLimit && !userLimit) {
            return true;
        }
        if (totalLimit && userLimit) {
            //当已售数量大于等于总限制数，个人限制数量大于等于个人限购数沉底处理
            return itemLimitDTO.getUsedCount() < itemLimitDTO.getTotalLimit()
                    && itemLimitDTO.getUserUsedCount() < itemLimitDTO.getUserLimit();
        }
        if (totalLimit) {
            return itemLimitDTO.getUsedCount() < itemLimitDTO.getTotalLimit();
        } else {
            return itemLimitDTO.getUserUsedCount() < itemLimitDTO.getUserLimit();
        }
    }


}
