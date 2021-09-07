package com.tmall.wireless.tac.biz.processor.huichang.inventory.InventoryEntranceModule;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.vo.ContentFilterSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ContentVO;
import com.tmall.tcls.gs.sdk.framework.model.ErrorCode;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextContent;
import com.tmall.tcls.gs.sdk.framework.suport.LogUtil;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 过滤掉 售罄和不卖的商品，进而过滤掉没挂品的场景
 */
@SdkExtension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
    useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
    scenario = HallScenarioConstant.HALL_SCENARIO_SCENARIO_INVENTORY_ENTRANCE_MODULE)
public class InventoryEntranceModuleContentFilterSdkExtPt extends Register implements ContentFilterSdkExtPt {

    Logger logger = LoggerFactory.getLogger(InventoryEntranceModuleContentFilterSdkExtPt.class);

    @Override
    public SgFrameworkResponse<ContentVO> process(SgFrameworkContextContent sgFrameworkContextContent) {
        logger.info("InventoryEntranceModuleContentFilterSdkExtPt.start");
        SgFrameworkResponse<ContentVO> entityVOSgFrameworkResponse = sgFrameworkContextContent.getContentVOSgFrameworkResponse();

        List<ContentVO> itemAndContentList = entityVOSgFrameworkResponse.getItemAndContentList();

        if (CollectionUtils.isEmpty(itemAndContentList)) {
            return entityVOSgFrameworkResponse;
        }
        List<ContentVO> itemAndContentListAfterFilter = Lists.newArrayList();
        // 过滤场景下的商品
        List<String> sceneSetIdList = new ArrayList<>();
        for (ContentVO contentVO : itemAndContentList) {
            String contentSetId = String.valueOf(contentVO.get("contentSetId"));
            if(sceneSetIdList.contains(contentSetId)){
                logger.info("InventoryEntranceModuleContentInfoQuerySdkExtPt.重复场景过滤,场景id:{}" , contentSetId);
                continue;
            }else {
                sceneSetIdList.add(contentSetId);
            }
            List<ItemEntityVO> itemEntityVOList = (List<ItemEntityVO>) contentVO.get("items");
            List<ItemEntityVO> itemEntityVOListFilter = Lists.newArrayList();
            for(ItemEntityVO itemEntityVO: itemEntityVOList) {
                if (itemEntityVO != null) {
                    if (isValid(itemEntityVO)) {
                        itemEntityVOListFilter.add(itemEntityVO);
                    } else {
                        LogUtil.errorCode(ErrorCode.ITEM_FILTER_BY_CAN_BUY, itemEntityVO.getString("itemId"));
                        logger.info("商品" + itemEntityVO.getString("itemId") + "被过滤");
                        HadesLogUtil.stream("inventoryEntranceModule")
                            .kv("InventoryEntranceModuleContentFilterSdkExtPt", "process")
                            .kv("商品过滤", "商品" + itemEntityVO.getString("itemId"))
                            .info();
                    }
                }else {
                    logger.info("itemEntityVO为null");
                    HadesLogUtil.stream("inventoryEntranceModule")
                        .kv("InventoryEntranceModuleContentFilterSdkExtPt", "process")
                        .kv("itemEntityVO","为null")
                        .error();
                }
            }
            if(itemEntityVOListFilter.size() > 0) {
                contentVO.put("items", itemEntityVOListFilter);
                itemAndContentListAfterFilter.add(contentVO);
            } else {
                logger.info("场景" + String.valueOf(contentVO.get("contentId"))+"被过滤");
                HadesLogUtil.stream("inventoryEntranceModule")
                    .kv("InventoryEntranceModuleContentFilterSdkExtPt", "process")
                    .kv("场景过滤", "场景" + String.valueOf(contentVO.get("contentId")))
                    .info();
            }
        }
        entityVOSgFrameworkResponse.setItemAndContentList(itemAndContentListAfterFilter);
        return entityVOSgFrameworkResponse;
    }

    /**
     * 是否是有效的
     * 1、要有库存
     * 2、要有商品图片字段
     * @return
     */
    private boolean isValid(ItemEntityVO item){
        if(canBuy(item) && !itemInfoError(item)){
            return true;
        }else {
            return false;
        }
    }


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
