package com.tmall.wireless.tac.biz.processor.huichang.inventory.inventoryChannelItemPage;

import com.google.common.collect.Maps;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextItem;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.utils.PageUrlUtil;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.Optional;

@SdkExtension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
        useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
        scenario = HallScenarioConstant.HALL_SCENARIO_SCENARIO_INVENTORY_CHANNEL_ITEM_PAGE)
public class InventoryChannelItemPageOriginDataRequestBuildSdkExtPt extends Register implements ItemOriginDataRequestBuildSdkExtPt {
    private static final String ITEM_SET_PREFIX = "crm_";
    public static final Long defaultLogAreaId = 107L;
    public static final Long SCENE_ITEM_RECOMMEND_APPID = 26562L;
    public static final Long defaultSmAreaId = 330100L;
    public static final int DEFAULT_PAGE_SIZE = 10;

    @SneakyThrows
    @Override
    public RecommendRequest process(SgFrameworkContextItem sgFrameworkContextItem) {
        Context context = sgFrameworkContextItem.getTacContext();
        RequestContext4Ald requestContext4Ald = (RequestContext4Ald) context;
        Map<String, Object> aldParams = requestContext4Ald.getParams();

        Map<String, String> params = Maps.newHashMap();
        String itemSets = PageUrlUtil.getParamFromCurPageUrl(aldParams, "itemSet"); // Todo likunlin
        if(StringUtils.isNotBlank(itemSets)) {
            if(!itemSets.startsWith(ITEM_SET_PREFIX)) {
                itemSets = ITEM_SET_PREFIX + itemSets;
            }
            params.put("itemSets",itemSets);
        } else {
            throw new Exception("url参数itemSet为空");
        }

        String scene = PageUrlUtil.getParamFromCurPageUrl(aldParams, "contentId"); // Todo likunlin
        if(StringUtils.isNotBlank(scene)) {
            params.put("scene", scene);
        } else {
            throw new Exception("url参数contentId为空");
        }

        String locType = PageUrlUtil.getParamFromCurPageUrl(aldParams, "locType");
        if("B2C".equals(locType) || locType == null){
            params.put("commerce","B2C");
        }else {
            params.put("commerce","O2O");
            if (Optional.ofNullable(sgFrameworkContextItem.getCommonUserParams().getLocParams().getRt1HourStoreId()).orElse(0L) > 0){
                params.put("rtOneHourStoreId", String.valueOf(sgFrameworkContextItem.getCommonUserParams().getLocParams().getRt1HourStoreId()));
            }else if(Optional.ofNullable(sgFrameworkContextItem.getCommonUserParams().getLocParams().getRtHalfDayStoreId()).orElse(0L) > 0){
                params.put("rtHalfDayStoreId", String.valueOf(sgFrameworkContextItem.getCommonUserParams().getLocParams().getRtHalfDayStoreId()));
            }
        }
        params.put("index", String.valueOf(Optional.ofNullable(sgFrameworkContextItem.getCommonUserParams().getUserPageInfo().getIndex()).orElse(0)));
        params.put("regionCode", String.valueOf(Optional.ofNullable(sgFrameworkContextItem.getCommonUserParams().getLocParams().getRegionCode()).orElse(defaultLogAreaId)));
        params.put("smAreaId", String.valueOf(Optional.ofNullable(sgFrameworkContextItem.getCommonUserParams().getLocParams().getSmAreaId()).orElse(defaultSmAreaId)));
        params.put("pageSize", String.valueOf(Optional.ofNullable(sgFrameworkContextItem.getCommonUserParams().getUserPageInfo().getPageSize()).orElse(DEFAULT_PAGE_SIZE))); //

//        params.put("appId", String.valueOf(SCENE_ITEM_RECOMMEND_APPID));
//        params.put("userId", String.valueOf(sgFrameworkContextItem.getCommonUserParams().getUserDO().getUserId()));

        RecommendRequest recommendRequest = new RecommendRequest();
        recommendRequest.setAppId(SCENE_ITEM_RECOMMEND_APPID);
        recommendRequest.setUserId(Optional.ofNullable(sgFrameworkContextItem.getCommonUserParams().getUserDO().getUserId()).orElse(0L));
        recommendRequest.setParams(params);
        recommendRequest.setLogResult(true);
        return recommendRequest;
    }
}
