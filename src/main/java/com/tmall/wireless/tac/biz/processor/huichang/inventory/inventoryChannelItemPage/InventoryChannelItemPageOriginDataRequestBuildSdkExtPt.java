package com.tmall.wireless.tac.biz.processor.huichang.inventory.inventoryChannelItemPage;

import java.util.Map;
import java.util.Optional;

import com.alibaba.fastjson.JSONObject;

import com.google.common.collect.Maps;
import com.tmall.aselfcaptain.util.StackTraceUtil;
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
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

@SdkExtension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
        useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
        scenario = HallScenarioConstant.HALL_SCENARIO_SCENARIO_INVENTORY_CHANNEL_ITEM_PAGE)
public class InventoryChannelItemPageOriginDataRequestBuildSdkExtPt extends Register implements ItemOriginDataRequestBuildSdkExtPt {
    private static final String ITEM_SET_PREFIX = "crm_";
    public static final Long DEFAULT_LOGAREAID = 107L;
    public static final Long SCENE_ITEM_RECOMMEND_APPID = 24179L;
    public static final Long DEFAULT_SMAREAID = 330100L;
    public static final int PAGE_SIZE = 40;
    private static final Long DefaultUserId = 0L;

    @Autowired
    TacLogger tacLogger;

    @Override
    public RecommendRequest process(SgFrameworkContextItem sgFrameworkContextItem) {
        tacLogger.debug("扩展点InventoryChannelItemPageOriginDataRequestBuildSdkExtPt");
        RecommendRequest recommendRequest = new RecommendRequest();
        try{
            Context context = sgFrameworkContextItem.getTacContext();
            RequestContext4Ald requestContext4Ald = (RequestContext4Ald) context;
            Map<String, Object> aldParams = requestContext4Ald.getAldParam();
            Map<String, Object> aldContext = requestContext4Ald.getAldContext();
            if(MapUtils.isEmpty(aldParams)) {
                tacLogger.debug("aldParams数据缺失");
                throw new Exception("aldParams数据缺失");
            }
            if(MapUtils.isEmpty(aldContext)) {
                tacLogger.debug("aldContext数据缺失");
                aldContext = Maps.newHashMap();
            }

            Map<String, String> params = Maps.newHashMap();
            String scene = PageUrlUtil.getParamFromCurPageUrl(aldParams, "contentId", tacLogger); // Todo likunlin
            if(StringUtils.isNotBlank(scene)) {
                params.put("scene", scene);
            } else {
                // 强依赖参数scene，如果缺失，直接异常
                tacLogger.debug("url参数contentId为空");
                throw new Exception("url参数contentId为空");
            }

            String itemSets = PageUrlUtil.getParamFromCurPageUrl(aldParams, "itemSetId", tacLogger); // Todo likunlin
            if(StringUtils.isNotBlank(itemSets)) {
                if(!itemSets.startsWith(ITEM_SET_PREFIX)) {
                    itemSets = ITEM_SET_PREFIX + itemSets;
                }
                params.put("itemSets",itemSets);
            } else {
                // 强依赖参数itemSets，如果缺失，直接异常
                tacLogger.debug("url参数itemSet为空");
                throw new Exception("url参数itemSet为空");
            }

            Long smAreaId = MapUtil.getLongWithDefault(aldParams, RequestKeyConstant.SMAREAID, DEFAULT_SMAREAID);
            params.put("smAreaId", String.valueOf(smAreaId));

            Object csa = aldParams.get(RequestKeyConstant.USER_PARAMS_KEY_CSA);
            LocParams locParams = null;
            if(csa == null){
                tacLogger.debug("csa为空");
            } else {
                locParams = ParseCsa.parseCsaObj(csa, smAreaId);
                if(locParams == null) {
                    tacLogger.debug("csa解析异常");
                }
            }

            params.put("regionCode", Optional.ofNullable(locParams).map(locParams1 -> locParams1.getRegionCode()).map(String::valueOf).orElse(String.valueOf(DEFAULT_LOGAREAID)));

            String locType = PageUrlUtil.getParamFromCurPageUrl(aldParams, "locType", tacLogger);
            if("B2C".equals(locType) || locType == null){
                params.put("commerce","B2C");
            }else {
                params.put("commerce","O2O");
                if (Optional.ofNullable(locParams).map(locParams1 -> locParams1.getRt1HourStoreId()).orElse(0L) > 0){
                    params.put("rtOneHourStoreId", String.valueOf(locParams.getRt1HourStoreId()));
                }else if(Optional.ofNullable(locParams).map(locParams1 -> locParams1.getRtHalfDayStoreId()).orElse(0L) > 0){
                    params.put("rtHalfDayStoreId", String.valueOf(locParams.getRtHalfDayStoreId()));
                }
            }

            params.put("index", "0"); // 不要求分页
            params.put("pageSize", String.valueOf(PAGE_SIZE));

            recommendRequest.setAppId(SCENE_ITEM_RECOMMEND_APPID);
            recommendRequest.setUserId(MapUtil.getLongWithDefault(aldContext, HallCommonAldConstant.USER_ID, DefaultUserId));
            recommendRequest.setParams(params);
            recommendRequest.setLogResult(true);
            tacLogger.debug("Tpp参数：" + JSONObject.toJSONString(recommendRequest));
            return recommendRequest;
        } catch (Exception e) {
            tacLogger.debug("扩展点InventoryChannelItemPageOriginDataRequestBuildSdkExtPt 失败" + StackTraceUtil.stackTrace(e));
            return recommendRequest;
        }
    }
}
