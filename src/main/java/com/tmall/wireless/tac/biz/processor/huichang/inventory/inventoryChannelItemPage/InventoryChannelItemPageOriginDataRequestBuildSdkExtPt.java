package com.tmall.wireless.tac.biz.processor.huichang.inventory.inventoryChannelItemPage;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.tmall.tcls.gs.sdk.biz.uti.MapUtil;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.constant.RequestKeyConstant;
import com.tmall.tcls.gs.sdk.framework.model.context.LocParams;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextItem;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallCommonAldConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.utils.PageUrlUtil;
import com.tmall.wireless.tac.biz.processor.huichang.common.utils.ParseCsa;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Optional;

@SdkExtension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
        useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
        scenario = HallScenarioConstant.HALL_SCENARIO_SCENARIO_INVENTORY_CHANNEL_ITEM_PAGE)
public class InventoryChannelItemPageOriginDataRequestBuildSdkExtPt extends Register implements ItemOriginDataRequestBuildSdkExtPt {
    private static final String ITEM_SET_PREFIX = "crm_";
    public static final Long DefaultLogAreaId = 107L;
    public static final Long SCENE_ITEM_RECOMMEND_APPID = 26562L;
    public static final Long DefaultSmAreaId = 330100L;
    public static final int PAGE_SIZE = 10;
    private static final Long DefaultUserId = 0L;

    @Autowired
    TacLogger tacLogger;

    @SneakyThrows
    @Override
    public RecommendRequest process(SgFrameworkContextItem sgFrameworkContextItem) {
        tacLogger.debug("扩展点InventoryChannelItemPageOriginDataRequestBuildSdkExtPt");
        Context context = sgFrameworkContextItem.getTacContext();
        RequestContext4Ald requestContext4Ald = (RequestContext4Ald) context;
        Map<String, Object> aldParams = requestContext4Ald.getAldParam();
        Map<String, Object> aldContext = requestContext4Ald.getAldContext();

        Map<String, String> params = Maps.newHashMap();
        String itemSets = PageUrlUtil.getParamFromCurPageUrl(aldParams, "itemSet", tacLogger); // Todo likunlin
        if(StringUtils.isNotBlank(itemSets)) {
            if(!itemSets.startsWith(ITEM_SET_PREFIX)) {
                itemSets = ITEM_SET_PREFIX + itemSets;
            }
            params.put("itemSets",itemSets);
        } else {
            tacLogger.debug("url参数itemSet为空");
            throw new Exception("url参数itemSet为空");
        }

        String scene = PageUrlUtil.getParamFromCurPageUrl(aldParams, "contentId", tacLogger); // Todo likunlin
        if(StringUtils.isNotBlank(scene)) {
            params.put("scene", scene);
        } else {
            tacLogger.debug("url参数contentId为空");
            throw new Exception("url参数contentId为空");
        }

        Long smAreaId = MapUtil.getLongWithDefault(aldParams, RequestKeyConstant.SMAREAID, DefaultSmAreaId);
        params.put("smAreaId", String.valueOf(smAreaId));

        LocParams locParams = ParseCsa.parseCsaObj(aldParams.get(RequestKeyConstant.USER_PARAMS_KEY_CSA), smAreaId);
        params.put("regionCode", String.valueOf(Optional.ofNullable(locParams.getRegionCode()).orElse(DefaultLogAreaId)));

        String locType = PageUrlUtil.getParamFromCurPageUrl(aldParams, "locType", tacLogger);
        if("B2C".equals(locType) || locType == null){
            params.put("commerce","B2C");
        }else {
            params.put("commerce","O2O");
            if (Optional.of(locParams.getRt1HourStoreId()).orElse(0L) > 0){
                params.put("rtOneHourStoreId", String.valueOf(locParams.getRt1HourStoreId()));
            }else if(Optional.of(locParams.getRtHalfDayStoreId()).orElse(0L) > 0){
                params.put("rtHalfDayStoreId", String.valueOf(locParams.getRtHalfDayStoreId()));
            }
        }

        String index = PageUrlUtil.getParamFromCurPageUrl(aldParams, "index", tacLogger);
        if(StringUtils.isNotBlank(index)) {
            params.put("index", index);
        } else {
            params.put("index", String.valueOf(Optional.ofNullable(aldParams.get("pageIndex")).orElse("0"))); // Todo
        }

        params.put("pageSize", String.valueOf(PAGE_SIZE)); //

        RecommendRequest recommendRequest = new RecommendRequest();
        recommendRequest.setAppId(SCENE_ITEM_RECOMMEND_APPID);
        recommendRequest.setUserId(MapUtil.getLongWithDefault(aldContext, HallCommonAldConstant.USER_ID, DefaultUserId));
        recommendRequest.setParams(params);
        recommendRequest.setLogResult(true);
        tacLogger.debug("Tpp参数：" + JSONObject.toJSONString(recommendRequest));
        return recommendRequest;
    }
}
