package com.tmall.wireless.tac.biz.processor.chaohaotou.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.aself.shoppingguide.client.loc.util.AddressUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.spi.recommend.RpcSpi;
import com.tmall.wireless.tac.biz.processor.chaohaotou.constant.Constant;
import com.tmall.wireless.tac.biz.processor.chaohaotou.model.convert.TmcsZntItemDTO;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
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

    public Pair<Boolean, List<TmcsZntItemDTO>> getCommercialFeeds(SgFrameworkContextItem sgFrameworkContextItem) {
        Pair<Boolean, List<TmcsZntItemDTO>> booleanListPair = null;
        Map<String, Object> paramMap = this.buildParam(sgFrameworkContextItem);
        tacLogger.info("getCommercialFeeds_入参" + JSON.toJSONString(paramMap));
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
        List<TmcsZntItemDTO> tmcsZntItemDTOList = Lists.newArrayList();
        JSONObject jsonObject = JSONObject.parseObject(o.toString());
        if (!jsonObject.getBoolean("success")) {
            return null;
        }
        JSONObject data = jsonObject.getJSONObject("data");
        JSONArray jsonArray = data.getJSONArray("listInfo");
        JSONObject pageInfo = jsonObject.getJSONObject("pageInfo");
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
        String regionCode = AddressUtil.parseCSA(csa).getRegionCode();
        paramMap.put("userId", userId);
        paramMap.put("regionCode", regionCode);
        paramMap.put("smAreaId", smAreaId);
        paramMap.put("currentPage", index);
        paramMap.put("pageSize", pageSize);
        paramMap.put("commerce", Constant.B2C);
        paramMap.put("bizType", bizType);
        paramsValue.put("tmcsZntFeedsRequest", paramMap);
        return paramsValue;
    }
}
