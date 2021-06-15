package com.tmall.wireless.tac.biz.processor.wzt;

import com.ali.com.google.common.base.Joiner;
import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.framework.extensions.origindata.request.ItemOriginDataRequestExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.txcs.gs.model.spi.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
//        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
//        scenario = ScenarioConstantApp.WU_ZHE_TIAN)
@Service
public class WuZheTianItemOriginDataRequestExtPt {
    private static final Long APP_ID = 21431L;

//    @Autowired
//    TacLogger tacLogger;
//
//    @Override
//    public RecommendRequest process(SgFrameworkContextItem sgFrameworkContextItem) {
//        OriginDataDTO<ItemEntity> originDataDTO = sgFrameworkContextItem.getItemEntityOriginDataDTO();
//        List<Long> itemIds = originDataDTO.getResult().stream().map(ItemEntity::getItemId).collect(Collectors.toList());
//        Long userId = MapUtil.getLongWithDefault(sgFrameworkContextItem.getRequestParams(), "userId", 0L);
//        RecommendRequest RecommendRequest = new RecommendRequest();
//        RecommendRequest recommendRequest = new RecommendRequest();
//        recommendRequest.setLogResult(true);
//        recommendRequest.setUserId(userId);
//        recommendRequest.setAppId(APP_ID);
//        Map<String, String> params = Maps.newHashMap();
//        params.put("userItemIdList", Joiner.on(",").join(itemIds));
//        recommendRequest.setParams(params);
//        tacLogger.info("tpp扩展点参数：" + JSON.toJSONString(recommendRequest));
//        return RecommendRequest;
//    }
}
