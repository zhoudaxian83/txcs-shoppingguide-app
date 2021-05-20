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
import com.tmall.txcs.gs.framework.model.EntityVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
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
        Map<String, Object> stringObjectMap = new HashMap<>(16);
        stringObjectMap.put("post-test-1", "post-test-1");
        tacLogger.info(
            "ItemInfoPostProcessorExtPt扩展点测试sgFrameworkContextItem=" + JSON.toJSONString(sgFrameworkContextItem));
        JSONObject getItemLimitResult = this.getItemLimitResult(this.buildGetItemLimitResult(sgFrameworkContextItem));
        stringObjectMap.put("post-test-getItemLimitResult", getItemLimitResult);
        SgFrameworkResponse<EntityVO> entityVOSgFrameworkResponse = new SgFrameworkResponse<EntityVO>();
        entityVOSgFrameworkResponse.setExtInfos(stringObjectMap);
        sgFrameworkContextItem.setEntityVOSgFrameworkResponse(entityVOSgFrameworkResponse);
        if (getItemLimitResult != null) {
            tacLogger.warn(LOG_PREFIX + "限购数据打印" + JSON.toJSONString(getItemLimitResult));
        } else {
            tacLogger.warn(LOG_PREFIX + "获取限购数据为空");
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

        Map skuMap2 = Maps.newHashMap();
        //skuMap.put("skuId", 4637368768647L);
        skuMap.put("itemId", 605659349023L);
        skuList.add(skuMap2);

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
