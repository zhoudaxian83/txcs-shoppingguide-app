package com.tmall.wireless.tac.biz.processor.wzt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.txcs.gs.framework.extensions.itemdatapost.ItemInfoPostProcessorExtPt;
import com.tmall.txcs.gs.framework.extensions.itemdatapost.ItemInfoPostProcessorResp;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.model.Response;
import com.tmall.txcs.gs.spi.recommend.RpcSpi;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: luoJunChong
 * @Date: 2021/5/18 18:57
 * description:
 */
@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.WU_ZHE_TIAN)
@Service
public class WuZheTianItemInfoPostProcessorExtPt implements ItemInfoPostProcessorExtPt {

    @Autowired
    RpcSpi rpcSpi;

    @Autowired
    TacLogger tacLogger;

    @Override
    public Response<ItemInfoPostProcessorResp> process(SgFrameworkContextItem sgFrameworkContextItem) {
        tacLogger.info("ItemInfoPostProcessorExtPt扩展点测试=" + JSON.toJSONString(sgFrameworkContextItem));

        Map<String, Object> paramsValue = new HashMap<>(16);
        Map paramMap = Maps.newHashMap();
        paramsValue.put("itemLimitInfoQuery", paramMap);
        paramMap.put("userId", 1681359525L);
        List<Map> skuList = Lists.newArrayList();
        Map skuMap = Maps.newHashMap();
        skuMap.put("skuId", 4637368768647L);
        skuMap.put("itemId", 643897236869L);
        skuList.add(skuMap);
        paramMap.put("itemIdList", skuList);

        try {
            tacLogger.info("测试返回结果begin");
            Object o = rpcSpi.invokeHsf("todayCrazyLimit", paramsValue);
            // java中Object转JsonObject
            JSONObject jsonObject = (JSONObject)JSON.toJSON(o);
            tacLogger.info("1-测试返回结果=" + JSON.toJSONString(o));
            tacLogger.info("2-测试返回结果=" + JSON.toJSONString(jsonObject));
        } catch (Exception e) {
            tacLogger.error("测试返回结果-异常", e);
            e.printStackTrace();
        }
        ItemInfoPostProcessorResp itemInfoPostProcessorResp = new ItemInfoPostProcessorResp();
        return Response.success(itemInfoPostProcessorResp);
    }

}
