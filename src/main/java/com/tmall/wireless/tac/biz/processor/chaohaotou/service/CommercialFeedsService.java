package com.tmall.wireless.tac.biz.processor.chaohaotou.service;

import com.tmall.aself.shoppingguide.client.loc.util.AddressUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.spi.recommend.RpcSpi;
import com.tmall.wireless.tac.biz.processor.chaohaotou.constant.Constant;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: luoJunChong
 * @Date: 2021/8/16 14:27
 */
public class CommercialFeedsService {
    @Autowired
    RpcSpi rpcSpi;

    @Autowired
    TacLogger tacLogger;

    public void getCommercialFeeds(SgFrameworkContextItem sgFrameworkContextItem) {
        // 品牌承接页feeds流	导购->Engine TODO:需要跟导购确定入参出参
        //ResultResponse<List<TmcsZntItemDTO>> commercialFeeds (TmcsZntFeedsRequest request);
        Map<String, Object> paramMap = this.buildParam(sgFrameworkContextItem);
        try {
            Object o = rpcSpi.invokeHsf(Constant.TODAY_CRAZY_LIMIT, paramMap);
        } catch (Exception e) {
            tacLogger.error("获取限购信息异常", e);
        }
    }

    private Map<String, Object> buildParam(SgFrameworkContextItem sgFrameworkContextItem) {
        Map<String, Object> paramsValue = new HashMap<>(16);
        Map<String, Object> paramMap = new HashMap<>();
        Long userId = MapUtil.getLongWithDefault(sgFrameworkContextItem.getRequestParams(), "userId", 0L);
        Long index = MapUtil.getLongWithDefault(sgFrameworkContextItem.getRequestParams(), "index", 0L);
        Long pageSize = MapUtil.getLongWithDefault(sgFrameworkContextItem.getRequestParams(), "pageSize", 20L);
        Long smAreaId = MapUtil.getLongWithDefault(sgFrameworkContextItem.getRequestParams(), "smAreaId", 330100L);
        String csa = MapUtil.getStringWithDefault(sgFrameworkContextItem.getRequestParams(), "csa", "");
        String regionCode = AddressUtil.parseCSA(csa).getRegionCode();
        paramMap.put("userId", userId);
        paramMap.put("feedsType", Constant.FEEDS_TYPE);
        paramMap.put("regionCode", regionCode);
        paramMap.put("smAreaId", smAreaId);
        paramMap.put("index", index);
        paramMap.put("pageSize", pageSize);
        paramMap.put("commerce", Constant.COMMERCE);
        paramsValue.put("TmcsZntFeedsRequest", paramMap);
        return paramsValue;
    }
}
