package com.tmall.wireless.tac.biz.processor.ext;

import com.alibaba.cola.extension.Extension;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.txcs.gs.framework.extensions.origindata.request.ItemOriginDataRequestExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.framework.model.constant.ScenarioConstant;
import com.tmall.txcs.gs.model.biz.context.EntitySetParams;
import com.tmall.txcs.gs.model.biz.context.LocParams;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.txcs.gs.service.model.TppRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by yangqing.byq on 2021/2/18.
 */
@Extension(bizId = ScenarioConstant.ENTITY_TYPE_ITEM,
        useCase = ScenarioConstant.BIZ_TYPE_B2C,
        scenario = "gul")
public class GulItemOriginDataRequestExtPt implements ItemOriginDataRequestExtPt {
    @Override
    public TppRequest processOriginDataRequest(SgFrameworkContextItem sgFrameworkContextItem) {
        TppRequest tppRequest = new TppRequest();

//        https://tui.taobao.com/recommend?closeSls=0
//        itemSetIdList=212229
//        pageSize=100
//        rt1HourStoreId=233930157
//        itemSetIdSource=crm
//        smAreaId=330402
//        userid=2856722021
//        itemBusinessType=OneHour
//        isFirstPage=true

        Map<String, String> params = Maps.newHashMap();
        List<Long> itemSetIds = Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContext::getEntitySetParams).map(EntitySetParams::getItemSetIdList).orElse(Lists.newArrayList());
        params.put("itemSetIdList", Joiner.on(",").join(itemSetIds));
        params.put("pageSize", "10");
        params.put("rt1HourStoreId", Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContext::getLocParams).map(LocParams::getRt1HourStoreId).orElse(0L).toString());
        params.put("rtHalfDayStoreId", Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContext::getLocParams).map(LocParams::getRtNextDayStoreId).orElse(0L).toString());
        params.put("itemSetIdSource", Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContext::getEntitySetParams).map(EntitySetParams::getItemSetSource).orElse(""));
        params.put("smAreaId", Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContext::getLocParams).map(LocParams::getSmAreaId).orElse(0L).toString());
        params.put("isFirstPage", "true");
        params.put("itemBusinessType","NextDay,HalfDay,OneHour,B2C");

        tppRequest.setParams(params);
        tppRequest.setLogResult(true);
        tppRequest.setAppId(18611L);
        tppRequest.setUserId(Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContext::getUserDO).map(UserDO::getUserId).orElse(0L));

        return tppRequest;
    }

}
