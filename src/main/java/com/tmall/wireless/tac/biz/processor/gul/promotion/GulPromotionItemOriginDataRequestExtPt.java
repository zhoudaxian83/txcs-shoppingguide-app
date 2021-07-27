package com.tmall.wireless.tac.biz.processor.gul.promotion;

import java.util.Map;
import java.util.Optional;
import com.alibaba.cola.extension.Extension;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.txcs.gs.framework.extensions.origindata.request.ItemOriginDataRequestExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.model.biz.context.LocParams;
import com.tmall.txcs.gs.model.biz.context.PageInfoDO;
import com.tmall.txcs.gs.model.biz.context.PmtParams;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.txcs.gs.model.spi.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author guijian
 */
@Extension(
    bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.GUL_PROMOTION
)
@Service
public class GulPromotionItemOriginDataRequestExtPt implements ItemOriginDataRequestExtPt {

    public static final Long APPID = 17433L;
    public static final String FRESH_LEVEL1_ID = "1217";
    public static final Long APPID_B2C = 23376L;
    public static final Long APPID_O2O = 23375L;

    @Override
    public RecommendRequest process(SgFrameworkContextItem sgFrameworkContextItem) {
        RecommendRequest tppRequest = new RecommendRequest();


        Map<String, String> params = Maps.newHashMap();
        params.put("pageSize", Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContext::getUserPageInfo).map(
            PageInfoDO::getPageSize).orElse(20).toString());
        params.put("smAreaId", Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContext::getLocParams).map(
            LocParams::getSmAreaId).orElse(0L).toString());
        params.put("logicAreaId", Joiner
            .on(",").join(Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContext::getLocParams).map(LocParams::getLogicIdByPriority).orElse(
                Lists.newArrayList())));
        /*params.put("userNick",Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContext::getUserDO).map(UserDO::getNick).orElse(""));
        Map<String, Object> requestParams = sgFrameworkContextItem.getRequestParams();
        String level1Id = MapUtil.getStringWithDefault(requestParams,"level1Id","1942");
        String level2Id = MapUtil.getStringWithDefault(requestParams,"level2Id","");

        params.put("level1Id", level1Id);
        params.put("level2Id", level2Id);
        params.put("type","promotionCainixihuan");*/
        //params.put("itemSetId", itemRecommendRequest.getItemSetId());


        Integer index = Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContext::getUserPageInfo).map(
            PageInfoDO::getIndex).orElse(0);
        /*params.put("index", String.valueOf(index));*/
        params.put("isFirstPage", index > 0 ? "false" : "true");
        tppRequest.setAppId(APPID);

        String moduleId = Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContextItem::getPmtParams).map(
            PmtParams::getModuleId).orElse("1217");
        String tagId = Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContextItem::getPmtParams).map(
            PmtParams::getTagId).orElse("");
        params.put("pmtSource", Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContextItem::getPmtParams).map(PmtParams::getPmtSource).orElse(""));
        params.put("pmtName", Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContextItem::getPmtParams).map(PmtParams::getPmtName).orElse(""));
        params.put("pageId", Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContextItem::getPmtParams).map(PmtParams::getPageId).orElse(""));
        params.put("moduleId", moduleId);
        /*params.put("tagId",tagId);*/
        if (StringUtils.equals(FRESH_LEVEL1_ID, moduleId)) {
            // 是生鲜tab
            tppRequest.setAppId(APPID_O2O);
            Long oneHour = Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContext::getLocParams).map(LocParams::getRt1HourStoreId).orElse(0L);
            Long halfDay = Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContext::getLocParams).map(LocParams::getRtHalfDayStoreId).orElse(0L);
            if (oneHour > 0) {
                params.put("itemBusinessType","OneHour");
                params.put("rt1HourStoreId", oneHour.toString());
            } else {
                params.put("itemBusinessType","HalfDay");
                params.put("rtHalfDayStoreId", halfDay.toString());
            }
        } else {
            // 是B2C的tab
            tppRequest.setAppId(APPID_B2C);
            params.put("itemBusinessType","B2C");
        }



        tppRequest.setParams(params);
        tppRequest.setLogResult(true);
        tppRequest.setUserId(Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContext::getUserDO).map(UserDO::getUserId).orElse(0L));

        return tppRequest;
    }
}
