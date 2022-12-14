package com.tmall.wireless.tac.biz.processor.brandclub.fp;

import com.google.common.collect.Lists;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.vo.ContentFilterSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ContentVO;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextContent;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.RenderContentTypeEnum;
import com.tmall.wireless.tac.biz.processor.huichang.inventory.InventoryEntranceModule.InventoryEntranceModuleContentFilterSdkExtPt;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


@SdkExtension(
        bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.BRAND_CLUB_FP
)
public class BrandClubFirstPageContentFilterSdkExtPt extends Register implements ContentFilterSdkExtPt {
    Logger logger = LoggerFactory.getLogger(InventoryEntranceModuleContentFilterSdkExtPt.class);


    @Override
    public SgFrameworkResponse<ContentVO> process(SgFrameworkContextContent sgFrameworkContextContent) {
        logger.info("BrandClubFirstPageContentFilterSdkExtPt.start");
        SgFrameworkResponse<ContentVO> entityVOSgFrameworkResponse = sgFrameworkContextContent.getContentVOSgFrameworkResponse();

        List<ContentVO> itemAndContentList = entityVOSgFrameworkResponse.getItemAndContentList();

        if (CollectionUtils.isEmpty(itemAndContentList)) {
            logger.info("BrandClubFirstPageContentFilterSdkExtPt.itemAndContentList is empty");
            return entityVOSgFrameworkResponse;
        }
        List<ContentVO> itemAndContentListAfterFilter = Lists.newArrayList();
        // ????????????????????????
        for (ContentVO contentVO : itemAndContentList) {
            Long contentId = contentVO.getLong("contentId");
            List<ItemEntityVO> itemEntityVOListFilter = filterItem(contentVO);
            if(itemEntityVOListFilter.size() >= getMinItemSizePerContent()) {
                contentVO.put("items", itemEntityVOListFilter);
                itemAndContentListAfterFilter.add(contentVO);
            } else {
                logger.error("BrandClubFirstPageContentFilterSdkExtPt,contentFilterByItemCount,contentId:{}", contentId);
            }
        }
        entityVOSgFrameworkResponse.setItemAndContentList(itemAndContentListAfterFilter);
        return entityVOSgFrameworkResponse;
    }

    private List<ItemEntityVO> filterItem(ContentVO contentVO) {

        List<ItemEntityVO> itemEntityVOList = (List<ItemEntityVO>) contentVO.get("items");
        Long contentId = contentVO.getLong("contentId");

        // ??????????????????????????????????????????????????????????????????????????????
        if (RenderContentTypeEnum.bangdanContent.getType().equals(contentVO.getString("contentType"))) {
            logger.warn("BrandClubFirstPageContentFilterSdkExtPt, processbangdan");
            boolean canShow = false;
            if (itemEntityVOList.size() < 3) {
                logger.error("BrandClubFirstPageContentFilterSdkExtPt,itemEntityVOList.size() < 3,{}", contentId);
                return Lists.newArrayList();
            }
            for (int i = 0; i < 3; i ++) {
                ItemEntityVO itemEntityVO = itemEntityVOList.get(i);
                if (canBuy(itemEntityVO) && !itemInfoError(itemEntityVO)) {
                    return itemEntityVOList;
                }
            }
            logger.error("BrandClubFirstPageContentFilterSdkExtPt,itemFilterByInventory,{}", contentId);
        }

        List<ItemEntityVO> itemEntityVOListFilter = Lists.newArrayList();
        for(ItemEntityVO itemEntityVO: itemEntityVOList) {
            if (itemEntityVO == null) {
                logger.error("BrandClubFirstPageContentFilterSdkExtPt,itemFilterIsNull,contentId:{}", contentId);
            }else if (!canBuy(itemEntityVO)) {
                logger.error("BrandClubFirstPageContentFilterSdkExtPt,itemFilterCanBuy,contentId:{},itemId:{}", contentId, itemEntityVO.getString("itemId"));
            } else if (itemInfoError(itemEntityVO)) {
                logger.error("BrandClubFirstPageContentFilterSdkExtPt,itemFilterItemInfoError,contentId:{},itemId:{}", contentId, itemEntityVO.getString("itemId"));
            } else {
                itemEntityVOListFilter.add(itemEntityVO);
            }
        }
        return itemEntityVOListFilter;
    }

    private int getMinItemSizePerContent() {
        return 6;
    }

    /**
     * ??????????????????
     * 1???????????????
     * 2???????????????????????????
     * @return
     */

    private boolean canBuy(ItemEntityVO item) {

        Boolean canBuy = item.getBoolean("canBuy");
        Boolean sellOut = item.getBoolean("sellOut");

        return (canBuy == null || canBuy) && (sellOut == null || !sellOut);
    }

    private boolean itemInfoError(ItemEntityVO item) {
        return StringUtils.isEmpty(item.getString("shortTitle"))
                || StringUtils.isEmpty(item.getString("itemImg"));
    }
}
