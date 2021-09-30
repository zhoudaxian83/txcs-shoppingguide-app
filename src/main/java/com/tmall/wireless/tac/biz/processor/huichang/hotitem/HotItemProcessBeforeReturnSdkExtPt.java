package com.tmall.wireless.tac.biz.processor.huichang.hotitem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;

import com.google.common.collect.Lists;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.filter.ItemProcessBeforeReturnSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextItem;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallCommonAldConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
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

        //特殊处理逻辑。放大倍数的去哪了tpp的结果，需要这一步，按照库存过滤，来选出符合每个行业固定数量的商品
        RequestContext4Ald requestContext4Ald = (RequestContext4Ald)(sgFrameworkContextItem.getTacContext());
        Map<String, Object> aldContext = requestContext4Ald.getAldContext();
        Object aldStaticData = aldContext.get(HallCommonAldConstant.STATIC_SCHEDULE_DATA);
        if(aldStaticData != null){
            Object aldStaticDataMap = requestContext4Ald.getParams().get("aldStaticDataMap");
            Map<Long, Map<String, Object>> staticDataMap = (Map<Long, Map<String, Object>>)aldStaticDataMap;
            //行业id：每个行业下面的商品list
            Map<String, List<Long>> industryItemIdMap = new HashMap<>();
            //行业id：行业下面要求的商品数量
            Map<String, Integer> industryItemShowNumMap = new HashMap<>();
            //商品id：商品id所属的行业id
            Map<Long, String> itemOfIndustryMap = new HashMap<>();
            //转化
            convert(staticDataMap, industryItemIdMap, industryItemShowNumMap, itemOfIndustryMap);

            //给tpp返回的商品分组；
            Map<String, List<ItemEntityVO>> industryItemEntityVOMap = new HashMap<>();
            for (ItemEntityVO entityVO : itemAndContentList) {
                Long itemId = entityVO.getItemId();
                String industryId = itemOfIndustryMap.get(itemId);
                if(StringUtils.isEmpty(industryId)) {
                    continue;
                }
                List<ItemEntityVO> itemEntityVOS = industryItemEntityVOMap.get(industryId);
                if(CollectionUtils.isEmpty(itemEntityVOS)){
                    industryItemEntityVOMap.put(industryId, Arrays.asList(entityVO));
                }else {
                    itemEntityVOS.add(entityVO);
                    industryItemEntityVOMap.put(industryId, itemEntityVOS);
                }
            }
            List<ItemEntityVO> dealItemEntityVOList = new ArrayList<>();
            for (Map.Entry<String, List<ItemEntityVO>> entry : industryItemEntityVOMap.entrySet()) {
                String industryId = entry.getKey();
                List<ItemEntityVO> itemEntityVOList = entry.getValue();

            }

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


    //Object industryId = map.get("industryId");
    //            Object showNum = map.get("showNum");
    //            Object itemId = map.get("contentId");
    private void convert(Map<Long, Map<String, Object>> staticDataMap,
        Map<String, List<Long>> industryItemIdMap,
        Map<String, Integer> industryItemShowNum,
        Map<Long, String> itemOfIndustryMap){
        for (Map.Entry<Long, Map<String, Object>> entry : staticDataMap.entrySet()) {
            Long itemId = entry.getKey();
            Map<String, Object> value = entry.getValue();
            Object industryId = value.get("industryId");
            Object showNum = value.get("showNum");
            if(industryId == null || showNum == null){
                continue;
            }
            String industryIdStr = String.valueOf(industryId);
            industryItemShowNum.put(industryIdStr, Integer.valueOf(String.valueOf(showNum)));
            itemOfIndustryMap.put(itemId, industryIdStr);

            List<Long> itemIdList = industryItemIdMap.get(industryIdStr);
            if(CollectionUtils.isEmpty(itemIdList)){
                List<Long> longs = Arrays.asList(itemId);
                industryItemIdMap.put(industryIdStr, longs);
            }else {
                itemIdList.add(itemId);
                industryItemIdMap.put(industryIdStr, itemIdList);
            }
        }
    }


    private boolean canBuy(ItemEntityVO item) {

        Boolean canBuy = item.getBoolean("canBuy");
        Boolean sellOut = item.getBoolean("sellOut");

        return (canBuy == null || canBuy) && (sellOut == null || !sellOut);
    }

}
