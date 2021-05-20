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
import com.tmall.txcs.gs.framework.model.ItemGroup;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.txcs.gs.framework.model.meta.ItemGroupMetaInfo;
import com.tmall.txcs.gs.framework.support.itemInfo.ItemInfoGroupResponse;
import com.tmall.txcs.gs.model.Response;
import com.tmall.txcs.gs.model.spi.model.ItemInfoBySourceDTO;
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
        Map<ItemGroup, ItemInfoGroupResponse> itemGroupItemInfoGroupResponseMap = sgFrameworkContextItem
            .getItemInfoGroupResponseMap();
        sgFrameworkContextItem.getItemEntityOriginDataDTO().getResult().forEach(itemEntity -> {
            ItemGroup itemGroup = new ItemGroup(itemEntity.getBizType(), itemEntity.getO2oType());
            tacLogger.info(
                "打印验证入参，itemGroup=" + JSON.toJSONString(itemGroup));

            JSONObject jsonObject = (JSONObject)JSONObject.toJSON(itemGroupItemInfoGroupResponseMap.get(itemGroup).getValue().get(itemGroup));
                //.getValue().get(itemGroup).getItemInfos());

            tacLogger.info(
                "打印验证jsonObject=" + JSON.toJSONString(jsonObject));

            tacLogger.info(
                "打印验证captain=" + JSON.toJSONString(jsonObject.get("captain")));

        });

        tacLogger.info(
            "ItemInfoPostProcessorExtPt扩展点测试sgFrameworkContextItem=" + JSON.toJSONString(sgFrameworkContextItem));
        Map<String, Object> userParams = Maps.newConcurrentMap();
        userParams.put("userParams-test-1", "userParams-test-1");
        sgFrameworkContextItem.setUserParams(userParams);

        JSONObject getItemLimitResult = this.getItemLimitResult(this.buildGetItemLimitResult(sgFrameworkContextItem));
        if (getItemLimitResult != null) {

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
        skuMap2.put("skuId", 0L);
        skuMap2.put("itemId", 605659349023L);
        skuList.add(skuMap2);

        paramMap.put("itemIdList", skuList);
        paramsValue.put("itemLimitInfoQuery", paramMap);
        tacLogger.warn(LOG_PREFIX + "限购入参paramsValue:" + JSON.toJSONString(paramsValue));
        return paramsValue;
    }

    private JSONObject getItemLimitResult(Map<String, Object> paramsValue) {
        Object o;
        try {
            o = rpcSpi.invokeHsf("todayCrazyLimit", paramsValue);
            JSONObject jsonObject = (JSONObject)JSON.toJSON(o);
            if ((Boolean)jsonObject.get("success")) {
                JSONObject itemLimitResult = (JSONObject)jsonObject.get("limitInfo");
                tacLogger.warn(LOG_PREFIX + "限购数据打印o:" + JSON.toJSONString(o));
                tacLogger.warn(LOG_PREFIX + "限购数据打印jsonObject：" + JSON.toJSONString(jsonObject));
                tacLogger.warn(LOG_PREFIX + "限购数据打印itemLimitResult：" + JSON.toJSONString(itemLimitResult));
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
