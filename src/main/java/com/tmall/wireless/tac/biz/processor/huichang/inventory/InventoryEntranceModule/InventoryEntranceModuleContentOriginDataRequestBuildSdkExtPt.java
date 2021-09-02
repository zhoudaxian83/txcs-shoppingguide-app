package com.tmall.wireless.tac.biz.processor.huichang.inventory.InventoryEntranceModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.alibaba.aladdin.lamp.domain.request.Request;
import com.alibaba.aladdin.lamp.domain.request.RequestItem;
import com.alibaba.aladdin.lamp.domain.request.modules.LocationInfo;
import com.alibaba.aladdin.lamp.domain.response.ResResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.google.common.collect.Lists;
import com.taobao.mtop.api.agent.MtopContext;
import com.tmall.aself.shoppingguide.client.loc.domain.AddressDTO;
import com.tmall.aself.shoppingguide.client.loc.util.AddressUtil;
import com.tmall.tcls.gs.sdk.biz.uti.MapUtil;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.constant.RequestKeyConstant;
import com.tmall.tcls.gs.sdk.framework.model.context.LocParams;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextContent;
import com.tmall.txcs.gs.spi.recommend.AldSpi;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.config.SxlSwitch;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallCommonAldConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.contentextpt.HallCommonContentOriginDataRequestBuildSdkExtPt;
import com.tmall.wireless.tac.biz.processor.huichang.common.utils.PageUrlUtil;
import com.tmall.wireless.tac.biz.processor.huichang.common.utils.ParseCsa;
import com.tmall.wireless.tac.biz.processor.huichang.common.utils.URLUtil;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.dataservice.TacOptLogger;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 请求tpp数据的入参
 *
 * @author wangguohui
 */
@SdkExtension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
    useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
    scenario = HallScenarioConstant.HALL_SCENARIO_SCENARIO_INVENTORY_ENTRANCE_MODULE)
public class InventoryEntranceModuleContentOriginDataRequestBuildSdkExtPt extends
    HallCommonContentOriginDataRequestBuildSdkExtPt
    implements ContentOriginDataRequestBuildSdkExtPt {

    @Autowired
    TacLogger tacLogger;

    @Autowired
    private TacOptLogger tacOptLogger;

    @Autowired
    private AldSpi aldSpi;

    Logger logger = LoggerFactory.getLogger(InventoryEntranceModuleContentOriginDataRequestBuildSdkExtPt.class);

    public static final Long SCENE_RECOMMEND_APPID = 27401L;

    private static final String prex_sceneSet = "intelligentCombinationItems_";

    private static final String APP_NAME = "txcs-shoppingguide-app";
    private static final Long defaultSmAreaId = 310100L;
    private static final Long defaultLogAreaId = 107L;

    public static final String yxsdPrefix = "SG_TMCS_1H_DS:";
    public static final String brdPrefix = "SG_TMCS_HALF_DAY_DS:";




    @Override
    public RecommendRequest process(SgFrameworkContextContent sgFrameworkContextContent) {
        RecommendRequest recommendRequest = new RecommendRequest();
        try{
            tacOptLogger.debug("tacOptLogger.InventoryEntranceModuleContentOriginDataRequestBuildSdkExtPt.start");
            logger.debug("logger.InventoryEntranceModuleContentOriginDataRequestBuildSdkExtPt.start");

            Context tacContext = sgFrameworkContextContent.getTacContext();
            RequestContext4Ald requestContext4Ald = (RequestContext4Ald)tacContext;
            Map<String, Object> aldParam = requestContext4Ald.getAldParam();//对应requestItem
            Map<String, Object> aldContext = requestContext4Ald.getAldContext();//对应solutionContext
            logger.info("logger.InventoryEntranceModuleContentOriginDataRequestBuildSdkExtPt.aldParam:{}", JSON.toJSONString(aldParam));
            logger.info("logger.InventoryEntranceModuleContentOriginDataRequestBuildSdkExtPt.aldContext:{}", JSON.toJSONString(aldContext));
            Map<String, String> tppRequestParams = new HashMap<>();

            Object aldCurrentResId = aldContext.get(HallCommonAldConstant.ALD_CURRENT_RES_ID);
            Object userId = aldContext.get(HallCommonAldConstant.USER_ID);
            tppRequestParams.put("resourceId", String.valueOf(aldCurrentResId));
            BizScenario bizScenario = sgFrameworkContextContent.getBizScenario();
            String uniqueIdentity = bizScenario.getUniqueIdentity();
            tppRequestParams.put("uniqueIdentity", uniqueIdentity);



            recommendRequest.setParams(tppRequestParams);
            recommendRequest.setLogResult(true);
            recommendRequest.setUserId(Long.valueOf(String.valueOf(userId)));
            recommendRequest.setAppId(SCENE_RECOMMEND_APPID);

            Map<String, Object> userParams = sgFrameworkContextContent.getUserParams();
            buildTppParams(tppRequestParams, aldParam, aldContext, userParams);
            String urlParamsByMap = URLUtil.getUrlParamsByMap(tppRequestParams);
            tacLogger.debug("urlParamsByMap:" + JSON.toJSONString(urlParamsByMap));
            logger.info("logger.InventoryEntranceModuleContentOriginDataRequestBuildSdkExtPt.urlParamsByMap:{}", JSON.toJSONString(urlParamsByMap));
            return recommendRequest;
        }catch (Exception e){
            tacLogger.error("tacLogger.InventoryEntranceModuleContentOriginDataRequestBuildSdkExtPt.error,", e);
            return recommendRequest;
        }

    }

    //http://tuipre.taobao.com/recommend?appid=27401&index=0&pageSize=3&commerce=B2C&smAreaId=330110
    // &sceneSet=intelligentCombinationItems_155012,intelligentCombinationItems_162002,
    // intelligentCombinationItems_153017&regionCode=107
    private void buildTppParams(Map<String, String> params, Map<String, Object> aldParams,
        Map<String, Object> aldContext, Map<String, Object> userParams) throws Exception{
        params.put("index", "0");


        Long smAreaId = MapUtil.getLongWithDefault(aldParams, HallCommonAldConstant.SM_AREAID, 330100L);
        params.put("smAreaId", String.valueOf(smAreaId));

        LocParams locParams = ParseCsa.parseCsaObj(aldParams.get(RequestKeyConstant.USER_PARAMS_KEY_CSA), smAreaId);

        params.put("regionCode", Optional.ofNullable(locParams).map(locParams1 -> locParams1.getRegionCode()).map(String::valueOf).orElse(String.valueOf(defaultLogAreaId)));

        //先从绑定的流程模板参数中拿，如果拿不到，代表是二跳页面的模块，这个时候从URL里面获取，如果还获取不到，则默认是B2C
        // 年羽改的
        String locType = "";
        String tacParams = MapUtil.getStringWithDefault(aldParams, "tacParams", "");
        if(StringUtils.isNotBlank(tacParams)){
            JSONObject tacParamsMap = JSON.parseObject(tacParams);
            locType = Optional.ofNullable(tacParamsMap.getString(HallCommonAldConstant.LOC_TYPE)).orElse("");
        }

        if(StringUtils.isEmpty(locType)){
             locType = PageUrlUtil.getParamFromCurPageUrl(aldParams, "locType");
             if(StringUtils.isEmpty(locType)){
                 locType = "B2C";
             }
        }
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
        Object staticScheduleData = null;
        String entryResourceId = PageUrlUtil.getParamFromCurPageUrl(aldParams, "entryResourceId");

        //需要在加个其他的参数来共同决定
        if(StringUtils.isNotEmpty(entryResourceId)){
            staticScheduleData = getAldStaticDataByResourceId(entryResourceId, aldParams, aldContext);
        }else {
            staticScheduleData = aldContext.get(HallCommonAldConstant.STATIC_SCHEDULE_DATA);
        }
        if(staticScheduleData == null){
            logger.error("staticScheduleData is empty.entryResourceId:{}", entryResourceId);
            throw new Exception("获取静态数据为空");

        }
        List<Map<String, Object>> staticScheduleDataList = (List<Map<String, Object>>)staticScheduleData;
        List<Map<String, String>> dealStaticDataList = new ArrayList<>();
        List<String> sceneSetIdList = new ArrayList<>();
        for(Map<String, Object> data : staticScheduleDataList){
            Map<String, String> dataMap = new HashMap<>();
            String contentSetId = MapUtil.getStringWithDefault(data, "contentSetId", "");
            sceneSetIdList.add(prex_sceneSet + contentSetId);
            String contentSetTitle = MapUtil.getStringWithDefault(data, "contentSetTitle", "");
            String contentSetSubTitle = MapUtil.getStringWithDefault(data, "contentSetSubTitle", "");
            dataMap.put("contentSetId", contentSetId);
            dataMap.put("contentSetTitle", contentSetTitle);
            dataMap.put("contentSetSubTitle", contentSetSubTitle);
            dealStaticDataList.add(dataMap);
        }

        params.put("sceneSet", String.join(",", sceneSetIdList)); // 场景集id
        //如果大于上限，那么就取上限值
        if(sceneSetIdList.size() > SxlSwitch.inventoryEntranceModuleQueryTppSizeLimit){
            params.put("pageSize", String.valueOf(SxlSwitch.inventoryEntranceModuleQueryTppSizeLimit)); // 场景集id
        }else {
            params.put("pageSize", String.valueOf(sceneSetIdList.size())); // 场景集id
        }

        params.put("appId", String.valueOf(SCENE_RECOMMEND_APPID));

        //把处理好的静态数从新设置一下，后面还需要
        userParams.put("dealStaticDataList", dealStaticDataList);
        userParams.put("locType", locType);

        //入口透出的场景id，需要在二跳页面的入口过滤掉
        String sceneExclude = PageUrlUtil.getParamFromCurPageUrl(aldParams, "filterContentIds"); // Todo likunlin
        if(org.apache.commons.lang.StringUtils.isNotBlank(sceneExclude)) {
            params.put("sceneExclude", sceneExclude); // 过滤的场景
        }


    }



    private Object getAldStaticDataByResourceId(String resourceId, Map<String, Object> aldParam, Map<String, Object> aldContext){
        Request request = buildAldRequest(resourceId, aldParam, aldContext);
        Map<String, ResResponse> aldResponseMap = aldSpi.queryAldInfoSync(request);
        if(MapUtils.isNotEmpty(aldResponseMap)){
            ResResponse resResponse = aldResponseMap.get(resourceId);
            Object data = resResponse.getData();
            return data;
        }
        return null;
    }

    private Request buildAldRequest(String resourceId, Map<String, Object> aldParams, Map<String, Object> aldContext) {
        Request request = new Request();

        request.setCallSource(APP_NAME);
        RequestItem item = new RequestItem();
        item.setCount(50);
        item.setResId(resourceId);
        JSONObject data = new JSONObject();
        //渠道参数，流程模板需要识别，识别到以后流程模板内部只返回静态数据，也就是只拿到商品id，不走渲染逻辑
        data.put("onlyGetStaticData", "true");
        item.setData(data);
        request.setRequestItems(Lists.newArrayList(item));
        //地址信息
        LocationInfo locationInfo = request.getLocationInfo();
        //四级地址

        AddressDTO addressDto;
        String csa = MapUtil.getStringWithDefault(aldParams, HallCommonAldConstant.CSA, "");
        if(StringUtils.isNotEmpty(csa)){
            addressDto = AddressUtil.parseCSA(csa);
            locationInfo.setCityLevel4(String.valueOf(addressDto.getDistrictId()));
            List<String> wdkCodes = Lists.newArrayList();
            if (addressDto.isRt1HourStoreCover()) {
                wdkCodes.add(yxsdPrefix + addressDto.getRt1HourStoreId());
            } else if(addressDto.isRtHalfDayStoreCover()){
                wdkCodes.add(brdPrefix + addressDto.getRtHalfDayStoreId());
            }
            locationInfo.setWdkCodes(wdkCodes);
        }
        request.getUserProfile().setUserId(MtopContext.getUserId());

        return request;
    }
}
