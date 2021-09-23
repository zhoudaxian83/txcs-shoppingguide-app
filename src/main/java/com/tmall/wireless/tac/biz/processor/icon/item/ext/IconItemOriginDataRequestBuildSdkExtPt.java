package com.tmall.wireless.tac.biz.processor.icon.item.ext;


import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.tmall.crowd.guava.collect.Maps;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.*;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.txcs.gs.model.biz.context.PageInfoDO;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.icon.item.ItemRecommendService;
import com.tmall.wireless.tac.biz.processor.icon.item.ItemRequest;
import com.tmall.wireless.tac.biz.processor.icon.level2.BusinessTypeUtil;
import com.tmall.wireless.tac.biz.processor.todaycrazy.utils.MapUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@SdkExtension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C
        , scenario = ScenarioConstantApp.ICON_ITEM
)
public class IconItemOriginDataRequestBuildSdkExtPt extends Register implements ItemOriginDataRequestBuildSdkExtPt {

    private static final String fullDomainGray = "fullDomainGray";


    public static final Long CATEGORY_RECOMMEND_ITEM_RECOMMEND_PLATEFORM = 18611L;

    // icon全域实验appid
    public static final Long RECOMMEND_PLATFORM_ICON_FULL_DOMAIN_APP_ID = 25682L;



    @Override
    public RecommendRequest process(SgFrameworkContextItem sgFrameworkContextItem) {

        ItemRequest itemRequest = (ItemRequest) Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getTacContext).map(c -> c.get(ItemRecommendService.ITEM_REQUEST_KEY)).orElse(null);
        Preconditions.checkArgument(itemRequest != null);

        RecommendRequest recommendRequest = new RecommendRequest();
        Long userId = Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getUserDO).map(UserDO::getUserId).orElse(0L);


        Long appId = CATEGORY_RECOMMEND_ITEM_RECOMMEND_PLATEFORM;
//        if(GrayUtils.checkGray(userId, fullDomainGray)){
//            appId = CATEGORY_RECOMMEND_ITEM_RECOMMEND_PLATEFORM;
//        }else{
//            appId = RECOMMEND_PLATFORM_ICON_FULL_DOMAIN_APP_ID;
//        }

        recommendRequest.setAppId(appId);
        Map<String, String> params = Maps.newHashMap();
        recommendRequest.setParams(params);

        List<String> businessList = Lists.newArrayList();

        if (StringUtils.isEmpty(itemRequest.getLevel3Business()) || itemRequest.getLevel3Business().contains(BusinessTypeUtil.B2C)) {
            businessList.add(BusinessTypeUtil.B2C);
        } else if (itemRequest.getLevel3Business().contains(BusinessTypeUtil.OneHour)) {
            businessList.add(BusinessTypeUtil.OneHour);
        } else if (itemRequest.getLevel3Business().contains(BusinessTypeUtil.HalfDay)) {
            businessList.add(BusinessTypeUtil.HalfDay);
        } else if (itemRequest.getLevel3Business().contains(BusinessTypeUtil.NextDay)) {
            businessList.add(BusinessTypeUtil.NextDay);
        }

        if (CollectionUtils.isEmpty(businessList)) {
            businessList.add(BusinessTypeUtil.B2C);
        }

        recommendRequest.setUserId(userId);
        params.put("pmtSource", "sm_manager");
        params.put("pmtName", "icon");
        params.put("pageId", itemRequest.getLevel1Id());
        params.put("moduleId", itemRequest.getLevel2Id());
        params.put("tagId", itemRequest.getLevel3Id());
        params.put("rtNextDayStoreId", Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getLocParams).map(LocParams::getRtNextDayStoreId).map(Object::toString).orElse("0"));
        params.put("rtHalfDayStoreId", Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getLocParams).map(LocParams::getRtHalfDayStoreId).map(Object::toString).orElse("0"));
        params.put("rt1HourStoreId",  Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getLocParams).map(LocParams::getRt1HourStoreId).map(Object::toString).orElse("0"));
        params.put("smAreaId",  Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getLocParams).map(LocParams::getSmAreaId).map(Object::toString).orElse("0"));
        params.put("itemBusinessType", Joiner.on(",").join(businessList));
        Map<String,Object> requestParams = sgFrameworkContextItem.getRequestParams();
        Integer index = 0;
        if(MapUtils.isNotEmpty(requestParams)){
            index = MapUtil.getIntWithDefault(requestParams,"index",0);
        }
        params.put("isFirstPage", index > 0 ? "false" : "true");

        params.put("logicAreaId", Joiner.on(",").join(Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams)
                .map(CommonUserParams::getLocParams).map(LocParams::getLogicIdByPriority).orElse(
                Lists.newArrayList())));


        params.put("userNick", Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getUserDO).map(UserDO::getNick).orElse(""));


        return recommendRequest;
    }
}
