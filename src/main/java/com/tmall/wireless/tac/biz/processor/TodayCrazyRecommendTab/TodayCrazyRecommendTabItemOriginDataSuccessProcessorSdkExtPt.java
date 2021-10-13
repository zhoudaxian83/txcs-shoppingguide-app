package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataSuccessProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.OriginDataProcessRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextItem;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.extensions.failprocessor.ItemFailProcessorRequest;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.constant.CommonConstant;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.constant.TabTypeEnum;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.service.TodayCrazyTairCacheService;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.util.CommonUtil;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

/**
 * Created from template by 罗俊冲 on 2021-09-23 14:14:31.
 * TPP获取成功后
 */

@SdkExtension(
        bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.TODAY_CRAZY_RECOMMEND_TAB
)
public class TodayCrazyRecommendTabItemOriginDataSuccessProcessorSdkExtPt extends Register implements ItemOriginDataSuccessProcessorSdkExtPt {
    @Autowired
    TacLoggerImpl tacLogger;

    @Autowired
    TodayCrazyTairCacheService todayCrazyTairCacheService;


    @Override
    public OriginDataDTO<ItemEntity> process(OriginDataProcessRequest originDataProcessRequest) {
        // 1,融合置顶商品；2，商品去重处理  直接把入参中的置顶商品置顶，每次查询进行去重处理
        OriginDataDTO<ItemEntity> originDataDTO = originDataProcessRequest.getItemEntityOriginDataDTO();
        List<ItemEntity> itemEntities = originDataDTO.getResult();
        //保存tairKey和item关联供后面逻辑使用
        originDataProcessRequest.getSgFrameworkContextItem().getUserParams().put(CommonConstant.ITEM_ID_AND_CACHE_KEYS, CommonUtil.buildItemIdAndCacheKey(itemEntities));
        ItemFailProcessorRequest itemFailProcessorRequest = JSON.parseObject(JSON.toJSONString(originDataProcessRequest), ItemFailProcessorRequest.class);
        //tpp请求成功写入缓存，供失败打底使用
        todayCrazyTairCacheService.process(itemFailProcessorRequest);
        boolean isFirstPage = (boolean) originDataProcessRequest.getSgFrameworkContextItem().getUserParams().get("isFirstPage");
        String tabType = MapUtil.getStringWithDefault(originDataProcessRequest.getSgFrameworkContextItem().getRequestParams(), "tabType", "");
        this.doTopItems(originDataDTO, originDataProcessRequest.getSgFrameworkContextItem(), isFirstPage);
        if (TabTypeEnum.TODAY_CHAO_SHENG.getType().equals(tabType)) {
            this.doItemSort(originDataDTO, originDataProcessRequest.getSgFrameworkContextItem(), isFirstPage);
        }
        return originDataDTO;
    }

    /**
     * 根据鸿雁传入置顶处理
     *
     * @param originDataDTO
     * @param sgFrameworkContextItem
     */
    public void doTopItems(OriginDataDTO<ItemEntity> originDataDTO, SgFrameworkContextItem sgFrameworkContextItem, boolean isFirstPage) {
        String topListStr = MapUtil.getStringWithDefault(sgFrameworkContextItem.getRequestParams(), "topList", "");
        List<String> topList = topListStr.equals("") ? Lists.newArrayList() : Arrays.asList(topListStr.split(","));
        //如果是第一页去除重复且置顶，非第一页只去重
        List<ItemEntity> itemEntities = originDataDTO.getResult();
        //todo 只有今日超省走双置顶逻辑，1，双中判断置顶有效期；2，只有第一页做置顶这个置顶逻辑；3每页走要进行置顶去重
        tacLogger.info("topList：" + JSON.toJSONString(topList));
        tacLogger.info("TPP返回数据条数：" + itemEntities.size());
        tacLogger.info("TPP返回数据itemEntities：" + JSON.toJSONString(itemEntities));
        //todo mock
        //itemEntities = this.mock();
        tacLogger.info("topList：" + JSON.toJSONString(topList));
        if (CollectionUtils.isEmpty(topList)) {
            originDataDTO.setResult(itemEntities);
            return;
        }
        itemEntities.removeIf(itemEntity -> topList.contains(String.valueOf(itemEntity.getItemId())));
        tacLogger.info("isFirstPage：" + isFirstPage);
        if (isFirstPage) {
            List<ItemEntity> topResultsItemEntityList = Lists.newArrayList();
            topList.forEach(itemId -> {
                ItemEntity itemEntity = new ItemEntity();
                itemEntity.setO2oType("B2C");
                itemEntity.setBizType("sm");
                itemEntity.setItemId(Long.valueOf(itemId));
                itemEntity.setTop(true);
                topResultsItemEntityList.add(itemEntity);
            });
            tacLogger.info("过滤后条数：" + itemEntities.size());
            topResultsItemEntityList.addAll(itemEntities);
            originDataDTO.setResult(topResultsItemEntityList);
        } else {
            originDataDTO.setResult(itemEntities);
        }
    }

    /**
     * 根据资源位置顶操作
     *
     * @param originDataDTO
     * @param sgFrameworkContextItem
     * @param isFirstPage
     */
    private void doItemSort(OriginDataDTO<ItemEntity> originDataDTO, SgFrameworkContextItem sgFrameworkContextItem, boolean isFirstPage) {
        List<ItemEntity> itemEntities = originDataDTO.getResult();
        Object o = todayCrazyTairCacheService.getSortItems();
        //只有首页进行置顶操作，但每一页需要去重操作
        if (isFirstPage) {

        }

    }


    private List<ItemEntity> mock() {
        String str = "[\n" +
                "\t{\n" +
                "\t\t\"bizType\": \"sm\",\n" +
                "\t\t\"brandId\": \"137887\",\n" +
                "\t\t\"cateId\": \"137887\",\n" +
                "\t\t\"itemId\": 582803559585,\n" +
                "\t\t\"itemUniqueId\": {\n" +
                "\t\t\t\"id\": 582803559585,\n" +
                "\t\t\t\"type\": \"B2C\"\n" +
                "\t\t},\n" +
                "\t\t\"o2oType\": \"B2C\",\n" +
                "\t\t\"rn\": 0,\n" +
                "\t\t\"top\": false,\n" +
                "\t\t\"track_point\": \"1007.37154.239449.0.null\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"bizType\": \"sm\",\n" +
                "\t\t\"brandId\": \"104391965\",\n" +
                "\t\t\"cateId\": \"104391965\",\n" +
                "\t\t\"itemId\": 529661830204,\n" +
                "\t\t\"itemUniqueId\": {\n" +
                "\t\t\t\"id\": 529661830204,\n" +
                "\t\t\t\"type\": \"B2C\"\n" +
                "\t\t},\n" +
                "\t\t\"o2oType\": \"B2C\",\n" +
                "\t\t\"rn\": 0,\n" +
                "\t\t\"top\": false,\n" +
                "\t\t\"track_point\": \"1007.37154.239449.0.null\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"bizType\": \"sm\",\n" +
                "\t\t\"brandId\": \"3485944\",\n" +
                "\t\t\"cateId\": \"3485944\",\n" +
                "\t\t\"itemId\": 574004235764,\n" +
                "\t\t\"itemUniqueId\": {\n" +
                "\t\t\t\"id\": 574004235764,\n" +
                "\t\t\t\"type\": \"B2C\"\n" +
                "\t\t},\n" +
                "\t\t\"o2oType\": \"B2C\",\n" +
                "\t\t\"rn\": 0,\n" +
                "\t\t\"top\": false,\n" +
                "\t\t\"track_point\": \"1007.37154.239449.0.null\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"bizType\": \"sm\",\n" +
                "\t\t\"brandId\": \"7606086\",\n" +
                "\t\t\"cateId\": \"7606086\",\n" +
                "\t\t\"itemId\": 12545040847,\n" +
                "\t\t\"itemUniqueId\": {\n" +
                "\t\t\t\"id\": 12545040847,\n" +
                "\t\t\t\"type\": \"B2C\"\n" +
                "\t\t},\n" +
                "\t\t\"o2oType\": \"B2C\",\n" +
                "\t\t\"rn\": 0,\n" +
                "\t\t\"top\": false,\n" +
                "\t\t\"track_point\": \"1007.37154.239449.0.null\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"bizType\": \"sm\",\n" +
                "\t\t\"brandId\": \"3801324\",\n" +
                "\t\t\"cateId\": \"3801324\",\n" +
                "\t\t\"itemId\": 525000115434,\n" +
                "\t\t\"itemUniqueId\": {\n" +
                "\t\t\t\"id\": 525000115434,\n" +
                "\t\t\t\"type\": \"B2C\"\n" +
                "\t\t},\n" +
                "\t\t\"o2oType\": \"B2C\",\n" +
                "\t\t\"rn\": 0,\n" +
                "\t\t\"top\": false,\n" +
                "\t\t\"track_point\": \"1007.37154.239449.0.null\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"bizType\": \"sm\",\n" +
                "\t\t\"brandId\": \"3250190\",\n" +
                "\t\t\"cateId\": \"3250190\",\n" +
                "\t\t\"itemId\": 40764741613,\n" +
                "\t\t\"itemUniqueId\": {\n" +
                "\t\t\t\"id\": 40764741613,\n" +
                "\t\t\t\"type\": \"B2C\"\n" +
                "\t\t},\n" +
                "\t\t\"o2oType\": \"B2C\",\n" +
                "\t\t\"rn\": 0,\n" +
                "\t\t\"top\": false,\n" +
                "\t\t\"track_point\": \"1007.37154.239449.0.null\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"bizType\": \"sm\",\n" +
                "\t\t\"brandId\": \"20328\",\n" +
                "\t\t\"cateId\": \"20328\",\n" +
                "\t\t\"itemId\": 605302254597,\n" +
                "\t\t\"itemUniqueId\": {\n" +
                "\t\t\t\"id\": 605302254597,\n" +
                "\t\t\t\"type\": \"B2C\"\n" +
                "\t\t},\n" +
                "\t\t\"o2oType\": \"B2C\",\n" +
                "\t\t\"rn\": 0,\n" +
                "\t\t\"top\": false,\n" +
                "\t\t\"track_point\": \"1007.37154.239449.0.null\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"bizType\": \"sm\",\n" +
                "\t\t\"brandId\": \"104391965\",\n" +
                "\t\t\"cateId\": \"104391965\",\n" +
                "\t\t\"itemId\": 39943512189,\n" +
                "\t\t\"itemUniqueId\": {\n" +
                "\t\t\t\"id\": 39943512189,\n" +
                "\t\t\t\"type\": \"B2C\"\n" +
                "\t\t},\n" +
                "\t\t\"o2oType\": \"B2C\",\n" +
                "\t\t\"rn\": 0,\n" +
                "\t\t\"top\": false,\n" +
                "\t\t\"track_point\": \"1007.37154.239449.0.null\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"bizType\": \"sm\",\n" +
                "\t\t\"brandId\": \"3801324\",\n" +
                "\t\t\"cateId\": \"3801324\",\n" +
                "\t\t\"itemId\": 15699771744,\n" +
                "\t\t\"itemUniqueId\": {\n" +
                "\t\t\t\"id\": 15699771744,\n" +
                "\t\t\t\"type\": \"B2C\"\n" +
                "\t\t},\n" +
                "\t\t\"o2oType\": \"B2C\",\n" +
                "\t\t\"rn\": 0,\n" +
                "\t\t\"top\": false,\n" +
                "\t\t\"track_point\": \"1007.37154.239449.0.null\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"bizType\": \"sm\",\n" +
                "\t\t\"brandId\": \"147280915\",\n" +
                "\t\t\"cateId\": \"147280915\",\n" +
                "\t\t\"itemId\": 571438384496,\n" +
                "\t\t\"itemUniqueId\": {\n" +
                "\t\t\t\"id\": 571438384496,\n" +
                "\t\t\t\"type\": \"B2C\"\n" +
                "\t\t},\n" +
                "\t\t\"o2oType\": \"B2C\",\n" +
                "\t\t\"rn\": 0,\n" +
                "\t\t\"top\": false,\n" +
                "\t\t\"track_point\": \"1007.37154.239449.0.null\"\n" +
                "\t}\n" +
                "]";
        return JSON.parseArray(str, ItemEntity.class);
    }
}
