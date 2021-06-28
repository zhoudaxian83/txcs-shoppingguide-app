package com.tmall.wireless.tac.biz.processor.firstScreenMind.origindatarequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.txcs.gs.model.biz.context.LocParams;
import com.tmall.txcs.gs.model.biz.context.PageInfoDO;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.txcs.gs.model.constant.RpmContants;
import com.tmall.txcs.gs.model.spi.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.TppItemBusinessTypeEnum;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.utils.ContentSetIdListUtil;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.utils.RenderLangUtil;
import com.tmall.wireless.tac.client.domain.Enviroment;

/**
 * @author guijian
 */
public class OriginDataRequestContentFeedsMind implements OriginDataRequest {

    @Override
    public RecommendRequest buildRecommendRequest(SgFrameworkContext sgFrameworkContext) {
        RecommendRequest tppRequest = new RecommendRequest();
        Map<String, String> params = Maps.newHashMap();
        tppRequest.setParams(params);
        tppRequest.setLogResult(true);
        tppRequest.setUserId(Optional.of(sgFrameworkContext).map(SgFrameworkContext::getUserDO)
            .map(UserDO::getUserId).orElse(0L));
        tppRequest.setAppId(25379L);
        Map<String, Object> requestParams = sgFrameworkContext.getRequestParams();
        if (requestParams == null || requestParams.isEmpty()) {
            return null;
        }
        List<Long> contentSetIdList = ContentSetIdListUtil.getMindContentSetIdList(requestParams);
        List<String> newContentSetIdList = contentSetIdList.stream().map(id -> "intelligentCombinationItems_" + id)
            .collect(
                Collectors.toList());
        params.put("sceneSet", Joiner.on(",").join(newContentSetIdList));
        /**心智场景支持O2O场景**/
        Long oneHour = Optional.of(sgFrameworkContext).map(SgFrameworkContext::getLocParams).map(LocParams::getRt1HourStoreId).orElse(0L);
        Long halfDay = Optional.of(sgFrameworkContext).map(SgFrameworkContext::getLocParams).map(LocParams::getRtHalfDayStoreId).orElse(0L);
        List<String> itemBusinessTypeList = Lists.newArrayList(TppItemBusinessTypeEnum.B2C.getType());
        if (oneHour > 0) {
            itemBusinessTypeList.add(TppItemBusinessTypeEnum.O2O.getType());
            params.put("rtOneHourStoreId", RenderLangUtil.safeString(oneHour));
        } else if (halfDay > 0){
            itemBusinessTypeList.add(TppItemBusinessTypeEnum.O2O.getType());
            params.put("rtHalfDayStoreId", RenderLangUtil.safeString(halfDay));
        }
        params.put("commerce", Joiner.on(",").join(itemBusinessTypeList));


        params.put("regionCode", Joiner.on(",").join(Optional.ofNullable(sgFrameworkContext).map(
            SgFrameworkContext::getLocParams).map(LocParams::getLogicIdByPriority).orElse(Lists.newArrayList())));
        if (params.get("regionCode") == null || "".equals(params.get("regionCode"))) {
            params.put("regionCode", "107");
        }
        params.put("smAreaId", Optional.ofNullable(sgFrameworkContext).map(SgFrameworkContext::getLocParams)
            .map(
                LocParams::getSmAreaId).orElse(0L).toString());
        params.put("exposureDataUserId", Optional.ofNullable(sgFrameworkContext).map(
            SgFrameworkContext::getUserDO).map(UserDO::getCna).orElse(""));
        params.put("pageSize", Optional.ofNullable(sgFrameworkContext).map(
            SgFrameworkContext::getUserPageInfo).map(
            PageInfoDO::getPageSize).orElse(20).toString());
        Integer index = Optional.ofNullable(sgFrameworkContext).map(SgFrameworkContext::getUserPageInfo).map(
            PageInfoDO::getIndex).orElse(0);
        params.put("index", String.valueOf(index));
        params.put("isFirstPage", index > 0 ? "false" : "true");
        if (Enviroment.PRE.equals(RpmContants.enviroment)) {
            params.put("_devEnv_", "1");
        }
        return tppRequest;
    }
}

