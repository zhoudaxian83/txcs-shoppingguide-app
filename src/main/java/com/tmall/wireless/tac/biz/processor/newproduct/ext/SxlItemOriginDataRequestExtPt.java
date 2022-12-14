package com.tmall.wireless.tac.biz.processor.newproduct.ext;

import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.txcs.gs.framework.extensions.origindata.request.ItemOriginDataRequestExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.framework.model.constant.ScenarioConstant;
import com.tmall.txcs.gs.model.biz.context.LocParams;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.txcs.gs.model.spi.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.newproduct.constant.Constant;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * tpp入参组装扩展点
 * @author haixiao.zhang
 * @date 2021/6/7
 */
@Extension(bizId = ScenarioConstant.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstant.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM)
@Service
public class SxlItemOriginDataRequestExtPt implements ItemOriginDataRequestExtPt {

    private static final Long APPID = 25385L;

    @Autowired
    TacLogger tacLogger;

    @Override
    public RecommendRequest process(SgFrameworkContextItem sgFrameworkContextItem) {

        /**
         * https://tui.taobao.com/recommend?appid=24910&itemSets=crm_219953,crm_219840&pageSize=20&index=0
         */

        RecommendRequest tppRequest = new RecommendRequest();
        tppRequest.setAppId(APPID);

        Map<String, String> params = Maps.newHashMap();
        params.put("pageSize", String.valueOf(sgFrameworkContextItem.getUserPageInfo().getPageSize()));
        params.put("itemSets",  buildItemSetIds(sgFrameworkContextItem));
        params.put("smAreaId", Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContext::getLocParams).map(LocParams::getSmAreaId).orElse(0L).toString());
        params.put("index", String.valueOf(sgFrameworkContextItem.getUserPageInfo().getIndex()));
        tppRequest.setUserId(Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContext::getUserDO)
            .map(UserDO::getUserId).orElse(0L));
        params.put("commerce", "B2C");

        Long regionCode = sgFrameworkContextItem.getLocParams().getRegionCode();
        params.put("regionCode", (regionCode == null || regionCode == 0) ? "107" : String.valueOf(regionCode));
        params.put("exposureDataUserId",Optional.ofNullable(sgFrameworkContextItem).map(
            SgFrameworkContext::getUserDO).map(UserDO::getCna).orElse(""));

        Object xRecommendContentClose = sgFrameworkContextItem.getRequestParams().get("xRecommendContentClose");
        if(xRecommendContentClose != null){
            params.put("x-recommend-content-close", String.valueOf(xRecommendContentClose));
        }

        tppRequest.setParams(params);
        tppRequest.setLogResult(true);
        HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM)
            .kv("tppRequest",JSON.toJSONString(tppRequest))
            .info();

        return tppRequest;
    }


    private String buildItemSetIds(SgFrameworkContextItem sgFrameworkContextItem){

        List<Long> itemSetIdList = sgFrameworkContextItem.getEntitySetParams().getItemSetIdList();
        if(CollectionUtils.isNotEmpty(itemSetIdList)){
            List<String> list = itemSetIdList.stream().map(e->{
                return sgFrameworkContextItem.getEntitySetParams().getItemSetSource()+"_"+e;
            }).collect(Collectors.toList());
            return String.join(",",list);
        }else {
            return "crm_" + Constant.SXL_ITEMSET_ID + "," + "crm_" + Constant.SXL_ALGO_ITEMSET_ID;
        }

    }
}
