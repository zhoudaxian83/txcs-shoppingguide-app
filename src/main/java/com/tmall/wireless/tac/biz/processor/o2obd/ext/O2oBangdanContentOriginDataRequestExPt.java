package com.tmall.wireless.tac.biz.processor.o2obd.ext;

import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.extensions.origindata.request.ContentOriginDataRequestExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextContent;
import com.tmall.txcs.gs.model.biz.context.LocParams;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.txcs.gs.model.spi.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * tpp参数组装
 * @author haixiao.zhang
 * @date 2021/6/22
 */
@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_O2O,
    scenario = ScenarioConstantApp.O2O_BANG_DAN)
@Service
public class O2oBangdanContentOriginDataRequestExPt implements ContentOriginDataRequestExtPt {

    private static final Long APPID = 25399L;


    @Autowired
    TacLogger tacLogger;

    @Override
    public RecommendRequest process(SgFrameworkContextContent sgFrameworkContextContent) {

        /**
         * https://tui.taobao.com/recommend?appid=23198&majorCityCode=0&logicAreaId=112&pageSize=10&rt1HourStoreId=233930382&contentSetSource=intelligentCombinationItems&itemCountPerContent=10&userid=1832025789&smAreaId=640105&itemBusinessType=OneHour&regionCode=112&topContentCount=1&isFirstPage=true&contentType=7&contentSetIdList=118003
         */

         /** https://tuipre.taobao.com/recommend?appid=23198&itemBusinessType=B2C&&logicAreaId=108&contentSetIdList=6006&contentSetSource=intelligentCombinationItems&itemCountPerContent=5&userid=0&smAreaId=370214&contentType=7
         */

        /**
         * {"majorCityCode":"107","logicAreaId":"112","contentSetIdList":"167004","pageSize":"10","rt1HourStoreId":"233930382","contentSetSource":"intelligentCombinationItems","itemCountPerContent":"5","userid":"1832025789","smAreaId":"330100","itemBusinessType":"OneHour","topContentCount":"1","isFirstPage":"true","contentType":"7"},"userId":1832025789
         */

        /**
         *  &majorCityCode=107&logicAreaId=112&pageSize=10&rt1HourStoreId=233930124&contentSetSource=intelligentCombinationItems&itemCountPerContent=10&userid=1832025789&smAreaId=640105&itemBusinessType=OneHour&regionCode=112&topContentCount=1&isFirstPage=true&contentType=7&contentSetIdList=167004
         */


        RecommendRequest tppRequest = new RecommendRequest();
        tppRequest.setAppId(APPID);
        tppRequest.setUserId(Optional.ofNullable(sgFrameworkContextContent).map(SgFrameworkContext::getUserDO)
            .map(UserDO::getUserId).orElse(0L));
        Map<String, String> params = Maps.newHashMap();
        params.put("contentSetIdList",  (String)sgFrameworkContextContent.getUserParams().get("contentSetIdList"));
        params.put("contentSetSource", "intelligentCombinationItems");
        params.put("itemCountPerContent", "5");
        long oneHourStoreId = sgFrameworkContextContent.getLocParams().getRt1HourStoreId();
        long halfDayStoreId = sgFrameworkContextContent.getLocParams().getRtHalfDayStoreId();
        if(oneHourStoreId!=0L){
            params.put("rt1HourStoreId", String.valueOf(oneHourStoreId));
            params.put("itemBusinessType", "OneHour");
        }else if(halfDayStoreId!=0L){
            params.put("rtHalfDayStoreId", String.valueOf(halfDayStoreId));
            params.put("itemBusinessType", "HalfDay");
        }
        params.put("contentType", "7");
        params.put("pageSize","10");
        params.put("majorCityCode", String.valueOf(sgFrameworkContextContent.getLocParams().getCityCode()));
        params.put("logicAreaId", String.valueOf(sgFrameworkContextContent.getLocParams().getRegionCode()));
        params.put("isFirstPage", "true");
        params.put("topContentCount", "1");
        params.put("smAreaId", Optional
            .ofNullable(sgFrameworkContextContent).map(SgFrameworkContext::getLocParams).map(LocParams::getSmAreaId).orElse(0L).toString());
        tppRequest.setParams(params);

        HadesLogUtil.stream(ScenarioConstantApp.O2O_BANG_DAN)
            .kv("step", "tppRequest")
            .kv("tppRequest",JSON.toJSONString(tppRequest))
            .error();
        tacLogger.info("O2oBangdanContentOriginDataRequestExPt tppRequest:"+JSON.toJSONString(tppRequest));
        return tppRequest;
    }
}
