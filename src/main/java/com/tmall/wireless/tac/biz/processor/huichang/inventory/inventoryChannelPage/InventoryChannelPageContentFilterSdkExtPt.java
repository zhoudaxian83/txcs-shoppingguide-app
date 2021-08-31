package com.tmall.wireless.tac.biz.processor.huichang.inventory.inventoryChannelPage;

import java.util.List;

import com.google.common.collect.Lists;
import com.tmall.aselfcaptain.util.StackTraceUtil;
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
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 过滤掉 售罄和不卖的商品，进而过滤掉没挂品的场景
 */
@SdkExtension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
        useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
        scenario = HallScenarioConstant.HALL_SCENARIO_SCENARIO_INVENTORY_CHANNEL_PAGE)
public class InventoryChannelPageContentFilterSdkExtPt extends Register implements ContentFilterSdkExtPt {
    @Autowired
    TacLogger tacLogger;

    @Override
    public SgFrameworkResponse<ContentVO> process(SgFrameworkContextContent sgFrameworkContextContent) {
        tacLogger.debug("扩展点InventoryChannelPageContentFilterSdkExtPt");
        SgFrameworkResponse<ContentVO> entityVOSgFrameworkResponse = sgFrameworkContextContent.getContentVOSgFrameworkResponse();

        List<ContentVO> itemAndContentList = entityVOSgFrameworkResponse.getItemAndContentList();

        if (CollectionUtils.isEmpty(itemAndContentList)) {
            return entityVOSgFrameworkResponse;
        }
        List<ContentVO> itemAndContentListAfterFilter = Lists.newArrayList();
        // 过滤场景下的商品
        for (ContentVO contentVO : itemAndContentList) {
            List<ItemEntityVO> itemEntityVOList = (List<ItemEntityVO>) contentVO.get("items");
            List<ItemEntityVO> itemEntityVOListFilter = Lists.newArrayList();
            for(ItemEntityVO itemEntityVO: itemEntityVOList) {
                if (itemEntityVO != null) {
                    if (canBuy(itemEntityVO)) {
                        itemEntityVOListFilter.add(itemEntityVO);
                    } else {
                        LogUtil.errorCode(ErrorCode.ITEM_FILTER_BY_CAN_BUY, itemEntityVO.getString("itemId"));
                        tacLogger.debug("商品" + itemEntityVO.getString("itemId") + "被过滤");
                        HadesLogUtil.stream("InventoryChannelPage")
                                .kv("InventoryChannelPageContentFilterSdkExtPt", "process")
                                .kv("商品过滤", "商品" + itemEntityVO.getString("itemId"))
                                .info();
                    }
                }else {
                    tacLogger.debug("itemEntityVO为null");
                    HadesLogUtil.stream("InventoryChannelPage")
                            .kv("InventoryChannelPageContentFilterSdkExtPt", "process")
                            .kv("itemEntityVO","为null")
                            .error();
                }
            }
            if(itemEntityVOListFilter.size() > 0) {
                contentVO.put("items", itemEntityVOListFilter);
                itemAndContentListAfterFilter.add(contentVO);
            } else {
                tacLogger.debug("场景" + String.valueOf(contentVO.get("contentId"))+"被过滤");
                HadesLogUtil.stream("InventoryChannelPage")
                        .kv("InventoryChannelPageContentFilterSdkExtPt", "process")
                        .kv("场景过滤", "场景" + String.valueOf(contentVO.get("contentId")))
                        .info();
            }
        }
        entityVOSgFrameworkResponse.setItemAndContentList(itemAndContentListAfterFilter);
        return entityVOSgFrameworkResponse;
    }

    private boolean canBuy(ItemEntityVO item) {

        Boolean canBuy = item.getBoolean("canBuy");
        Boolean sellOut = item.getBoolean("sellOut");

        return (canBuy == null || canBuy) && (sellOut == null || !sellOut);
    }
}
