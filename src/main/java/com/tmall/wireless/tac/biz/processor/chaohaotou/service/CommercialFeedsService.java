package com.tmall.wireless.tac.biz.processor.chaohaotou.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.aself.shoppingguide.client.loc.util.AddressUtil;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.spi.recommend.RpcSpi;
import com.tmall.wireless.tac.biz.processor.chaohaotou.constant.Constant;
import com.tmall.wireless.tac.biz.processor.chaohaotou.model.TmcsZntItemDTO;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: luoJunChong
 * @Date: 2021/8/16 14:27
 */
@Component
public class CommercialFeedsService {
    @Autowired
    RpcSpi rpcSpi;

    @Autowired
    TacLogger tacLogger;

    private static final Logger LOGGER = LoggerFactory.getLogger(CommercialFeedsService.class);

    public Pair<Boolean, List<TmcsZntItemDTO>> getCommercialFeeds(SgFrameworkContextItem sgFrameworkContextItem) {
        Pair<Boolean, List<TmcsZntItemDTO>> booleanListPair = null;
        HadesLogUtil.debug("sgFrameworkContextItem_debug" + JSON.toJSONString(sgFrameworkContextItem)+"end_");
        Map<String, Object> paramMap = this.buildParam(sgFrameworkContextItem);
        tacLogger.info("getCommercialFeeds_入参" + JSON.toJSONString(paramMap));
        HadesLogUtil.debug("getCommercialFeeds_debug_paramMap" + JSON.toJSONString(paramMap)+"end_");
        HadesLogUtil.stream(ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_CONTENT)
                .kv("getCommercialFeeds_入参", JSON.toJSONString(paramMap))
                .kv("sgFrameworkContextItem_", JSON.toJSONString(sgFrameworkContextItem))
                .info();
        try {
            Object o = rpcSpi.invokeHsf(Constant.TMCS_ZNT_ENGINE, paramMap);
            if (o == null) {
                return null;
            }
            booleanListPair = this.convert(o);
        } catch (Exception e) {
            tacLogger.error("tmcsZntEngine接口调用异常", e);
        }
        return booleanListPair;
    }

    private Pair<Boolean, List<TmcsZntItemDTO>> convert(Object o) {
        String jsonStr = JSONObject.toJSONString(o);
        tacLogger.info("convert_2" + jsonStr);
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        List<TmcsZntItemDTO> tmcsZntItemDTOList = Lists.newArrayList();
        if (!jsonObject.getBoolean("success")) {
            return null;
        }
        JSONObject data = jsonObject.getJSONObject("data");
        JSONArray jsonArray = data.getJSONArray("listInfo");
        JSONObject pageInfo = data.getJSONObject("pageInfo");
        if (!CollectionUtils.isEmpty(jsonArray)) {
            tmcsZntItemDTOList = JSONObject.parseArray(jsonArray.toJSONString(), TmcsZntItemDTO.class);
        }
        return Pair.of(pageInfo.getBoolean("hasMore"), tmcsZntItemDTOList);
    }

    private Map<String, Object> buildParam(SgFrameworkContextItem sgFrameworkContextItem) {
        Map<String, Object> paramsValue = new HashMap<>(16);
        Map<String, Object> paramMap = Maps.newHashMap();
        Long userId = MapUtil.getLongWithDefault(sgFrameworkContextItem.getRequestParams(), "userId", 0L);
        Long index = MapUtil.getLongWithDefault(sgFrameworkContextItem.getRequestParams(), "index", 1L);
        Long pageSize = MapUtil.getLongWithDefault(sgFrameworkContextItem.getRequestParams(), "pageSize", 20L);
        Long smAreaId = MapUtil.getLongWithDefault(sgFrameworkContextItem.getRequestParams(), "smAreaId", 330100L);
        String csa = MapUtil.getStringWithDefault(sgFrameworkContextItem.getRequestParams(), "csa", "");
        String bizType = MapUtil.getStringWithDefault(sgFrameworkContextItem.getRequestParams(), "bizType", "");
        String itemId = MapUtil.getStringWithDefault(sgFrameworkContextItem.getRequestParams(), "itemId", "0");
        String regionCode = AddressUtil.parseCSA(csa).getRegionCode();
        paramMap.put("userId", userId);
        paramMap.put("regionCode", regionCode);
        paramMap.put("smAreaId", smAreaId);
        paramMap.put("currentPage", index);
        paramMap.put("pageSize", pageSize);
        paramMap.put("commerce", Constant.B2C);
        paramMap.put("bizType", bizType);
        paramMap.put("itemId", itemId);
        paramsValue.put("tmcsZntFeedsRequest", paramMap);
        return paramsValue;
    }
}
