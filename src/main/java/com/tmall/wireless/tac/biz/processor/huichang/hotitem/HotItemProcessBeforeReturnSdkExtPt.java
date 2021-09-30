package com.tmall.wireless.tac.biz.processor.huichang.hotitem;

import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;

import com.google.common.collect.Lists;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.filter.ItemProcessBeforeReturnSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextItem;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 爆款专区不需要库存过滤，但是需要把没有库存的沉淀
 * @author wangguohui
 */
@SdkExtension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
    useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
    scenario = HallScenarioConstant.HALL_SCENARIO_HOT_ITEM)
public class HotItemProcessBeforeReturnSdkExtPt extends Register implements ItemProcessBeforeReturnSdkExtPt {

    Logger logger = LoggerFactory.getLogger(HotItemProcessBeforeReturnSdkExtPt.class);

    @Override
    public SgFrameworkContextItem process(SgFrameworkContextItem sgFrameworkContextItem) {
        logger.error("--> HotItemProcessBeforeReturnSdkExtPt.start");
        SgFrameworkResponse<ItemEntityVO> entityVOSgFrameworkResponse = sgFrameworkContextItem
            .getEntityVOSgFrameworkResponse();

        List<ItemEntityVO> itemAndContentList = entityVOSgFrameworkResponse.getItemAndContentList();

        if (CollectionUtils.isEmpty(itemAndContentList)) {
            return sgFrameworkContextItem;
        }

        List<ItemEntityVO> finalItemAndContentList = Lists.newArrayList();
        //售罄的商品列表
        List<ItemEntityVO> sellOutItemAndContentList = Lists.newArrayList();

        for (ItemEntityVO entityVO : itemAndContentList) {
            if(canBuy(entityVO)){
                finalItemAndContentList.add(entityVO);
            }else {
                sellOutItemAndContentList.add(entityVO);
            }
        }
        int totalSize = itemAndContentList.size();
        int canBuySize = finalItemAndContentList.size();
        int sellOutSize = sellOutItemAndContentList.size();
        List<Long> sellOutItemIds = sellOutItemAndContentList.stream().map(ItemEntityVO::getItemId).collect(
            Collectors.toList());
        logger.error("爆款专区库存沉底结果.totalSize:{}, canBuySize:{}, sellOutSize:{}, sellOutItemIds:{}",
            totalSize, canBuySize, sellOutSize, JSON.toJSONString(sellOutItemIds));
        finalItemAndContentList.addAll(sellOutItemAndContentList);
        entityVOSgFrameworkResponse.setItemAndContentList(finalItemAndContentList);

        return sgFrameworkContextItem;

    }

    private boolean canBuy(ItemEntityVO item) {

        Boolean canBuy = item.getBoolean("canBuy");
        Boolean sellOut = item.getBoolean("sellOut");

        return (canBuy == null || canBuy) && (sellOut == null || !sellOut);
    }

}
