package com.tmall.wireless.tac.biz.processor.wzt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.google.common.collect.Maps;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.extensions.itemdatapost.ItemInfoPostProcessorExtPt;
import com.tmall.txcs.gs.framework.extensions.itemdatapost.ItemInfoPostProcessorResp;
import com.tmall.txcs.gs.framework.model.ItemGroup;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.framework.support.itemInfo.ItemInfoGroupResponse;
import com.tmall.txcs.gs.model.Response;
import com.tmall.txcs.gs.spi.recommend.RpcSpi;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.wzt.model.convert.ItemDTO;
import com.tmall.wireless.tac.biz.processor.wzt.model.convert.ItemInfoDTO;
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
            "ItemInfoPostProcessorExtPt扩展点测试sgFrameworkContextItem=" + JSON.toJSONString(sgFrameworkContextItem));
        Map<String, Object> userParams = Maps.newConcurrentMap();
        userParams.put("userParams-test-1", "userParams-test-1");
        JSONObject getItemLimitResult = this.getItemLimitResult(this.buildGetItemLimitResult(sgFrameworkContextItem));
        if (getItemLimitResult != null) {
            userParams.put("getItemLimitResult", getItemLimitResult);
        } else {
            tacLogger.warn(LOG_PREFIX + "获取限购数据为空");
        }
        sgFrameworkContextItem.setUserParams(userParams);
        ItemInfoPostProcessorResp itemInfoPostProcessorResp = new ItemInfoPostProcessorResp();
        return Response.success(itemInfoPostProcessorResp);
    }

    private Map<String, Object> buildGetItemLimitResult(SgFrameworkContextItem sgFrameworkContextItem) {
        Long userId = MapUtil.getLongWithDefault(sgFrameworkContextItem.getRequestParams(), "userId", 0L);
        Map<ItemGroup, ItemInfoGroupResponse> itemGroupItemInfoGroupResponseMap = sgFrameworkContextItem
            .getItemInfoGroupResponseMap();
        ItemGroup itemGroup = new ItemGroup("sm", "B2C");
        List<ItemInfoDTO> itemInfoDTOS = JSON.parseArray(JSON.toJSONString(itemGroupItemInfoGroupResponseMap.get(
            itemGroup).getValue()
            .values()), ItemInfoDTO.class);
        List<Map> skuList = itemInfoDTOS.stream().map(itemInfoDTO -> {
            ItemDTO itemDTO = itemInfoDTO.getItemInfos().get("captain").getItemDTO();
            Map skuMap = Maps.newHashMap();
            Long i = itemDTO.getItemId() == null ? 0L : itemDTO.getItemId();
            skuMap.put("skuId", itemDTO.getSkuId() == null ? 0L : itemDTO.getSkuId());
            skuMap.put("itemId", itemDTO.getItemId() == null ? 0L : itemDTO.getItemId());
            return skuMap;
        }).collect(Collectors.toList());
        Map<String, Object> paramsValue = new HashMap<>(16);
        Map paramMap = Maps.newHashMap();
        paramMap.put("userId", userId);
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
