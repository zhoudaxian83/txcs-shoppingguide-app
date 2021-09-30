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
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

    @Override
    public OriginDataDTO<ItemEntity> process(OriginDataProcessRequest originDataProcessRequest) {
        // 1,融合置顶商品；2，商品去重处理  直接把入参中的置顶商品置顶，每次查询进行去重处理
        OriginDataDTO<ItemEntity> originDataDTO = originDataProcessRequest.getItemEntityOriginDataDTO();
        this.addIsTopList(originDataDTO, originDataProcessRequest.getSgFrameworkContextItem());
        return originDataDTO;
    }

    public void addIsTopList(OriginDataDTO<ItemEntity> originDataDTO, SgFrameworkContextItem sgFrameworkContextItem) {
        String topListStr = MapUtil.getStringWithDefault(sgFrameworkContextItem.getRequestParams(), "topList", "");
        List<String> topList = topListStr.equals("") ? Lists.newArrayList() : Arrays.asList(topListStr.split(","));
        boolean isFirstPage = (boolean) sgFrameworkContextItem.getUserParams().get("isFirstPage");
        Map<String, Object> objectMap = sgFrameworkContextItem.getUserParams();
        //如果是第一页去除重复且置顶，非第一页只去重
        List<ItemEntity> itemEntities = originDataDTO.getResult();
        tacLogger.info("topList："+JSON.toJSONString(topList));
        tacLogger.info("TPP返回数据条数："+itemEntities.size());
        tacLogger.info("TPP返回数据结果："+JSON.toJSONString(itemEntities));
        //todo mock
        //itemEntities = this.mock();
        tacLogger.info("topList："+JSON.toJSONString(topList));
        if (CollectionUtils.isEmpty(topList)) {
            originDataDTO.setResult(itemEntities);
            return;
        }
        itemEntities.removeIf(itemEntity -> topList.contains(String.valueOf(itemEntity.getItemId())));
        tacLogger.info("isFirstPage："+isFirstPage);
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
            tacLogger.info("过滤后条数："+itemEntities.size());
            topResultsItemEntityList.addAll(itemEntities);
            originDataDTO.setResult(topResultsItemEntityList);
        } else {
            originDataDTO.setResult(itemEntities);
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
