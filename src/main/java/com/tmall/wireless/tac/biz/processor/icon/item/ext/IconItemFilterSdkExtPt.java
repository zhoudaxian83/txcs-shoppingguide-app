package com.tmall.wireless.tac.biz.processor.icon.item.ext;

import com.google.common.collect.Lists;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.framework.extensions.item.filter.ItemFilterRequest;
import com.tmall.tcls.gs.sdk.framework.extensions.item.filter.ItemFilterSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ErrorCode;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkItemService;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;


@SdkExtension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.ICON_ITEM
)
public class IconItemFilterSdkExtPt implements ItemFilterSdkExtPt {
    private static final Logger LOGGER = LoggerFactory.getLogger(IconItemFilterSdkExtPt.class);

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
        List<ItemEntityVO> itemAndContentListCheckFieldSuccess = Lists.newArrayList();

        for (ItemEntityVO entityVO : itemAndContentList) {
            if (entityVO != null) {
                if (!canBuy(entityVO)) {
                    LOGGER.error("itemFilter,{}, itemId:{}", ErrorCode.ITEM_FILTER_BY_CAN_BUY, entityVO.getString("itemId"));
                } else {
                    if (checkField(entityVO)) {
                        itemAndContentListAfterFilter.add(entityVO);
                    }
                }
            }
        }

        for (ItemEntityVO entityVO : itemAndContentList) {
            if (entityVO != null) {
                if (checkField(entityVO)) {
                    itemAndContentListCheckFieldSuccess.add(entityVO);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(itemAndContentListAfterFilter)) {
            entityVOSgFrameworkResponse.setItemAndContentList(itemAndContentListAfterFilter);
        } else {
            entityVOSgFrameworkResponse.setItemAndContentList(itemAndContentListCheckFieldSuccess);
        }

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
}
