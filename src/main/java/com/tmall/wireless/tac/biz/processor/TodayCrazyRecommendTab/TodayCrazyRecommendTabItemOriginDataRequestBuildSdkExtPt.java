package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.aself.shoppingguide.client.loc.domain.AddressDTO;
import com.tmall.aself.shoppingguide.client.loc.util.AddressUtil;
import com.tmall.aselfcommon.model.oc.domain.LogicalArea;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.*;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.constant.AppIdEnum;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.constant.AppTypeEnum;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.constant.TabTypeEnum;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * Created from template by 罗俊冲 on 2021-09-15 17:54:58.
 * 构建TPP参数
 */

@SdkExtension(
        bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.TODAY_CRAZY_RECOMMEND_TAB
)
public class TodayCrazyRecommendTabItemOriginDataRequestBuildSdkExtPt extends Register implements ItemOriginDataRequestBuildSdkExtPt {
    @Autowired
    TacLoggerImpl tacLogger;

    @Override
    public RecommendRequest process(SgFrameworkContextItem sgFrameworkContextItem) {
        return this.buildTppParam(sgFrameworkContextItem);
    }

    private RecommendRequest buildTppParam(SgFrameworkContextItem sgFrameworkContextItem) {
        String csa = MapUtil.getStringWithDefault(sgFrameworkContextItem.getRequestParams(), "csa", "");
        String appType = MapUtil.getStringWithDefault(sgFrameworkContextItem.getRequestParams(), "appType", AppTypeEnum.INDEX_PAGE.getType());
        long userId = Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getUserDO).map(UserDO::getUserId).orElse(0L);
        long index = MapUtil.getLongWithDefault(sgFrameworkContextItem.getRequestParams(), "index", 0L);
        boolean isFirstPage = index == 0;
        sgFrameworkContextItem.getUserParams().put("isFirstPage", isFirstPage);
        AddressDTO addressDTO = AddressUtil.parseCSA(csa);
        String regionCode = addressDTO.getRegionCode();
        tacLogger.info("addressDTO_:" + JSON.toJSONString(addressDTO));
        tacLogger.info("sgFrameworkContextItem_:" + JSON.toJSONString(sgFrameworkContextItem));
        String categoryIdsString = MapUtil.getStringWithDefault(sgFrameworkContextItem.getRequestParams(), "categoryIds", "");
        String tabType = MapUtil.getStringWithDefault(sgFrameworkContextItem.getRequestParams(), "tabType", "");
        List<String> categoryIds = new ArrayList<>(Arrays.asList(categoryIdsString.split(",")));
        List<String> cacheKeyList = this.buildCacheKeyList(categoryIds, tabType);
        tacLogger.info("cacheKeyList_:" + JSON.toJSONString(cacheKeyList));
        //根据类别id构建参数
        RecommendRequest recommendRequest = new RecommendRequest();
        Map<String, String> params = Maps.newHashMap();
        params.put("pageSize", MapUtil.getStringWithDefault(sgFrameworkContextItem.getRequestParams(), "pageSize", "20"));
        params.put("isFirstPage", String.valueOf(isFirstPage));
        params.put("smAreaId", Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getLocParams).map(LocParams::getSmAreaId).map(Objects::toString).orElse("330100"));
        params.put("itemTairKeys", String.join(",", cacheKeyList));
        params.put("regionCode", regionCode);
        params.put("exposureDataUserId", Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getUserDO).map(UserDO::getCna).orElse(""));
        if (AppTypeEnum.TAB_PAGE.getType().equals(appType)) {
            params.put("appid", String.valueOf(AppIdEnum.TAB_APP_ID.getCode()));
            recommendRequest.setAppId(AppIdEnum.TAB_APP_ID.getCode());
        } else {
            params.put("appid", String.valueOf(AppIdEnum.INDEX_APP_ID.getCode()));
            recommendRequest.setAppId(AppIdEnum.INDEX_APP_ID.getCode());
        }
        recommendRequest.setUserId(userId);
        recommendRequest.setParams(params);
        recommendRequest.setLogResult(true);
        tacLogger.info("recommendRequest_:" + JSON.toJSONString(recommendRequest));
        // todo MOCK
        //recommendRequest = this.mock();
        return recommendRequest;
    }

    private RecommendRequest mock() {
        RecommendRequest mockData = JSON.parseObject("{\n" +
                "\t\"appId\": 27154,\n" +
                "\t\"logResult\": false,\n" +
                "\t\"params\": {\n" +
                "\t\t\"itemBusinessType\": \"OneHour\",\n" +
                "\t\t\"itemSetIdList\": \"13545\",\n" +
                "\t\t\"appid\": \"27154\",\n" +
                "\t\t\"logicAreaId\": \"107\",\n" +
                "\t\t\"rt1HourStoreId\": \"233930003\",\n" +
                "\t\t\"isFirstPage\": \"true\",\n" +
                "\t\t\"smAreaId\": \"330100\",\n" +
                "\t\t\"userId\": \"1832025789\"\n" +
                "\t},\n" +
                "\t\"userId\": 1832025789\n" +
                "}", RecommendRequest.class);
        return mockData;
    }

    /**
     * 构建类目id作为tair的key
     * @param categoryIds
     * @param tabType
     * @return
     */
    private List<String> buildCacheKeyList(List<String> categoryIds, String tabType) {
        //String shorthand = LogicalArea.parseByCode(addressDTO.getRegionCode()).getShorthand();
        List<String> cacheKeyList = Lists.newArrayList();
        if (TabTypeEnum.TODAY_CHAO_SHENG.getType().equals(tabType)) {
            cacheKeyList.addAll(Arrays.asList("today_featured", "today_algorithm"));
        } else {
            categoryIds.forEach(categoryId -> {
                cacheKeyList.add("today_" + categoryId);
                cacheKeyList.add("today_algorithm_" + categoryId);
            });
        }
        return cacheKeyList;
    }
}
