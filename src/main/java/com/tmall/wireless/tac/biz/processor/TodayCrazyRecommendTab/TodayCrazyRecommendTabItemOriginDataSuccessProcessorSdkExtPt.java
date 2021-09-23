package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab;

import com.alibaba.fastjson.JSON;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataSuccessProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.OriginDataProcessRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextItem;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.springframework.beans.factory.annotation.Autowired;

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

    public OriginDataDTO<ItemEntity> addIsTopList(OriginDataDTO<ItemEntity> originDataDTO, SgFrameworkContextItem sgFrameworkContextItem) {
        tacLogger.info("sgFrameworkContextItem_" + JSON.toJSONString(sgFrameworkContextItem));
        boolean isFirstPage = (boolean) sgFrameworkContextItem.getUserParams().get("isFirstPage");

        //如果是第一页则追加入参置顶
        if (isFirstPage) {

        }
        Map<String, Object> objectMap = sgFrameworkContextItem.getUserParams();
        tacLogger.info("objectMap_" + JSON.toJSONString(objectMap));
        //如果是第一页去除重复且置顶，非第一页只去重
        List<ItemEntity> itemEntities = originDataDTO.getResult();
//        //去除重复
//        itemEntities.removeIf()
//        //遍历构建
//        ItemEntity itemEntity = new ItemEntity();
//        itemEntities.add(itemEntity);
        originDataDTO.setResult(itemEntities);
        return originDataDTO;
    }
}
