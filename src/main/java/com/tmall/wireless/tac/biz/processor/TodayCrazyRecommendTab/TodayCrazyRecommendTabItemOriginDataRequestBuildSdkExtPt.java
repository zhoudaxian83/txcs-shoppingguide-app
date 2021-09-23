package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab;

import com.ali.unit.rule.util.lang.CollectionUtils;
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
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.constant.CommonConstant;
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
        long userId = Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getUserDO).map(UserDO::getUserId).orElse(0L);
        int index = Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getUserPageInfo).map(PageInfoDO::getIndex).orElse(0);
        AddressDTO addressDTO = AddressUtil.parseCSA(csa);
        String regionCode = addressDTO.getRegionCode();
        tacLogger.info("addressDTO_:" + JSON.toJSONString(addressDTO));
        tacLogger.info("sgFrameworkContextItem_:" + JSON.toJSONString(sgFrameworkContextItem));
        String categoryIdsString = MapUtil.getStringWithDefault(sgFrameworkContextItem.getRequestParams(), "categoryIds", "");
        String tabType = MapUtil.getStringWithDefault(sgFrameworkContextItem.getRequestParams(), "tabType", "");
        List<String> categoryIds = new ArrayList<>(Arrays.asList(categoryIdsString.split(",")));
        List<String> cacheKeyList = this.buildCacheKeyList(categoryIds, tabType, addressDTO);
        tacLogger.info("cacheKeyList_:" + JSON.toJSONString(cacheKeyList));
        //根据类别id构建参数
        RecommendRequest recommendRequest = new RecommendRequest();
        Map<String, String> params = Maps.newHashMap();
        params.put("pageSize", Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getUserPageInfo).map(PageInfoDO::getPageSize).map(Objects::toString).orElse("20"));
        params.put("isFirstPage", String.valueOf(index == 0));
        params.put("smAreaId", Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getLocParams).map(LocParams::getSmAreaId).map(Objects::toString).orElse("330100"));
        params.put("itemTairKeys", String.join(",", cacheKeyList));
        params.put("regionCode", regionCode);
        params.put("exposureDataUserId", Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getUserDO).map(UserDO::getCna).orElse(""));

        params.put("index", String.valueOf(index));
        params.put("appid", String.valueOf(CommonConstant.APP_ID));


        recommendRequest.setAppId(CommonConstant.APP_ID);
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
                "\t\"appId\": 27989,\n" +
                "\t\"logResult\": true,\n" +
                "\t\"params\": {\n" +
                "\t\t\"pmtName\": \"guessULike\",\n" +
                "\t\t\"logicAreaId\": \"107\",\n" +
                "\t\t\"pageSize\": \"20\",\n" +
                "\t\t\"index\": \"0\",\n" +
                "\t\t\"pmtSource\": \"sm_manager\",\n" +
                "\t\t\"type\": \"cainixihuan1\",\n" +
                "\t\t\"pageId\": \"cainixihuan1\",\n" +
                "\t\t\"enlargeCainixihuanToHigher\": \"500\",\n" +
                "\t\t\"smAreaId\": \"330100\",\n" +
                "\t\t\"itemBusinessType\": \"B2C,OneHour,HalfDay,NextDay\",\n" +
                "\t\t\"regionCode\": \"107\",\n" +
                "\t\t\"exposureDataUserId\": \"2443459148\",\n" +
                "\t\t\"appid\": \"27989\",\n" +
                "\t\t\"level1Id\": \"153\",\n" +
                "\t\t\"moduleId\": \"153\",\n" +
                "\t\t\"honehourStoreId\": \"0\",\n" +
                "\t\t\"isFirstPage\": \"false\",\n" +
                "\t\t\"frontIndex\": \"0\"\n" +
                "\t},\n" +
                "\t\"userId\": 2443459148\n" +
                "}", RecommendRequest.class);
        return mockData;
    }

    /**
     * 构建类目id作为tair的key
     *
     * @param categoryIds
     * @param tabType
     * @param addressDTO
     * @return
     */
    private List<String> buildCacheKeyList(List<String> categoryIds, String tabType, AddressDTO addressDTO) {
        String shorthand = LogicalArea.parseByCode(addressDTO.getRegionCode()).getShorthand();
        List<String> noFeaturedList = Lists.newArrayList();
        List<String> cacheKeyList = Lists.newArrayList();
        categoryIds.forEach(categoryId -> {
            String categoryIdAndShorthand = categoryId + "_" + shorthand;
            cacheKeyList.add("today_featured_" + categoryIdAndShorthand);
            cacheKeyList.add("today_algorithm_" + categoryIdAndShorthand);
            if (TabTypeEnum.OTHER.getType().equals(tabType)) {
                noFeaturedList.add("today_no_featured_" + categoryIdAndShorthand);
            }
        });
        if (CollectionUtils.isNotEmpty(noFeaturedList)) {
            cacheKeyList.addAll(noFeaturedList);
        }
        return cacheKeyList;
    }
}
