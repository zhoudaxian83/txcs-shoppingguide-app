package com.tmall.wireless.tac.biz.processor.chaohaotou.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.model.ItemGroup;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.framework.support.itemInfo.ItemInfoGroupResponse;
import com.tmall.txcs.gs.spi.recommend.RpcSpi;
import com.tmall.wireless.tac.biz.processor.chaohaotou.model.ItemLimitDTO;
import com.tmall.wireless.tac.biz.processor.chaohaotou.model.convert.ItemDTO;
import com.tmall.wireless.tac.biz.processor.chaohaotou.model.convert.ItemInfoDTO;
import com.tmall.wireless.tac.biz.processor.wzt.constant.Constant;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: luoJunChong
 * @Date: 2021/6/17 9:56
 * 限购信息获取
 */
@Component
public class LimitService {
    Logger LOGGER = LoggerFactory.getLogger(LimitService.class);
    private static final String LOG_PREFIX = "LimitService-";

    @Autowired
    RpcSpi rpcSpi;

    @Autowired
    TacLogger tacLogger;

    private Map<String, Object> buildGetItemLimitParam(SgFrameworkContextItem sgFrameworkContextItem) {
        Long userId = MapUtil.getLongWithDefault(sgFrameworkContextItem.getRequestParams(), "userId", 0L);
        Map<ItemGroup, ItemInfoGroupResponse> itemGroupItemInfoGroupResponseMap = sgFrameworkContextItem
            .getItemInfoGroupResponseMap();
        ItemGroup itemGroup = new ItemGroup("sm", "B2C");
        //captain获取skuId
        List<ItemInfoDTO> itemInfoDTOS = JSON.parseArray(JSON.toJSONString(itemGroupItemInfoGroupResponseMap.get(
            itemGroup).getValue()
            .values()), ItemInfoDTO.class);
        List<Map> skuList = itemInfoDTOS.stream().map(itemInfoDTO -> {
            ItemDTO itemDTO = itemInfoDTO.getItemInfos().get("captain").getItemDTO();
            Map<String, Object> skuMap = Maps.newHashMap();
            skuMap.put("skuId", itemDTO.getSkuId() == null ? 0L : itemDTO.getSkuId());
            skuMap.put("itemId", itemDTO.getItemId() == null ? 0L : itemDTO.getItemId());
            return skuMap;
        }).collect(Collectors.toList());
        Map<String, Object> paramsValue = new HashMap<>(16);
        Map<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("userId", userId);
        paramMap.put("itemIdList", skuList);
        paramsValue.put("itemLimitInfoQuery", paramMap);
        return paramsValue;
    }

    private Map<Long, List<ItemLimitDTO>> getItemLimitResult(Map<String, Object> paramsValue) {
        Object o;
        try {
            o = rpcSpi.invokeHsf(Constant.TODAY_CRAZY_LIMIT, paramsValue);
            JSONObject jsonObject = (JSONObject)JSON.toJSON(o);
            Boolean success = jsonObject.getBoolean(Constant.SUCCESS);
            //适配异常情况
            if (success == null) {
                tacLogger.info(LOG_PREFIX + "限购接口RPC调用返回异常paramsValue:" + paramsValue + "|jsonObject：" + JSON
                    .toJSONString(jsonObject));
                LOGGER.error("限购接口RPC调用返回异常");
                return null;
            }
            if (success) {
                return JSONObject.parseObject(((JSONObject)jsonObject.get(
                    Constant.LIMIT_INFO)).toJSONString(),
                    new TypeReference<Map<Long, List<ItemLimitDTO>>>() {
                    });
            } else {
                tacLogger.warn(LOG_PREFIX + "限购信息查询结果为空");
                return null;
            }
        } catch (Exception e) {
            tacLogger.error(LOG_PREFIX + "获取限购信息异常", e);
            LOGGER.error("获取限购信息异常,paramsValue:" + paramsValue);
            e.printStackTrace();
        }
        return null;
    }

    public Map<Long, List<ItemLimitDTO>> getItemLimitResult(SgFrameworkContextItem sgFrameworkContextItem) {
        Map<Long, List<ItemLimitDTO>> limitResult;
        //MOCK
        if (false) {
            limitResult = this.mock();
        } else {
            Map<String, Object> param = this.buildGetItemLimitParam(sgFrameworkContextItem);
            limitResult = this.getItemLimitResult(param);
            tacLogger.info("limit入参：" + JSON.toJSONString(param));
            tacLogger.info("limit结果：" + JSON.toJSONString(limitResult));
        }
        return limitResult;
    }

    private Map<Long, List<ItemLimitDTO>> mock() {
        Map<Long, List<ItemLimitDTO>> limitResult = new HashMap<>(16);
        List<ItemLimitDTO> itemLimitDTOList = Lists.newArrayList();
        ItemLimitDTO itemLimitDTO = new ItemLimitDTO();
        //超过总限购
        //itemLimitDTO.setTotalLimit(10L);
        //itemLimitDTO.setUsedCount(10L);
        //超过用户限购
        //itemLimitDTO.setUserLimit(5L);
        //itemLimitDTO.setUserUsedCount(5L);

        itemLimitDTO.setSkuId(123456L);
        itemLimitDTOList.add(itemLimitDTO);
        limitResult.put(609977546160L, itemLimitDTOList);
        return limitResult;
    }
}
