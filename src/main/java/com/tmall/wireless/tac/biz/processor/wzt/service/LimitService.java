package com.tmall.wireless.tac.biz.processor.wzt.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import com.google.common.collect.Maps;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.model.ItemGroup;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.framework.support.itemInfo.ItemInfoGroupResponse;
import com.tmall.txcs.gs.spi.recommend.RpcSpi;
import com.tmall.wireless.tac.biz.processor.wzt.constant.Constant;
import com.tmall.wireless.tac.biz.processor.wzt.model.ItemLimitDTO;
import com.tmall.wireless.tac.biz.processor.wzt.model.convert.ItemDTO;
import com.tmall.wireless.tac.biz.processor.wzt.model.convert.ItemInfoDTO;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: luoJunChong
 * @Date: 2021/6/17 9:56
 * 限购信息获取
 */
@Component
public class LimitService {

    private static final String LOG_PREFIX = "LimitService-";

    @Autowired
    RpcSpi rpcSpi;

    @Autowired
    TacLogger tacLogger;

    private Map<String, Object> buildGetItemLimitResult(SgFrameworkContextItem sgFrameworkContextItem) {
        Long userId = MapUtil.getLongWithDefault(sgFrameworkContextItem.getRequestParams(), "userId", 0L);
        Map<ItemGroup, ItemInfoGroupResponse> itemGroupItemInfoGroupResponseMap = sgFrameworkContextItem
                .getItemInfoGroupResponseMap();
        ItemGroup itemGroup = new ItemGroup("sm", "B2C");

        //tair缓存取skuId
        List<Map> skuList = (List<Map>) sgFrameworkContextItem.getUserParams().get("LimitSkuList");
        //captain获取skuId
        //List<ItemInfoDTO> itemInfoDTOS = JSON.parseArray(JSON.toJSONString(itemGroupItemInfoGroupResponseMap.get(
        //    itemGroup).getValue()
        //    .values()), ItemInfoDTO.class);
        //List<Map> skuList = itemInfoDTOS.stream().map(itemInfoDTO -> {
        //    ItemDTO itemDTO = itemInfoDTO.getItemInfos().get("captain").getItemDTO();
        //    Map<String, Object> skuMap = Maps.newHashMap();
        //    skuMap.put("skuId", itemDTO.getSkuId() == null ? 0L : itemDTO.getSkuId());
        //    skuMap.put("itemId", itemDTO.getItemId() == null ? 0L : itemDTO.getItemId());
        //    return skuMap;
        //}).collect(Collectors.toList());
        Map<String, Object> paramsValue = new HashMap<>(16);
        Map<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("userId", userId);
        paramMap.put("itemIdList", skuList);
        paramsValue.put("itemLimitInfoQuery", paramMap);
        tacLogger.info("构建limit入参" + JSON.toJSONString(paramsValue));
        return paramsValue;
    }

    private JSONObject doGetItemLimitResult(Map<String, Object> paramsValue) {
        Object o;
        try {
            o = rpcSpi.invokeHsf(Constant.TODAY_CRAZY_LIMIT, paramsValue);
            JSONObject jsonObject = (JSONObject) JSON.toJSON(o);
            if ((boolean) jsonObject.get(Constant.SUCCESS)) {
                return (JSONObject) jsonObject.get(Constant.LIMIT_INFO);
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

    public Map<Long, List<ItemLimitDTO>> getItemLimitResult(SgFrameworkContextItem sgFrameworkContextItem) {
        JSONObject limitJsonObject = this.doGetItemLimitResult(this.buildGetItemLimitResult(sgFrameworkContextItem));
        return this.convert(limitJsonObject);
    }

    private Map<Long, List<ItemLimitDTO>> convert(JSONObject limitJsonObject) {
        Map<Long, List<ItemLimitDTO>> limitResult = JSONObject.parseObject(limitJsonObject.toJSONString(),
                new TypeReference<Map<Long, List<ItemLimitDTO>>>() {
                });
        if (limitResult != null) {
            return limitResult;
            //return this.mock(limitResult);
        }
        return null;
    }

    private Map<Long, List<ItemLimitDTO>> mock(Map<Long, List<ItemLimitDTO>> longListMap) {
        Map<Long, List<ItemLimitDTO>> longListMap1 = new HashMap<>(16);
        for (Long key : longListMap.keySet()) {
            List<ItemLimitDTO> itemLimitDTOS = longListMap.get(key);
            //美宝莲唇膏
            if (key == 607615049047L) {
                ItemLimitDTO itemLimitDTO = new ItemLimitDTO();
                //超过总限购
                itemLimitDTO.setTotalLimit(10L);
                itemLimitDTO.setUsedCount(10L);
                //超过用户限购
                itemLimitDTO.setUserLimit(5L);
                itemLimitDTO.setUserUsedCount(5L);
                itemLimitDTOS.add(0, itemLimitDTO);
            }
            longListMap1.put(key, itemLimitDTOS);
        }
        return longListMap1;
    }

}
