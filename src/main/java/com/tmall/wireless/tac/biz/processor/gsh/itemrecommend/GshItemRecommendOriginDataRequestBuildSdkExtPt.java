package com.tmall.wireless.tac.biz.processor.gsh.itemrecommend;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.alibaba.fastjson.JSONObject;

import com.google.common.base.Joiner;
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
    scenario = HallScenarioConstant.HALL_SCENARIO_SCENARIO_GSH_ITEM_RECOMMEND)
public class GshItemRecommendOriginDataRequestBuildSdkExtPt extends Register implements ItemOriginDataRequestBuildSdkExtPt {

    Logger logger = LoggerFactory.getLogger(GshItemRecommendOriginDataRequestBuildSdkExtPt.class);
    public static final Long DEFAULT_LOGAREAID = 107L;
    public static final Long ITEM_RECOMMEND_APPID = 28097L;
    public static final Long DEFAULT_SMAREAID = 330100L;
    public static final int PAGE_SIZE = 200;
    private static final Long DefaultUserId = 0L;

    @Autowired
    TacLogger tacLogger;

    @Override
    public RecommendRequest process(SgFrameworkContextItem sgFrameworkContextItem) {
        logger.error("-----GshItemRecommendOriginDataRequestBuildSdkExtPt.start----");
        RecommendRequest recommendRequest = new RecommendRequest();
        try {
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

            Object staticScheduleData = aldContext.get(HallCommonAldConstant.STATIC_SCHEDULE_DATA);
            if(staticScheduleData == null){
                throw new Exception("数据未填写");
            }
            List<Map<String, Object>> staticScheduleDataList = (List<Map<String, Object>>)staticScheduleData;
            List<String> itemIdList= new ArrayList<>();
            for(Map<String, Object> data : staticScheduleDataList){
                String contentSetId = MapUtil.getStringWithDefault(data, "contentSetId", "");
                if(StringUtils.isNotEmpty(contentSetId)){
                    itemIdList.add(contentSetId);
                }
            }
            String itemIds = Joiner.on(",").join(itemIdList);
            params.put("itemIds", itemIds);

            params.put("commerce", "B2C");
            params.put("index", "0"); // 不要求分页
            params.put("pageSize", String.valueOf(PAGE_SIZE));

            recommendRequest.setAppId(ITEM_RECOMMEND_APPID);
            recommendRequest.setUserId(
                MapUtil.getLongWithDefault(aldContext, HallCommonAldConstant.USER_ID, DefaultUserId));
            recommendRequest.setParams(params);
            tacLogger.debug("Tpp参数：" + JSONObject.toJSONString(recommendRequest));
            return recommendRequest;
        } catch (Exception e) {
            logger.error("GshItemRecommendOriginDataRequestBuildSdkExtPt 失败.", e);
            return recommendRequest;
        }
    }


}
