package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.alibaba.fastjson.JSON;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.aself.shoppingguide.client.loc.domain.AddressDTO;
import com.tmall.aself.shoppingguide.client.loc.util.AddressUtil;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.CommonUserParams;
import com.tmall.tcls.gs.sdk.framework.model.context.LocParams;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContext;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextItem;
import com.tmall.tcls.gs.sdk.framework.model.context.UserDO;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.constant.AppTypeEnum;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.constant.CommonConstant;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.constant.TabTypeEnum;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

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
        //tacLogger.info("tpp参数构建originDataProcessRequest:" + JSON.toJSONString(sgFrameworkContextItem));
        String csa = MapUtil.getStringWithDefault(sgFrameworkContextItem.getRequestParams(), "csa", "");
        String appType = MapUtil.getStringWithDefault(sgFrameworkContextItem.getRequestParams(), "appType", AppTypeEnum.INDEX_PAGE.getType());
        long userId = Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getUserDO).map(UserDO::getUserId).orElse(0L);
        long index = MapUtil.getLongWithDefault(sgFrameworkContextItem.getRequestParams(), "index", 0L);
        boolean isFirstPage = index == 0;
        sgFrameworkContextItem.getUserParams().put("isFirstPage", isFirstPage);
        AddressDTO addressDTO = AddressUtil.parseCSA(csa);
        tacLogger.info("addressDTO" + JSON.toJSONString(addressDTO));
        String regionCode = addressDTO.getRegionCode();
        String categoryIdsString = MapUtil.getStringWithDefault(sgFrameworkContextItem.getRequestParams(), "categoryIds", "");
        String tabType = MapUtil.getStringWithDefault(sgFrameworkContextItem.getRequestParams(), "tabType", "");
        List<String> categoryIds = new ArrayList<>(Arrays.asList(categoryIdsString.split(",")));
        List<String> cacheKeyList = this.buildCacheKeyList(categoryIds, tabType);
        //根据类别id构建参数
        RecommendRequest recommendRequest = new RecommendRequest();
        Map<String, String> params = Maps.newHashMap();
        params.put("pageSize", MapUtil.getStringWithDefault(sgFrameworkContextItem.getRequestParams(), "pageSize", "20"));
        params.put("isFirstPage", String.valueOf(isFirstPage));
        params.put("smAreaId", Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getLocParams).map(LocParams::getSmAreaId).map(Objects::toString).orElse("330100"));
        params.put("itemTairKeys", String.join(",", cacheKeyList));
        if (StringUtils.isEmpty(regionCode)) {
            regionCode = "107";
        }
        params.put("regionCode", regionCode);
        params.put("exposureDataUserId", Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getUserDO).map(UserDO::getCna).orElse(""));
        //算法需要根据appId区分埋点，故同一接口不同场景做区分
        //if (AppTypeEnum.TAB_PAGE.getType().equals(appType)) {21431L
        //    params.put("appid", String.valueOf(AppIdEnum.TAB_APP_ID.getCode()));
        //    recommendRequest.setAppId(AppIdEnum.TAB_APP_ID.getCode());
        //} else {22519L
        //    params.put("appid", String.valueOf(AppIdEnum.INDEX_APP_ID.getCode()));
        //    recommendRequest.setAppId(AppIdEnum.INDEX_APP_ID.getCode());
        //}

        // 承接页
        if (AppTypeEnum.TAB_PAGE.getType().equals(appType)) {
            if (TabTypeEnum.TODAY_CHAO_SHENG.getType().equals(tabType)) {
                params.put("appid", "21431");
                recommendRequest.setAppId(21431L);
            } else {
                params.put("appid", "31438");
                recommendRequest.setAppId(31438L);
            }
            // 首页
        } else {
            params.put("appid", "31439");
            recommendRequest.setAppId(31439L);
        }

        recommendRequest.setUserId(userId);
        recommendRequest.setParams(params);
        recommendRequest.setLogResult(true);
        /*tacLogger.info("tpp入参_recommendRequest_:" + JSON.toJSONString(recommendRequest));
        HadesLogUtil.stream(ScenarioConstantApp.TODAY_CRAZY_RECOMMEND_TAB)
                .kv("sgFrameworkContextItem", JSON.toJSONString(sgFrameworkContextItem))
                .kv("recommendRequest", JSON.toJSONString(recommendRequest))
                .info();*/
        return recommendRequest;
    }


    /**
     * 构建类目id作为tair的key
     * 区分线上线下环境
     * todo 跟程斐确认后再发布
     *
     * @param categoryIds
     * @param tabType
     * @return
     */
    private List<String> buildCacheKeyList(List<String> categoryIds, String tabType) {
        List<String> cacheKeyList = Lists.newArrayList();
        String _pre = "_pre";
        if (TabTypeEnum.TODAY_CHAO_SHENG.getType().equals(tabType)) {
//            if (RpmContants.enviroment.isOnline()) {
            if (true) {
                cacheKeyList.addAll(Arrays.asList(CommonConstant.TODAY_CHANNEL_NEW_FEATURED, CommonConstant.TODAY_PROMOTION_FEATURED, CommonConstant.TODAY_ALGORITHM));
            } else {
                cacheKeyList.addAll(Arrays.asList(CommonConstant.TODAY_CHANNEL_NEW_FEATURED + _pre, CommonConstant.TODAY_PROMOTION_FEATURED + _pre, CommonConstant.TODAY_ALGORITHM + _pre));
            }
        } else {
            categoryIds.forEach(categoryId -> {
//                if (RpmContants.enviroment.isOnline()) {
                if (true) {
                    cacheKeyList.add(CommonConstant.TODAY_CHANNEL_NEW + "_" + categoryId);
                    cacheKeyList.add(CommonConstant.TODAY_PROMOTION + "_" + categoryId);
                    cacheKeyList.add(CommonConstant.TODAY_ALGORITHM + "_" + categoryId);
                } else {
                    cacheKeyList.add(CommonConstant.TODAY_CHANNEL_NEW + "_" + categoryId + _pre);
                    cacheKeyList.add(CommonConstant.TODAY_PROMOTION + "_" + categoryId + _pre);
                    cacheKeyList.add(CommonConstant.TODAY_ALGORITHM + "_" + categoryId + _pre);
                }
            });
        }
        return cacheKeyList;
    }
}
