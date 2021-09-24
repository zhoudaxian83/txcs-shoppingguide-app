package com.tmall.wireless.tac.biz.processor.guessWhatYouLikeMenu.ext;

import com.ali.com.google.common.collect.Maps;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.*;

import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.model.constant.RpmContants;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.TppItemBusinessTypeEnum;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.utils.RenderLangUtil;
import com.tmall.wireless.tac.biz.processor.guessWhatYouLikeMenu.constant.ConstantValue;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.Enviroment;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Yushan
 * @date 2021/8/31 4:22 下午
*/
@SdkExtension(
        bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.CNXH_MENU_FEEDS
)
public class GulMenuContentOriginDataRequestBuildSdkExtPt extends Register implements ContentOriginDataRequestBuildSdkExtPt {
    @Resource
    TacLogger logger;

    @Override
    public RecommendRequest process(SgFrameworkContextContent sgFrameworkContextContent) {
        RecommendRequest tppRequest = new RecommendRequest();
        tppRequest.setLogResult(true);
        tppRequest.setAppId(ConstantValue.APPID);
        tppRequest.setUserId(Optional.ofNullable(sgFrameworkContextContent)
                .map(SgFrameworkContext::getCommonUserParams)
                .map(CommonUserParams::getUserDO)
                .map(UserDO::getUserId)
                .orElse(0L));

        Map<String, String> params = Maps.newHashMap();
        String regionCode = Joiner.on(",").join(Optional.ofNullable(sgFrameworkContextContent)
                .map(SgFrameworkContext::getCommonUserParams)
                .map(CommonUserParams::getLocParams)
                .map(LocParams::getLogicIdByPriority)
                .orElse(Lists.newArrayList()));
        if (StringUtils.isEmpty(regionCode)) {
            regionCode = "107";
        }
        params.put("regionCode", regionCode);
        params.put("smAreaId", Optional.ofNullable(sgFrameworkContextContent)
                .map(SgFrameworkContext::getCommonUserParams)
                .map(CommonUserParams::getLocParams)
                .map(LocParams::getSmAreaId)
                .orElse(0L)
                .toString());
        Long oneHourStoreId = Optional.ofNullable(sgFrameworkContextContent)
                .map(SgFrameworkContext::getCommonUserParams)
                .map(CommonUserParams::getLocParams)
                .map(LocParams::getRt1HourStoreId)
                .orElse(0L);
        Long halfDayStoreId = Optional.ofNullable(sgFrameworkContextContent)
                .map(SgFrameworkContext::getCommonUserParams)
                .map(CommonUserParams::getLocParams)
                .map(LocParams::getRtHalfDayStoreId)
                .orElse(0L);
        params.put("majorCityCode", Optional.ofNullable(sgFrameworkContextContent)
                .map(SgFrameworkContext::getCommonUserParams)
                .map(CommonUserParams::getLocParams)
                .map(LocParams::getMajorCityCode)
                .orElse(0L)
                .toString());
        params.put("logicAreaId", Joiner.on(",").join(Optional.ofNullable(sgFrameworkContextContent)
                .map(SgFrameworkContext::getCommonUserParams)
                .map(CommonUserParams::getLocParams)
                .map(LocParams::getLogicIdByPriority)
                .orElse(Lists.newArrayList())));
        params.put("contentType", "7");
        Map<String, Object> requestMap = Optional.ofNullable(sgFrameworkContextContent)
                .map(SgFrameworkContext::getTacContext)
                .map(Context::getParams)
                .orElse(Maps.newHashMap());
        String source = MapUtils.getString(requestMap, "source", "gul-recipe");
        List<String> itemBusinessTypeList = Lists.newArrayList();
        if (source.equals(ConstantValue.SOURCE_CHANNEL_GUL_RECIPE)) {
            if (oneHourStoreId > 0L) {
                itemBusinessTypeList.add(TppItemBusinessTypeEnum.OneHour.getType());
                params.put("rt1HourStoreId", RenderLangUtil.safeString(oneHourStoreId));
            } else if (halfDayStoreId > 0L) {
                itemBusinessTypeList.add(TppItemBusinessTypeEnum.HalfDay.getType());
                params.put("rtHalfDayStoreId", RenderLangUtil.safeString(halfDayStoreId));
            }
        } else if (source.equals(ConstantValue.SOURCE_CHANNEL_MMC_HALF_DAY)) {
            if (halfDayStoreId > 0L) {
                itemBusinessTypeList.add(TppItemBusinessTypeEnum.HalfDay.getType());
                params.put("rtHalfDayStoreId", RenderLangUtil.safeString(halfDayStoreId));
            }
        }
        params.put("itemBusinessType", Joiner.on(",").join(itemBusinessTypeList));
        params.put("pageSize", MapUtils.getString(requestMap, "pageSize", "8"));
        params.put("itemCountPerContent", MapUtils.getString(requestMap, "itemCountPerContent", "8"));
        Boolean isFirstPage = MapUtils.getBoolean(requestMap, "isFirstPage", true);
        params.put("isFirstPage", String.valueOf(isFirstPage));
        List<Long> contentSetIds = getLongWithDefault(requestMap, "recipeContentSetIds")
                .stream()
                .filter(contentSetId -> contentSetId > 0)
                .collect(Collectors.toList());
        params.put("contentSetIdList", Joiner.on(",").join(contentSetIds));
        params.put("contentSetSource", "intelligentCombinationItems");
        params.put("exposureDataUserId", Optional.ofNullable(sgFrameworkContextContent)
                .map(SgFrameworkContext::getCommonUserParams)
                .map(CommonUserParams::getUserDO)
                .map(UserDO::getCna)
                .orElse(""));
        List<Long> topContentIdList = getLongWithDefault(requestMap, "recipeContentTopIdList")
                .stream()
                .filter(topContentId -> topContentId > 0)
                .collect(Collectors.toList());
        // 首页才有置顶坑位
        if (isFirstPage) {
            params.put("topContentIds", Joiner.on(",").join(topContentIdList));
        }

        tppRequest.setParams(params);
        logger.info("GulMenuContentOriginDataRequestBuildSdkExtPt: tppRequest: " + JSON.toJSONString(tppRequest));
        String requestLog = "https://tui.taobao.com/recommend?appid=" + tppRequest.getAppId() + "&" +
                Joiner.on("&").withKeyValueSeparator("=").join(tppRequest.getParams());
        logger.info("TPP_REQUEST: " + requestLog);
        HadesLogUtil.stream(ScenarioConstantApp.CNXH_MENU_FEEDS)
                .kv("GulMenuContentOriginDataRequestBuildSdkExtPt","tppRequest")
                .kv("source", source)
                .kv("tppRequest", JSON.toJSONString(tppRequest))
                .info();
        return tppRequest;
    }

    public static List<Long> getLongWithDefault(Map<String, Object> map, String key) {

        String longListStr = MapUtil.getStringWithDefault(map, key, "");

        if (StringUtils.isEmpty(longListStr)) {
            return Lists.newArrayList();
        }
        List<String> longList = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(longListStr);

        return longList.stream().filter(StringUtils::isNumeric).map(Long::valueOf).collect(Collectors.toList());
    }
}
