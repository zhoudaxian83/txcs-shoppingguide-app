package com.tmall.wireless.tac.biz.processor.wzt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
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

    private static final String LOG_PREFIX = "WuZheTianItemInfoPostProcessorExtPt-";

    @Autowired
    RpcSpi rpcSpi;

    @Autowired
    TacLogger tacLogger;

    @Override
    public Response<ItemInfoPostProcessorResp> process(SgFrameworkContextItem sgFrameworkContextItem) {
        tacLogger.info(
            "ItemInfoPostProcessorExtPt扩展点测试=" + JSON.toJSONString(sgFrameworkContextItem.getItemMetaInfo()));
        tacLogger.info("getEntityVOSgFrameworkResponse=" + JSON
            .toJSONString(sgFrameworkContextItem.getEntityVOSgFrameworkResponse()));
        tacLogger.info(
            "getItemEntityOriginDataDTO=" + JSON.toJSONString(sgFrameworkContextItem.getItemEntityOriginDataDTO()));
        tacLogger.info(
            "getItemInfoGroupResponseMap=" + JSON.toJSONString(sgFrameworkContextItem.getItemInfoGroupResponseMap()));
        tacLogger.info("getItemMetaInfo=" + JSON.toJSONString(sgFrameworkContextItem.getItemMetaInfo()));
        JSONObject getItemLimitResult = this.getItemLimitResult(this.buildGetItemLimitResult(sgFrameworkContextItem));
        if (getItemLimitResult != null) {

        } else {
            tacLogger.info(LOG_PREFIX + "获取限购数据为空");
        }
        ItemInfoPostProcessorResp itemInfoPostProcessorResp = new ItemInfoPostProcessorResp();
        return Response.success(itemInfoPostProcessorResp);
    }

    private Map<String, Object> buildGetItemLimitResult(SgFrameworkContextItem sgFrameworkContextItem) {
        //获取itemId和sku

        List<Long> items = sgFrameworkContextItem.getItemEntityOriginDataDTO().getResult().stream().map(item -> {
            return item.getItemId();
        }).collect(Collectors.toList());
        Long userId = MapUtil.getLongWithDefault(sgFrameworkContextItem.getRequestParams(), "userId", 0L);
        Map<String, Object> paramsValue = new HashMap<>(16);
        Map paramMap = Maps.newHashMap();
        paramMap.put("userId", userId);
        List<Map> skuList = Lists.newArrayList();
        Map skuMap = Maps.newHashMap();
        skuMap.put("skuId", 4637368768647L);
        skuMap.put("itemId", 643897236869L);
        skuList.add(skuMap);
        paramMap.put("itemIdList", skuList);
        paramsValue.put("itemLimitInfoQuery", paramMap);
        return paramsValue;
    }

    private JSONObject getItemLimitResult(Map<String, Object> paramsValue) {
        Object o;
        try {
            o = rpcSpi.invokeHsf("todayCrazyLimit", paramsValue);
            JSONObject jsonObject = (JSONObject)JSON.toJSON(o);
            if ((Boolean)jsonObject.get("success")) {
                JSONObject itemLimitResult = (JSONObject)jsonObject.get("limitInfo");
                return itemLimitResult;
            } else {
                tacLogger.warn(LOG_PREFIX + "限购信息查询结果为空");
                return null;
            }
        } catch (Exception e) {
            tacLogger.error(LOG_PREFIX + "获取限购信息异常", e);
            e.printStackTrace();
        }
        return null;

    }

}
