package com.tmall.wireless.tac.biz.processor.icon.item.ext;


import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.tmall.crowd.guava.collect.Maps;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.*;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.icon.item.ItemRecommendService;
import com.tmall.wireless.tac.biz.processor.icon.item.ItemRequest;
import com.tmall.wireless.tac.biz.processor.icon.level2.BusinessTypeUtil;
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


    @Override
    public RecommendRequest process(SgFrameworkContextItem sgFrameworkContextItem) {

        ItemRequest itemRequest = (ItemRequest) Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getTacContext).map(c -> c.get(ItemRecommendService.ITEM_REQUEST_KEY)).orElse(null);
        Preconditions.checkArgument(itemRequest != null);

        RecommendRequest recommendRequest = new RecommendRequest();

        // todo fixme
        recommendRequest.setAppId(18611L);
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

        recommendRequest.setUserId(Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getUserDO).map(UserDO::getUserId).orElse(0L));
        params.put("pmtSource", "sm_manager");
        params.put("pmtName", "icon");
        params.put("pageId", itemRequest.getLevel1Id());
        params.put("moduleId", itemRequest.getLevel2Id());
        params.put("tagId", itemRequest.getLevel3Id());
        params.put("rtNextDayStoreId", Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getLocParams).map(LocParams::getRtNextDayStoreId).map(Object::toString).orElse("0"));
        params.put("rtHalfDayStoreId", Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getLocParams).map(LocParams::getRtHalfDayStoreId).map(Object::toString).orElse("0"));
        params.put("rtOneHourStoreId",  Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getLocParams).map(LocParams::getRt1HourStoreId).map(Object::toString).orElse("0"));
        params.put("smAreaId",  Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getLocParams).map(LocParams::getSmAreaId).map(Object::toString).orElse("0"));
        params.put("itemBusinessType", Joiner.on(",").join(businessList));

        params.put("logicAreaId", Joiner.on(",").join(Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams)
                .map(CommonUserParams::getLocParams).map(LocParams::getLogicIdByPriority).orElse(
                Lists.newArrayList())));


        params.put("userNick", Optional.of(sgFrameworkContextItem).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getUserDO).map(UserDO::getNick).orElse(""));


        return recommendRequest;
    }
}
