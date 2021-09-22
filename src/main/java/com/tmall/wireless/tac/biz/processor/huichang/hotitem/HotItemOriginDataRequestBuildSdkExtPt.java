package com.tmall.wireless.tac.biz.processor.huichang.hotitem;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 请求Tpp参数
 */
@SdkExtension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
    useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
    scenario = HallScenarioConstant.HALL_SCENARIO_HOT_ITEM)
public class HotItemOriginDataRequestBuildSdkExtPt extends Register implements ItemOriginDataRequestBuildSdkExtPt {

    Logger logger = LoggerFactory.getLogger(HotItemOriginDataRequestBuildSdkExtPt.class);
    private static final String ITEM_SET_PREFIX = "crm_";
    public static final Long DEFAULT_LOGAREAID = 107L;
    public static final Long SCENE_ITEM_RECOMMEND_APPID = 27753L;
    public static final Long DEFAULT_SMAREAID = 330100L;
    public static final int PAGE_SIZE = 200;
    private static final Long DefaultUserId = 0L;

    @Autowired
    TacLogger tacLogger;

    //https://tui.taobao.com/recommend?appid=27753&pageSize=2&index=0&itemSets=crm_378428
    // &commerce=B2C&smAreaId=330200&_devEnv_=0&regionCode=107&exposureDataUserId=FY
    // &itemAndIndustry=649361494634:1100:1;651103243384:1200:1
    @Override
    public RecommendRequest process(SgFrameworkContextItem sgFrameworkContextItem) {
        logger.error("-----HotItemOriginDataRequestBuildSdkExtPt.start----");
        tacLogger.debug("HotItemOriginDataRequestBuildSdkExtPt");
        RecommendRequest recommendRequest = new RecommendRequest();
        try {
            Context context = sgFrameworkContextItem.getTacContext();
            RequestContext4Ald requestContext4Ald = (RequestContext4Ald)context;
            Map<String, Object> aldParams = requestContext4Ald.getAldParam();
            Map<String, Object> aldContext = requestContext4Ald.getAldContext();
            Map<String, Object> customParams = requestContext4Ald.getParams();
            Map<String, String> params = Maps.newHashMap();
            Object aldStaticData = aldContext.get(HallCommonAldConstant.STATIC_SCHEDULE_DATA);
            if(aldStaticData == null){
                throw new Exception("获取阿拉丁静态数据为空");
            }
            List<Map<String, Object>> aldStaticDataList = (List<Map<String, Object>>)aldStaticData;
            Map<Long, Map<String, Object>> aldStaticDataMap = aldStaticDataList.stream().collect(
                Collectors.toMap(e -> Long.valueOf(String.valueOf(e.get("contentId"))), Function.identity(), (key1, key2) -> key2));
            customParams.put("aldStaticDataMap", aldStaticDataMap);

            String itemAndIndustryData = convertStaticData(aldStaticDataList);
            params.put("itemAndIndustry", itemAndIndustryData);
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

            //String itemSetId = "";
            //String tacParams = MapUtil.getStringWithDefault(aldParams, "tacParams", "");
            //if (StringUtils.isNotBlank(tacParams)) {
            //    JSONObject tacParamsMap = JSON.parseObject(tacParams);
            //    itemSetId = Optional.ofNullable(tacParamsMap.getString(HallCommonAldConstant.ITEM_SET_ID)).orElse("");
            //}
            //if (StringUtils.isEmpty(itemSetId)) {
            //    throw new Exception("itemSetId is empty");
            //}

            //String itemSetId = MapUtil.getStringWithDefault(aldParams, HallCommonAldConstant.ITEM_SET_ID, "");
            String itemSetId = "398287";
            logger.error("-----HotItemOriginDataRequestBuildSdkExtPt.itemSetId:{}", itemSetId);
            params.put("commerce", "B2C");
            params.put("index", "0"); // 不要求分页
            params.put("pageSize", String.valueOf(PAGE_SIZE));
            params.put("itemSets", ITEM_SET_PREFIX + itemSetId);

            recommendRequest.setAppId(SCENE_ITEM_RECOMMEND_APPID);
            //TODO 需要处理设备id
            recommendRequest.setUserId(
                MapUtil.getLongWithDefault(aldContext, HallCommonAldConstant.USER_ID, DefaultUserId));
            recommendRequest.setParams(params);
            recommendRequest.setLogResult(true);
            tacLogger.debug("Tpp参数：" + JSONObject.toJSONString(recommendRequest));
            return recommendRequest;
        } catch (Exception e) {
            tacLogger.debug(
                "HotItemOriginDataRequestBuildSdkExtPt 失败" + StackTraceUtil.stackTrace(e));
            logger.error("HotItemOriginDataRequestBuildSdkExtPt 失败.", e);
            return recommendRequest;
        }
    }

    //649361494634:1100:1;651103243384:1200:1
    //商品id：行业id：数量
    private String convertStaticData(List<Map<String, Object>> aldStaticDataList){
        StringBuilder sb = new StringBuilder();
        for(Map<String, Object> map : aldStaticDataList){
            Object industryId = map.get("industryId");
            Object showNum = map.get("showNum");
            Object itemId = map.get("contentId");
            sb.append(itemId).append(":").append(industryId).append(":").append(showNum).append(";");
        }
        return sb.toString();
    }

}
