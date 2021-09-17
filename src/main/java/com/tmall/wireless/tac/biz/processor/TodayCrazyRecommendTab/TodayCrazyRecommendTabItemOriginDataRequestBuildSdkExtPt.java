package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab;

import com.ali.unit.rule.util.lang.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.aself.shoppingguide.client.loc.domain.AddressDTO;
import com.tmall.aself.shoppingguide.client.loc.util.AddressUtil;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.*;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.constant.TabTypeEnum;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * Created from template by 罗俊冲 on 2021-09-15 17:54:58.
 */

@SdkExtension(
        bizId = "supermarket",
        useCase = "b2c",
        scenario = "TodayCrazyRecommendTab"
)
public class TodayCrazyRecommendTabItemOriginDataRequestBuildSdkExtPt extends Register implements ItemOriginDataRequestBuildSdkExtPt {
    @Autowired
    TacLogger tacLogger;

    @Override
    public RecommendRequest process(SgFrameworkContextItem sgFrameworkContextItem) {
        return this.buildTppParam(sgFrameworkContextItem);
    }

    private RecommendRequest buildTppParam(SgFrameworkContextItem sgFrameworkContextItem) {
        String csa = MapUtil.getStringWithDefault(sgFrameworkContextItem.getRequestParams(), "csa", "");
        AddressDTO addressDTO = AddressUtil.parseCSA(csa);
        tacLogger.info("addressDTO_:" + JSON.toJSONString(addressDTO));
        tacLogger.info("sgFrameworkContextItem_:" + JSON.toJSONString(sgFrameworkContextItem));
        String categoryIdsString = MapUtil.getStringWithDefault(sgFrameworkContextItem.getRequestParams(), "categoryIds", "");
        String tabType = MapUtil.getStringWithDefault(sgFrameworkContextItem.getRequestParams(), "tabType", "");
        List<String> categoryIds = new ArrayList<>(Arrays.asList(categoryIdsString.split(",")));
        List<String> tairKeyList = this.buildTairKeyList(categoryIds, tabType);
        tacLogger.info("tairKeyList_:" + JSON.toJSONString(tairKeyList));
        //根据类别id构建参数
        RecommendRequest recommendRequest = new RecommendRequest();
        Map<String, String> params = Maps.newHashMap();
        params.put("pageSize", Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getUserPageInfo).map(PageInfoDO::getPageSize).map(Objects::toString).orElse("20"));
        params.put("index", Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getUserPageInfo).map(PageInfoDO::getIndex).map(Objects::toString).orElse("0"));
        params.put("smAreaId", Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getLocParams).map(LocParams::getSmAreaId).map(Objects::toString).orElse("330100"));
        recommendRequest.setAppId(21895L);
        recommendRequest.setUserId(Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getUserDO).map(UserDO::getUserId).orElse(0L));
        recommendRequest.setParams(params);
        recommendRequest.setLogResult(true);
        tacLogger.info("recommendRequest_:" + JSON.toJSONString(recommendRequest));
        return recommendRequest;
    }

    /**
     * 构建类目id作为tair的key
     *
     * @return
     */
    private List<String> buildTairKeyList(List<String> categoryIds, String tabType) {
        List<String> noFeaturedList = Lists.newArrayList();
        List<String> tairKeyList = Lists.newArrayList();
        categoryIds.forEach(categoryId -> {
            tairKeyList.add("today_featured_" + categoryId);
            tairKeyList.add("today_algorithm_" + categoryId);
            if (TabTypeEnum.OTHER.getType().equals(tabType)) {
                noFeaturedList.add("today_no_featured_" + categoryId);
            }
        });
        if (CollectionUtils.isNotEmpty(noFeaturedList)) {
            tairKeyList.addAll(noFeaturedList);
        }
        return tairKeyList;
    }
}
