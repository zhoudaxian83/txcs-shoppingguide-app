package com.tmall.wireless.tac.biz.processor.gsh.itemsetrecommend;

import java.util.Map;
import java.util.Optional;

import com.alibaba.fastjson.JSON;
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
import com.tmall.wireless.tac.biz.processor.huichang.common.utils.ParseCsa;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 请求Tpp参数
 */
@SdkExtension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
    useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_GSH,
    scenario = HallScenarioConstant.HALL_SCENARIO_SCENARIO_GSH_ITEM_SET_RECOMMEND)
public class GshItemSetOriginDataRequestBuildSdkExtPt extends Register implements ItemOriginDataRequestBuildSdkExtPt {

    Logger logger = LoggerFactory.getLogger(GshItemSetOriginDataRequestBuildSdkExtPt.class);
    private static final String ITEM_SET_PREFIX = "rb_";
    public static final Long DEFAULT_LOGAREAID = 107L;
    public static final Long ITEM_SET_RECOMMEND_APPID = 28364L;
    public static final Long DEFAULT_SMAREAID = 330100L;
    public static final int PAGE_SIZE = 20;
    private static final Long DefaultUserId = 0L;

    @Autowired
    TacLogger tacLogger;

    @Override
    public RecommendRequest process(SgFrameworkContextItem sgFrameworkContextItem) {
        logger.error("-----EdItemSetOriginDataRequestBuildSdkExtPt.start----");
        tacLogger.debug("EdItemSetOriginDataRequestBuildSdkExtPt");
        RecommendRequest recommendRequest = new RecommendRequest();
        try {
            recommendRequest.setLogResult(true);
            Context context = sgFrameworkContextItem.getTacContext();
            RequestContext4Ald requestContext4Ald = (RequestContext4Ald)context;
            Map<String, Object> aldParams = requestContext4Ald.getAldParam();
            Map<String, Object> aldContext = requestContext4Ald.getAldContext();

            Map<String, String> params = Maps.newHashMap();
            Long smAreaId = MapUtil.getLongWithDefault(aldParams, RequestKeyConstant.SMAREAID, DEFAULT_SMAREAID);
            params.put("smAreaId", String.valueOf(smAreaId));

            Object csa = aldParams.get(RequestKeyConstant.USER_PARAMS_KEY_CSA);
            LocParams locParams = null;
            if (csa == null) {
                tacLogger.debug("csa为空");
            } else {
                locParams = ParseCsa.parseCsaObj(csa, smAreaId);
                if (locParams == null) {
                    tacLogger.debug("csa解析异常");
                }
            }

            params.put("regionCode", Optional.ofNullable(locParams).map(locParams1 -> locParams1.getRegionCode())
                .map(String::valueOf).orElse(String.valueOf(DEFAULT_LOGAREAID)));

            String itemSetId = "";
            String tacParams = MapUtil.getStringWithDefault(aldParams, "tacParams", "");
            if (StringUtils.isNotBlank(tacParams)) {
                JSONObject tacParamsMap = JSON.parseObject(tacParams);
                itemSetId = Optional.ofNullable(tacParamsMap.getString(HallCommonAldConstant.ITEM_SET_ID)).orElse("");
            }
            if (StringUtils.isEmpty(itemSetId)) {
                logger.error("-----EdItemSetOriginDataRequestBuildSdkExtPt.itemSetId is empty-------");
                throw new Exception("itemSetId is empty");
            }
            int pageIndex = MapUtil.getIntWithDefault(aldParams, "pageIndex", 0);
            params.put("commerce", "B2C");
            int index = pageIndex * PAGE_SIZE;
            params.put("index", String.valueOf(index));
            params.put("pageSize", String.valueOf(PAGE_SIZE));
            params.put("itemSets", ITEM_SET_PREFIX + itemSetId);

            recommendRequest.setAppId(ITEM_SET_RECOMMEND_APPID);
            recommendRequest.setUserId(
                MapUtil.getLongWithDefault(aldContext, HallCommonAldConstant.USER_ID, DefaultUserId));
            recommendRequest.setParams(params);
            tacLogger.debug("Tpp参数：" + JSONObject.toJSONString(recommendRequest));
            return recommendRequest;
        } catch (Exception e) {
            tacLogger.debug(
                "EdItemSetOriginDataRequestBuildSdkExtPt 失败" + StackTraceUtil.stackTrace(e));
            logger.error("EdItemSetOriginDataRequestBuildSdkExtPt 失败.", e);
            return recommendRequest;
        }
    }

}
