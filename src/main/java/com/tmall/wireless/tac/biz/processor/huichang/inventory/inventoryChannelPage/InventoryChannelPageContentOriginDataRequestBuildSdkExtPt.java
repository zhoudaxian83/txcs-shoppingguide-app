package com.tmall.wireless.tac.biz.processor.huichang.inventory.inventoryChannelPage;

import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.tmall.aselfcaptain.util.StackTraceUtil;
import com.tmall.tcls.gs.sdk.biz.uti.MapUtil;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.constant.RequestKeyConstant;
import com.tmall.tcls.gs.sdk.framework.model.context.LocParams;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextContent;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallCommonAldConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.utils.PageUrlUtil;
import com.tmall.wireless.tac.biz.processor.huichang.common.utils.ParseCsa;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import lombok.SneakyThrows;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * Tpp请求场景，入参组装
 */
@SdkExtension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
        useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
        scenario = HallScenarioConstant.HALL_SCENARIO_SCENARIO_INVENTORY_CHANNEL_PAGE)
public class InventoryChannelPageContentOriginDataRequestBuildSdkExtPt extends Register implements ContentOriginDataRequestBuildSdkExtPt {
    @Autowired
    TacLogger tacLogger;

    private static final Long DEFAULT_SMAREAID = 310100L;
    private static final Long DEFAULT_LOGAREAID = 107L;
    private static final String SCENE_SET_PREFIX = "intelligentCombinationItems_";
    public static final Long APPID = 26563L;  //Todo likunlin
    private static final Long DEFAULT_USERID = 0L; // Todo likunlin
    private static final int PAGE_SIZE = 1; //Todo likunlin

    @Override
    public RecommendRequest process(SgFrameworkContextContent sgFrameworkContextContent) {
        tacLogger.debug("扩展点InventoryChannelPageContentOriginDataRequestBuildSdkExtPt");
        RecommendRequest recommendRequest = new RecommendRequest();
        try{
            Context context = sgFrameworkContextContent.getTacContext();
            RequestContext4Ald requestContext4Ald = (RequestContext4Ald) context;
            Map<String, Object> aldParams = requestContext4Ald.getAldParam();
            Map<String, Object> aldContext = requestContext4Ald.getAldContext();
            Map<String, String> params = Maps.newHashMap();
            if(MapUtils.isEmpty(aldParams)) {
                tacLogger.debug("aldParams数据缺失");
                throw new Exception("aldParams数据缺失");
            }
            if(MapUtils.isEmpty(aldContext)) {
                tacLogger.debug("aldContext数据缺失");
                aldContext = Maps.newHashMap();
            }

            tacLogger.debug("aldParams: " + JSONObject.toJSONString(aldParams));
            int pageIndex = Optional.ofNullable(PageUrlUtil.getParamFromCurPageUrl(aldParams, "pageIndex", tacLogger)).map(Integer::valueOf).orElse(MapUtil.getIntWithDefault(aldParams, "pageIndex", 1));
            int index = (pageIndex - 1) * PAGE_SIZE;
            params.put("index", String.valueOf(index));

            params.put("pageSize", String.valueOf(PAGE_SIZE));
            Long smAreaId = MapUtil.getLongWithDefault(aldParams, RequestKeyConstant.SMAREAID, DEFAULT_SMAREAID);
            params.put("smAreaId", String.valueOf(smAreaId));

            LocParams locParams = ParseCsa.parseCsaObj(aldParams.get(RequestKeyConstant.USER_PARAMS_KEY_CSA), smAreaId);
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

            String sceneSet = PageUrlUtil.getParamFromCurPageUrl(aldParams, "contentSetId", tacLogger); // Todo likunlin
            if(!sceneSet.contains(SCENE_SET_PREFIX)) {
                sceneSet = SCENE_SET_PREFIX + sceneSet;
            }
            params.put("sceneSet", sceneSet); // 场景集id

            String sceneExclude = PageUrlUtil.getParamFromCurPageUrl(aldParams, "filterContentIds", tacLogger); // Todo likunlin
            if(StringUtils.isNotBlank(sceneExclude)) {
                params.put("sceneExclude", sceneExclude); // 过滤的场景
            }
            // 这个参数名就是entryContentIds，这里该参数只会带一个id
            String sceneTop = PageUrlUtil.getParamFromCurPageUrl(aldParams, "entryContentIds", tacLogger); // Todo likunlin
            if(StringUtils.isNotBlank(sceneTop)) {
                params.put("sceneTop", sceneTop); // 置顶的场景
            }

            recommendRequest.setAppId(APPID);
            recommendRequest.setUserId(MapUtil.getLongWithDefault(aldContext, HallCommonAldConstant.USER_ID, DEFAULT_USERID));
            recommendRequest.setParams(params);
            recommendRequest.setLogResult(true);
            tacLogger.debug("Tpp请求参数是：" + JSONObject.toJSONString(recommendRequest));
            return recommendRequest;
        } catch (Exception e) {
            tacLogger.debug("扩展点InventoryChannelPageOriginDataRequestBuildSdkExtPt 失败" + StackTraceUtil.stackTrace(e));
//            throw new Exception("扩展点InventoryChannelPageOriginDataRequestBuildSdkExtPt 失败", e);
            return recommendRequest;
        }
    }
}
