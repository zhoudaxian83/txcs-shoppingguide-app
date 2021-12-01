package com.tmall.wireless.tac.biz.processor.guessWhatYouLike.promotion;

import java.util.Map;
import java.util.Optional;

import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.ali.com.google.common.base.Joiner;
import com.ali.com.google.common.collect.Maps;
import com.google.common.collect.Lists;
import com.tmall.aselfcommon.util.StackTraceUtil;
import com.tmall.tcls.gs.sdk.biz.uti.MapUtil;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.constant.RequestKeyConstant;
import com.tmall.tcls.gs.sdk.framework.model.context.CommonUserParams;
import com.tmall.tcls.gs.sdk.framework.model.context.LocParams;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContext;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextItem;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.gsh.itemsetrecommend.GshItemSetOriginDataRequestBuildSdkExtPt;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallCommonAldConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.utils.ParseCsa;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zhongwei
 * @date 2021/12/1
 */
@SdkExtension(
    bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.SCENARIO_SUB_PROMOTION_PAGE
)
public class GulItemOriginDataRequestBuildSdkExtPt extends Register implements ItemOriginDataRequestBuildSdkExtPt {

    Logger logger = LoggerFactory.getLogger(GulItemOriginDataRequestBuildSdkExtPt.class);

    private static final String ITEM_SET_PREFIX = "rb_";
    public static final Long DEFAULT_LOGAREAID = 107L;
    public static final Long ITEM_SET_RECOMMEND_APPID = 29202L;
    public static final Long DEFAULT_SMAREAID = 330100L;
    public static final int PAGE_SIZE = 6;
    private static final Long DefaultUserId = 0L;

    @Autowired
    TacLogger tacLogger;

    @Override
    public RecommendRequest process(SgFrameworkContextItem sgFrameworkContextItem) {
        tacLogger.debug("GulItemOriginDataRequestBuildSdkExtPt");
        RecommendRequest recommendRequest = new RecommendRequest();
        try {

            //String resourceId = MapUtil.getStringWithDefault(aldContext, HallCommonAldConstant.ALD_CURRENT_RES_ID, "0");
            //params.put("resourceId", resourceId);

            recommendRequest.setLogResult(true);
            Context context = sgFrameworkContextItem.getTacContext();
            RequestContext4Ald requestContext4Ald = (RequestContext4Ald)context;
            Map<String, Object> aldParams = requestContext4Ald.getAldParam();
            Map<String, Object> aldContext = requestContext4Ald.getAldContext();
            Map<String, String> params = Maps.newHashMap();
            Long smAreaId = MapUtil.getLongWithDefault(aldParams, RequestKeyConstant.SMAREAID, DEFAULT_SMAREAID);
            params.put("smAreaId", String.valueOf(smAreaId));
            params.put("usingCrowd2I", "false");

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

            params.put("logicAreaId", Joiner.on(",").join(Optional.ofNullable(locParams)
                .map(LocParams::getLogicIdByPriority).orElse(
                    Lists.newArrayList())));

            int pageIndex = MapUtil.getIntWithDefault(aldParams, "pageIndex", 0);
            params.put("isFirstPage", pageIndex > 0 ? "false" : "true");

            Long rt1HourStoreId = Optional.ofNullable(locParams).map(LocParams::getRt1HourStoreId).orElse(0L);
            params.put("rt1HourStoreId", String.valueOf(rt1HourStoreId));

            Long rtHalfDayStoreId = Optional.ofNullable(locParams).map(LocParams::getRtHalfDayStoreId).orElse(0L);
            params.put("rtHalfDayStoreId", String.valueOf(rtHalfDayStoreId));

            params.put("closeSls", "0");

            String itemSetId = "";
            String tacParams = MapUtil.getStringWithDefault(aldParams, "tacParams", "");
            if (StringUtils.isNotBlank(tacParams)) {
                JSONObject tacParamsMap = JSON.parseObject(tacParams);
                itemSetId = Optional.ofNullable(tacParamsMap.getString(HallCommonAldConstant.ITEM_SET_ID)).orElse("");
            }
            if (StringUtils.isEmpty(itemSetId)) {
                logger.error("-----GulItemOriginDataRequestBuildSdkExtPt.itemSetId is empty-------");
                throw new Exception("itemSetId is empty");
            }
            params.put("itemSetIdList", itemSetId);
            // todo  先写死
            params.put("itemBusinessType","B2C");

            recommendRequest.setAppId(ITEM_SET_RECOMMEND_APPID);
            recommendRequest.setUserId(
                MapUtil.getLongWithDefault(aldContext, HallCommonAldConstant.USER_ID, DefaultUserId));
            recommendRequest.setParams(params);

            tacLogger.debug("Tpp参数：" + JSONObject.toJSONString(recommendRequest));
            return recommendRequest;
        } catch (Exception e) {
            tacLogger.debug(
                "GulItemOriginDataRequestBuildSdkExtPt 失败" + StackTraceUtil.stackTrace(e));
            return recommendRequest;
        }
    }
}
