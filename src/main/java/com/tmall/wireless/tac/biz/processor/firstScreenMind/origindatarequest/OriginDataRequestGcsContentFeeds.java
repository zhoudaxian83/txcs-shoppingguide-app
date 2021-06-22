package com.tmall.wireless.tac.biz.processor.firstScreenMind.origindatarequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.txcs.gs.model.biz.context.LocParams;
import com.tmall.txcs.gs.model.biz.context.PageInfoDO;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.txcs.gs.model.spi.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.TppItemBusinessTypeEnum;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.utils.ContentSetIdListUtil;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.utils.RenderLangUtil;

/**
 * @author guijian
 */
public class OriginDataRequestGcsContentFeeds implements OriginDataRequest{
    @Override
    public RecommendRequest buildRecommendRequest(SgFrameworkContext sgFrameworkContext) {
        RecommendRequest tppRequest = new RecommendRequest();
        Map<String, String> params = Maps.newHashMap();

        Long oneHour = Optional.of(sgFrameworkContext).map(SgFrameworkContext::getLocParams).map(LocParams::getRt1HourStoreId).orElse(0L);
        Long halfDay = Optional.of(sgFrameworkContext).map(SgFrameworkContext::getLocParams).map(LocParams::getRtHalfDayStoreId).orElse(0L);

        List<String> itemBusinessTypeList = Lists.newArrayList(TppItemBusinessTypeEnum.B2C.getType());
        if (oneHour > 0) {
            itemBusinessTypeList.add(TppItemBusinessTypeEnum.OneHour.getType());
            params.put("rt1HourStoreId", RenderLangUtil.safeString(oneHour));
        } else if (halfDay >= 0){
            itemBusinessTypeList.add(TppItemBusinessTypeEnum.HalfDay.getType());
            params.put("rtHalfDayStoreId", RenderLangUtil.safeString(halfDay));
        }
        params.put("itemBusinessType", Joiner.on(",").join(itemBusinessTypeList));

        Map<String, Object> requestParams = sgFrameworkContext.getRequestParams();
        if (requestParams == null || requestParams.isEmpty()) {
            return null;
        }

        List<Long> contentSetIdList = ContentSetIdListUtil.getGcsContentSetIdList(requestParams);

        params.put("contentSetIdList", Joiner.on(",").join(contentSetIdList));

        // 新版本的内容集id
        /*List<String> newContentSetIdList = contentSetIdList.stream().map(id -> "intelligentCombinationItems_" + id)
            .collect(Collectors.toList());
        params.put("sceneSet", Joiner.on(",").join(newContentSetIdList));*/

        params.put("pageSize", Optional.ofNullable(sgFrameworkContext).map(SgFrameworkContext::getUserPageInfo)
            .map(PageInfoDO::getPageSize).orElse(4).toString());
        //逛超市TPP内容召回每个内容挂载的商品数量
        params.put("itemCountPerContent", "10");
        params.put("contentType", "7");
        params.put("contentSetSource", "intelligentCombinationItems");
        //未登陆用户唯一身份ID，确认是否必须
        params.put("exposureDataUserId", Optional.ofNullable(sgFrameworkContext).map(
            SgFrameworkContext::getUserDO).map(UserDO::getCna).orElse(""));
        params.put("smAreaId", Optional.ofNullable(sgFrameworkContext).map(SgFrameworkContext::getLocParams).map(
            LocParams::getSmAreaId).orElse(0L).toString());
        params.put("logicAreaId", Joiner.on(",").join(Optional.ofNullable(sgFrameworkContext).map(
            SgFrameworkContext::getLocParams).map(LocParams::getLogicIdByPriority).orElse(
            Lists.newArrayList())));
        Integer index = Optional.ofNullable(sgFrameworkContext).map(SgFrameworkContext::getUserPageInfo).map(
            PageInfoDO::getIndex).orElse(0);
        params.put("isFirstPage", index > 0 ? "false" : "true");
        tppRequest.setAppId(23198L);
        tppRequest.setParams(params);
        tppRequest.setLogResult(true);
        tppRequest.setUserId(Optional.ofNullable(sgFrameworkContext).map(SgFrameworkContext::getUserDO)
            .map(UserDO::getUserId).orElse(0L));
        return tppRequest;
    }

}
