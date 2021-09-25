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
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.model.ItemLimitDTO;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
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

        tacLogger.info("过滤节点开始");
        SgFrameworkResponse<ItemEntityVO> entityVOSgFrameworkResponse = itemFilterRequest.getEntityVOSgFrameworkResponse();

        List<ItemEntityVO> itemAndContentList = entityVOSgFrameworkResponse.getItemAndContentList();

        if (CollectionUtils.isEmpty(itemAndContentList)) {
            return entityVOSgFrameworkResponse;
        }
        List<ItemEntityVO> itemAndContentListAfterFilter = Lists.newArrayList();

        for (ItemEntityVO entityVO : itemAndContentList) {
            if (entityVO != null) {
                if (!this.canBuy(entityVO) || !this.noLimitBuy(entityVO)) {
                    tacLogger.info("过滤——1" + entityVO.getString("itemId"));
                    LOGGER.error("itemFilter,{}, itemId:{}", ErrorCode.ITEM_FILTER_BY_CAN_BUY, entityVO.getString("itemId"));
                } else {
                    tacLogger.info("过滤——2" + entityVO.getString("itemId"));
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

    public boolean noLimitBuy(ItemEntityVO itemEntityVO) {
        if (!CommonConstant.LIMIT_BUY_SWITCH) {
            return true;
        }
        ItemLimitDTO itemLimitDTO = (ItemLimitDTO) itemEntityVO.get("itemLimit");
        if (itemLimitDTO == null) {
            return true;
        }
        if (571438384496L == itemLimitDTO.getSkuId()) {
            itemLimitDTO.setUsedCount(10001);
        }
        return itemLimitDTO.getUsedCount() < itemLimitDTO.getTotalLimit()
                && itemLimitDTO.getUserUsedCount() < itemLimitDTO.getUserLimit();
    }


}
