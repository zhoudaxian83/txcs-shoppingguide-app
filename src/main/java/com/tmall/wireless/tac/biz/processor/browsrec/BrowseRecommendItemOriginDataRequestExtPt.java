package com.tmall.wireless.tac.biz.processor.browsrec;

import com.alibaba.cola.extension.Extension;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.extensions.origindata.request.ItemOriginDataRequestExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.framework.model.constant.ScenarioConstant;
import com.tmall.txcs.gs.model.biz.context.LocParams;
import com.tmall.txcs.gs.model.biz.context.PageInfoDO;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.txcs.gs.model.spi.model.RecommendRequest;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * Created by yangqing.byq on 2021/2/18.
 */
@Extension(bizId = ScenarioConstant.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstant.LOC_TYPE_B2C,
        scenario = ScenarioConstant.SCENARIO_GUL_BROWSE_RECOMMEND)
@Service
public class BrowseRecommendItemOriginDataRequestExtPt implements ItemOriginDataRequestExtPt {

    public static final String FRESH_LEVEL1_ID = "1217";
    public static final Long APPID_B2C = 23376L;
    public static final Long APPID_O2O = 23375L;


//    https://tuipre.taobao.com/recommend?
//    appid=24696&
//    detailItemIdList=638358005687&
//    pageSize=200&
//    index=0&
//    smAreaId=330110&
//    logicAreaId=107

    @Override
    public RecommendRequest process(SgFrameworkContextItem sgFrameworkContextItem) {
        RecommendRequest tppRequest = new RecommendRequest();


        Map<String, String> params = Maps.newHashMap();
        params.put("pageSize", "20");
        params.put("smAreaId", Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContext::getLocParams).map(LocParams::getSmAreaId).orElse(0L).toString());
        params.put("logicAreaId", Joiner.on(",").join(Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContext::getLocParams).map(LocParams::getLogicIdByPriority).orElse(Lists.newArrayList())));
        params.put("itemBusinessType","B2C");
        params.put("detailItemIdList", MapUtil.getStringWithDefault(
                Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContext::getRequestParams).orElse(Maps.newHashMap()),
                "detailItemIdList",
                ""
        ));


        Integer index = Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContext::getUserPageInfo).map(PageInfoDO::getIndex).orElse(0);
        params.put("isFirstPage", index > 0 ? "true" : "false");

        tppRequest.setAppId(24696L);


        tppRequest.setParams(params);
        tppRequest.setLogResult(true);
        tppRequest.setUserId(Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContext::getUserDO).map(UserDO::getUserId).orElse(0L));

        return tppRequest;
    }


}
