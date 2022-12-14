package com.tmall.wireless.tac.biz.processor.guessYourLikeShopCart4;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.*;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.todaycrazy.utils.MapUtil;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


/**
 * Created from template by 程斐斐 on 2021-09-14 14:56:32.
 */

@SdkExtension(
        bizId = "supermarket",
        useCase = "b2c",
        scenario = "guessYourLikeShopCart4"
)
public class GuessYourLikeShopCart4ItemOriginDataRequestBuildSdkExtPt extends Register implements ItemOriginDataRequestBuildSdkExtPt {

    public static final Long APPID_B2C = 28371L;

    @Autowired
    TacLoggerImpl tacLogger;

    @Override
    public RecommendRequest process(SgFrameworkContextItem sgFrameworkContextItem) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
        tacLogger.info("=================tacLogger+ 已进入tpp参数组装==================");
        tacLogger.info(dateFormat.format(System.currentTimeMillis())+" sgFrameworkContextItem=" + JSON.toJSONString(sgFrameworkContextItem));
        RecommendRequest tppRequest = new RecommendRequest();
        tppRequest.setAppId(APPID_B2C);
        tppRequest.setUserId(Optional.of(sgFrameworkContextItem).
                map(SgFrameworkContext::getCommonUserParams).
                map(CommonUserParams::getUserDO).map(UserDO::getUserId).orElse(0L));
        Map<String, String> params = Maps.newHashMap();
        params.put("smAreaId", Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getLocParams)
                .map(LocParams::getSmAreaId).orElse(0L).toString());

        //解析sgFrameworkContextItem获取参数map
        Map<String,Object> contextParamsMap = Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getTacContext).map(Context::getParams)
                .orElse(Maps.newHashMap());

        String type = MapUtil.getStringWithDefault(contextParamsMap, "type", "");
        String logicAreaId = MapUtil.getStringWithDefault(contextParamsMap, "logicAreaId", "0");
        String index = MapUtil.getStringWithDefault(contextParamsMap, "index", "0");
        String pageSize = MapUtil.getStringWithDefault(contextParamsMap, "pageSize", "20");
        String level1Id = MapUtil.getStringWithDefault(contextParamsMap, "level1Id", "0");
        String level2Id = MapUtil.getStringWithDefault(contextParamsMap, "level2Id", "0");
        String detailItemIdList = MapUtil.getStringWithDefault(contextParamsMap, "detailItemIdList", "");
        String itemBusinessType = MapUtil.getStringWithDefault(contextParamsMap, "itemBusinessType", "");
        String honehourStoreId = MapUtil.getStringWithDefault(contextParamsMap, "honehourStoreId", "0");
        String isFirstPage = MapUtil.getStringWithDefault(contextParamsMap, "isFirstPage", "true");
        params.put("type", type);
        params.put("logicAreaId", logicAreaId);
        params.put("index", index);
        params.put("pageSize", pageSize);
        params.put("level1Id", level1Id);
        params.put("level2Id", level2Id);
        params.put("detailItemIdList", detailItemIdList);
        params.put("itemBusinessType", itemBusinessType);
        params.put("honehourStoreId", honehourStoreId);
        params.put("isFirstPage", isFirstPage);



        tppRequest.setParams(params);
        tacLogger.info(dateFormat.format(System.currentTimeMillis())+" tppRequest="+JSON.toJSONString(tppRequest));
        tacLogger.info("=================tacLogger+ 已完成tpp参数组装==================");
        return tppRequest;
    }
}
