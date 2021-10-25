package com.tmall.wireless.tac.biz.processor.wzt.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.model.ItemGroup;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.framework.support.itemInfo.ItemInfoGroupResponse;
import com.tmall.txcs.gs.spi.recommend.RpcSpi;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.wzt.constant.Constant;
import com.tmall.wireless.tac.biz.processor.wzt.model.ItemLimitDTO;
import com.tmall.wireless.tac.biz.processor.wzt.model.convert.ItemInfoDTO;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        List<Map> skuList = Lists.newArrayList();
        itemInfoDTOS.forEach(itemInfoDTO -> {
            Map<String, Object> skuMap = Maps.newHashMap();
            try {
                if (itemInfoDTO.getItemInfos().get("captain") != null) {
                    Map<String, Object> itemInfoVO = itemInfoDTO.getItemInfos().get("captain").getItemInfoVO();
                    skuMap.put("skuId", itemInfoVO.get("skuId") == null ? 0L : itemInfoVO.get("skuId"));
                    skuMap.put("itemId", itemInfoDTO.getItemEntity().getItemId());
                    skuList.add(skuMap);
                } else {
                    HadesLogUtil.stream(ScenarioConstantApp.WU_ZHE_TIAN)
                            .kv("method", "buildGetItemLimitParam")
                            .kv("captain is null ,itemId ", Long.toString(itemInfoDTO.getItemEntity().getItemId()))
                            .info();
                    tacLogger.info("buildGetItemLimitParam参数构建captain为空,itemId=" + itemInfoDTO.getItemEntity().getItemId());
                }
            } catch (Exception e) {
                HadesLogUtil.stream(ScenarioConstantApp.WU_ZHE_TIAN)
                        .kv("method", "buildGetItemLimitParam")
                        .kv("build Exception", JSON.toJSONString(e))
                        .info();
                tacLogger.info("buildGetItemLimitParam参数构建异常,itemId=" + itemInfoDTO.getItemEntity().getItemId() + JSON.toJSONString(e));
            }
        });
        if (skuList.size() == 0) {
            return null;
        }

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
            JSONObject jsonObject = (JSONObject) JSON.toJSON(o);
            Boolean success = jsonObject.getBoolean(Constant.SUCCESS);
            //适配异常情况
            if (success == null) {
                HadesLogUtil.stream(ScenarioConstantApp.WU_ZHE_TIAN)
                        .kv("getItemLimitResult", "success is null")
                        .info();
                return null;
            }
            if (success) {
                Map<Long, List<ItemLimitDTO>> longListMap = null;
                try {
                    longListMap = JSONObject.parseObject(((JSONObject) jsonObject.get(
                            Constant.LIMIT_INFO)).toJSONString(),
                            new TypeReference<Map<Long, List<ItemLimitDTO>>>() {
                            });
                } catch (Exception e) {
                    HadesLogUtil.stream(ScenarioConstantApp.WU_ZHE_TIAN)
                            .kv("getItemLimitResult", "convert json error")
                            .info();
                }
                HadesLogUtil.stream(ScenarioConstantApp.WU_ZHE_TIAN)
                        .kv("paramsValue", JSON.toJSONString(paramsValue))
                        .kv("longListMap", JSON.toJSONString(longListMap))
                        .info();
                tacLogger.info(LOG_PREFIX + "限购接口查询paramsValue:" + JSON.toJSONString(paramsValue) + "|jsonObject：" + JSON
                        .toJSONString(jsonObject));
                return longListMap;
            } else {
                HadesLogUtil.stream(ScenarioConstantApp.WU_ZHE_TIAN)
                        .kv("getItemLimitResult", "query limit success is false")
                        .info();
                tacLogger.warn(LOG_PREFIX + "限购信息查询结果为空");
                return null;
            }
        } catch (Exception e) {
            HadesLogUtil.stream(ScenarioConstantApp.WU_ZHE_TIAN)
                    .kv("getItemLimitResult", "query limit Exception")
                    .kv("Exception", JSON.toJSONString(e))
                    .info();
            tacLogger.error(LOG_PREFIX + "获取限购信息异常", e);
            e.printStackTrace();
        }
        return null;
    }

    public Map<Long, List<ItemLimitDTO>> getItemLimitResult(SgFrameworkContextItem sgFrameworkContextItem) {
        Map<Long, List<ItemLimitDTO>> limitResult;
        Map<String, Object> param = this.buildGetItemLimitParam(sgFrameworkContextItem);
        if (param == null) {
            return null;
        }
        tacLogger.info("limit查询入参：" + JSON.toJSONString(param));
        limitResult = this.getItemLimitResult(param);
        tacLogger.info("limit返回结果：" + JSON.toJSONString(limitResult));
        return limitResult;
    }

}
