package com.tmall.wireless.tac.biz.processor.huichang.hotitem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;

import com.google.common.collect.Lists;
import com.taobao.eagleeye.EagleEye;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.tmall.aselfcaptain.util.StackTraceUtil;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.biz.uti.MapUtil;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.filter.ItemProcessBeforeReturnSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextItem;
import com.tmall.tmallwireless.tac.spi.context.SPIResult;
import com.tmall.wireless.store.spi.tair.TairSpi;
import com.tmall.wireless.tac.biz.processor.config.SxlSwitch;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallCommonAldConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 爆款专区不需要库存过滤，但是需要把没有库存的沉淀
 * @author wangguohui
 */
@SdkExtension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
    useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
    scenario = HallScenarioConstant.HALL_SCENARIO_HOT_ITEM)
public class HotItemProcessBeforeReturnSdkExtPt extends Register implements ItemProcessBeforeReturnSdkExtPt {


    private static final AtomicLong backUpCounter = new AtomicLong(0);

    public static final String HOT_ITEM_TAIR_USER_NAME = "b6241830ca7f4b9d";
    public static final int HOT_ITEM_NAME_SPACE = 184;

    Logger logger = LoggerFactory.getLogger(HotItemProcessBeforeReturnSdkExtPt.class);

    @Autowired
    TairSpi tairSpi;

    @Override
    public SgFrameworkContextItem process(SgFrameworkContextItem sgFrameworkContextItem) {
        SgFrameworkResponse<ItemEntityVO> entityVOSgFrameworkResponse = sgFrameworkContextItem
            .getEntityVOSgFrameworkResponse();
        String aldCurrentResId = "0";
        List<ItemEntityVO> finalItemAndContentList = new ArrayList<>();
        try{
            Context context = sgFrameworkContextItem.getTacContext();
            RequestContext4Ald requestContext4Ald = (RequestContext4Ald)context;
            Map<String, Object> aldContext = requestContext4Ald.getAldContext();
            aldCurrentResId = MapUtil.getStringWithDefault(aldContext, HallCommonAldConstant.ALD_CURRENT_RES_ID, "0");
            List<ItemEntityVO> itemAndContentList = entityVOSgFrameworkResponse.getItemAndContentList();

            try{
                if(SxlSwitch.openHotItemDouble){
                    logger.error("HotItemProcessBeforeReturnSdkExtPt.openHotItemDouble");
                    List<ItemEntityVO> itemEntityVOList = dealDouble(sgFrameworkContextItem, itemAndContentList);
                    itemAndContentList= itemEntityVOList;
                }
            }catch (Exception e){
                logger.error("HotItemProcessBeforeReturnSdkExtPt.openHotItemDouble.exception", e);
            }

            Map<String, Object> userParams = sgFrameworkContextItem.getUserParams();
            String customItemSetId = MapUtil.getStringWithDefault(userParams, "customItemSetId", "0");

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

            //打底逻辑
            if(CollectionUtils.isNotEmpty(finalItemAndContentList)){
                String tairKey = buildKey(customItemSetId);
                writeBottomData(tairKey, finalItemAndContentList);
                HadesLogUtil.stream("HotItemModuleHandler|HotItemProcessBeforeReturnSdkExtPt|" + isEagleEyeTest() + "|success|")
                    .kv("resourceId", aldCurrentResId)
                    .error();
            }else {
                HadesLogUtil.stream("HotItemModuleHandler|HotItemProcessBeforeReturnSdkExtPt|" + isEagleEyeTest() + "|bottom")
                    .kv("resourceId", aldCurrentResId)
                    .error();
                String tairKey = buildKey(customItemSetId);
                finalItemAndContentList = readeBottomData(tairKey);
            }
        }catch (Exception e){
            HadesLogUtil.stream("HotItemModuleHandler|HotItemProcessBeforeReturnSdkExtPt|" + isEagleEyeTest() + "|exception")
                .kv("resourceId", aldCurrentResId)
                .kv("errorMsg", StackTraceUtil.stackTrace(e))
                .error();
        }
        entityVOSgFrameworkResponse.setItemAndContentList(finalItemAndContentList);
        return sgFrameworkContextItem;

    }


    //读打底数据
    private List<ItemEntityVO> readeBottomData(String key){
        SPIResult<Result<DataEntry>> resultSPIResult = tairSpi.get(HOT_ITEM_TAIR_USER_NAME, HOT_ITEM_NAME_SPACE, key);
        Object o = Optional.ofNullable(resultSPIResult).map(SPIResult::getData).map(Result::getValue).map(DataEntry::getValue).orElse(null);
        if(o != null){
            List<ItemEntityVO> itemEntityVOList = JSON.parseArray(o.toString(), ItemEntityVO.class);
            return itemEntityVOList;
        }else {
            return new ArrayList<>();
        }
    }

    private void writeBottomData(String key, List<ItemEntityVO> itemEntityVOList){
        if(isItemBackup(backUpCounter)){
            tairSpi.put(HOT_ITEM_TAIR_USER_NAME, HOT_ITEM_NAME_SPACE,  key, JSON.toJSONString(itemEntityVOList));
        }
    }

    private String buildKey(String customItemSetId) {
        StringBuilder sb = new StringBuilder();
        sb.append("hall_bottom_hot_").append(customItemSetId);
        return sb.toString();

    }

    /**
     * 是否是压测请求
     *
     * @return
     */
    protected boolean isEagleEyeTest() {
        try {
            String ut = EagleEye.getUserData("t");
            if ("1".equals(ut) || "2".equals(ut)) {
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("method isEagleEyeTest error.", e);
            return false;
        }
    }

    public boolean isItemBackup(AtomicLong backUpCounter) {
        long count = backUpCounter.getAndAdd(1);
        return count % SxlSwitch.backUpHotItem == 0;
    }

    //特殊处理逻辑。放大倍数的去拿了tpp的结果，需要这一步，按照库存过滤，来选出符合每个行业固定数量的商品
    public List<ItemEntityVO> dealDouble(SgFrameworkContextItem sgFrameworkContextItem, List<ItemEntityVO> itemAndContentList){
        List<ItemEntityVO> dealItemEntityVOList = new ArrayList<>();
        RequestContext4Ald requestContext4Ald = (RequestContext4Ald)(sgFrameworkContextItem.getTacContext());
        Map<String, Object> aldContext = requestContext4Ald.getAldContext();
        Object aldStaticData = aldContext.get(HallCommonAldConstant.STATIC_SCHEDULE_DATA);

        if(aldStaticData != null){
            Object aldStaticDataMap = requestContext4Ald.getParams().get("aldStaticDataMap");
            Map<Long, Map<String, Object>> staticDataMap = (Map<Long, Map<String, Object>>)aldStaticDataMap;
            fillItem(staticDataMap, itemAndContentList, dealItemEntityVOList);
        }
        return dealItemEntityVOList;
    }


    private static void fillItem(Map<Long, Map<String, Object>> staticDataMap, List<ItemEntityVO> itemAndContentList, List<ItemEntityVO> dealItemEntityVOList){
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
                List<ItemEntityVO> list = new ArrayList<>();
                list.add(entityVO);
                industryItemEntityVOMap.put(industryId, list);
            }else {
                itemEntityVOS.add(entityVO);
                industryItemEntityVOMap.put(industryId, itemEntityVOS);
            }
        }

        for (Map.Entry<String, List<ItemEntityVO>> entry : industryItemEntityVOMap.entrySet()) {
            String industryId = entry.getKey();
            Integer showNum = industryItemShowNumMap.get(industryId);
            if(showNum == null){
                continue;
            }
            List<ItemEntityVO> canBuyItemEntityVO = new ArrayList<>();
            List<ItemEntityVO> selloutItemEntityVO = new ArrayList<>();
            List<ItemEntityVO> itemEntityVOList = entry.getValue();
            for(ItemEntityVO itemEntityVO : itemEntityVOList){
                if(canBuyItemEntityVO.size() == showNum){
                    continue;
                }
                if(canBuy(itemEntityVO)){
                    canBuyItemEntityVO.add(itemEntityVO);
                }else {
                    selloutItemEntityVO.add(itemEntityVO);
                }
            }
            //如果处理完，不满足要求的数量，需要随便拿相应的数量商品补上
            if(canBuyItemEntityVO.size() != showNum ){
                Integer needFillSize  = showNum - canBuyItemEntityVO.size();
                List<ItemEntityVO> itemEntityVOList1 = selloutItemEntityVO.subList(0, needFillSize);
                canBuyItemEntityVO.addAll(itemEntityVOList1);
            }
            dealItemEntityVOList.addAll(canBuyItemEntityVO);
        }
    }

    //Object industryId = map.get("industryId");
    //            Object showNum = map.get("showNum");
    //            Object itemId = map.get("contentId");
    private static void convert(Map<Long, Map<String, Object>> staticDataMap,
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
                List<Long> list = new ArrayList<>();
                list.add(itemId);
                industryItemIdMap.put(industryIdStr, list);
            }else {
                itemIdList.add(itemId);
                industryItemIdMap.put(industryIdStr, itemIdList);
            }
        }
    }


    private static boolean canBuy(ItemEntityVO item) {

        Boolean canBuy = item.getBoolean("canBuy");
        Boolean sellOut = item.getBoolean("sellOut");

        return (canBuy == null || canBuy) && (sellOut == null || !sellOut);
    }

    //Object industryId = map.get("industryId");
    //            Object showNum = map.get("showNum");
    //            Object itemId = map.get("contentId");
    public static void main(String[] args) {
        //Map<Long, Map<String, Object>> staticDataMap,
        // List<ItemEntityVO> itemAndContentList,
        // List<ItemEntityVO> dealItemEntityVOList
        Map<Long, Map<String, Object>> staticDataMap = new HashMap<>();

        List<ItemEntityVO> itemAndContentList = new ArrayList<>();
        List<ItemEntityVO> dealItemEntityVOList= new ArrayList<>();

        Map<String, Object> map1 = new HashMap<>();
        map1.put("industryId", "A");
        map1.put("showNum", "2");
        map1.put("contentId", "1");
        Map<String, Object> map2 = new HashMap<>();
        map2.put("industryId", "A");
        map2.put("showNum", "2");
        map2.put("contentId", "2");

        Map<String, Object> map3 = new HashMap<>();
        map3.put("industryId", "A");
        map3.put("showNum", "2");
        map3.put("contentId", "3");

        Map<String, Object> map4 = new HashMap<>();
        map4.put("industryId", "B");
        map4.put("showNum", "1");
        map4.put("contentId", "4");

        Map<String, Object> map5 = new HashMap<>();
        map5.put("industryId", "B");
        map5.put("showNum", "1");
        map5.put("contentId", "5");

        Map<String, Object> map6 = new HashMap<>();
        map6.put("industryId", "C");
        map6.put("showNum", "1");
        map6.put("contentId", "6");

        Map<String, Object> map7 = new HashMap<>();
        map7.put("industryId", "C");
        map7.put("showNum", "1");
        map7.put("contentId", "7");

        staticDataMap.put(1L, map1);
        staticDataMap.put(2L, map2);
        staticDataMap.put(3L, map3);
        staticDataMap.put(4L, map4);
        staticDataMap.put(5L, map5);
        staticDataMap.put(6L, map6);
        staticDataMap.put(7L, map7);


        ItemEntityVO entityVO1 = new ItemEntityVO();
        entityVO1.setItemId(1L);
        entityVO1.put("canBuy", true);
        entityVO1.put("sellOut", false);
        entityVO1.put("industryId", "A");

        ItemEntityVO entityVO2 = new ItemEntityVO();
        entityVO2.setItemId(2L);
        entityVO2.put("canBuy", false);
        entityVO2.put("sellOut", false);
        entityVO2.put("industryId", "A");

        ItemEntityVO entityVO3 = new ItemEntityVO();
        entityVO3.setItemId(3L);
        entityVO3.put("canBuy", false);
        entityVO3.put("sellOut", false);
        entityVO3.put("industryId", "A");

        ItemEntityVO entityVO4 = new ItemEntityVO();
        entityVO4.setItemId(4L);
        entityVO4.put("canBuy", true);
        entityVO4.put("sellOut", false);
        entityVO4.put("industryId", "B");

        ItemEntityVO entityVO5 = new ItemEntityVO();
        entityVO5.setItemId(5L);
        entityVO5.put("canBuy", true);
        entityVO5.put("sellOut", false);
        entityVO5.put("industryId", "B");


        ItemEntityVO entityVO6 = new ItemEntityVO();
        entityVO6.setItemId(6L);
        entityVO6.put("canBuy", false);
        entityVO6.put("sellOut", false);
        entityVO6.put("industryId", "C");


        ItemEntityVO entityVO7 = new ItemEntityVO();
        entityVO7.setItemId(7L);
        entityVO7.put("canBuy", false);
        entityVO7.put("sellOut", false);
        entityVO7.put("industryId", "C");

        itemAndContentList.add(entityVO1);
        itemAndContentList.add(entityVO2);
        itemAndContentList.add(entityVO3);
        itemAndContentList.add(entityVO4);
        itemAndContentList.add(entityVO5);
        itemAndContentList.add(entityVO6);
        itemAndContentList.add(entityVO7);



        fillItem(staticDataMap, itemAndContentList, dealItemEntityVOList);
        System.out.println(JSON.toJSONString(dealItemEntityVOList));
    }

}
