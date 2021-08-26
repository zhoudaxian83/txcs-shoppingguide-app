package com.tmall.wireless.tac.biz.processor.iconRecommend.ext;

import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.txcs.gs.framework.extensions.origindata.request.ContentOriginDataRequestExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextContent;
import com.tmall.txcs.gs.framework.model.constant.ScenarioConstant;
import com.tmall.txcs.gs.model.biz.context.LocParams;
import com.tmall.txcs.gs.model.biz.context.PageInfoDO;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.txcs.gs.model.spi.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * @author Yushan
 * @date 2021/8/6 4:53 下午
 */
@Extension(bizId = ScenarioConstant.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstant.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.SCENARIO_ICON_RECOMMEND_SCENE
)
@Service
public class IconRecommendSceneContentOriginDataRequestExtPt implements ContentOriginDataRequestExtPt {

    @Autowired
    TacLogger logger;

    public static final Long appId = 26765L;

    @Override
    public RecommendRequest process(SgFrameworkContextContent sgFrameworkContextContent) {

        // https://tuipre.taobao.com/recommend?_mtop_rl_url_=true&rtHalfDayStoreId=236635411&commerce=B2C,O2O
        // &smAreaId=330110&_devEnv_=1&regionCode=107&appid=26765&index=0&pageSize=8&sceneSet=intelligentCombinationItems
        // &itemIds=535780624508,624851528087&exposureSwitch=false&maxItemReturn=21&manufacturerId=device_id_12345
        RecommendRequest recommendRequest = new RecommendRequest();
        recommendRequest.setAppId(appId);
        recommendRequest.setUserId(Optional.ofNullable(sgFrameworkContextContent)
                .map(SgFrameworkContext::getUserDO)
                .map(UserDO::getUserId)
                .orElse(0L));
        recommendRequest.setLogResult(true);

        Map<String, String> params = Maps.newHashMap();
        // 固定前缀
        params.put("_mtop_rl_url_", "true");
//        params.put("rtHalfDayStoreId", "236635411");
        params.put("commerce", "B2C");
        params.put("smAreaId", Optional.ofNullable(sgFrameworkContextContent).map(SgFrameworkContext::getLocParams).map(LocParams::getSmAreaId).orElse(330110L).toString());
        params.put("_devEnv_", "1");
        params.put("regionCode", Joiner.on(",").join(Optional.ofNullable(sgFrameworkContextContent).map(
                SgFrameworkContext::getLocParams).map(LocParams::getLogicIdByPriority).orElse(Lists.newArrayList())));
        if (params.get("regionCode") == null || "".equals(params.get("regionCode"))) {
            params.put("regionCode", "107");
        }
        // 其余参数
        // 曝光过滤数据
        Integer index = Optional.ofNullable(sgFrameworkContextContent)
                .map(SgFrameworkContext::getUserPageInfo)
                .map(PageInfoDO::getIndex)
                .orElse(1);
        if (index == 1) {
            params.put("index", "0");
        } else {
            params.put("index", "1");
        }
//        params.put("index", String.valueOf(Optional.ofNullable(sgFrameworkContextContent)
//                .map(SgFrameworkContext::getUserPageInfo)
//                .map(PageInfoDO::getIndex)
//                .orElse(1)));
        params.put("pageSize", "1");
        params.put("sceneSet", "intelligentCombinationItems");
//        params.put("itemIds", Joiner.on(",").join((List<Long>) Optional.ofNullable(sgFrameworkContextContent)
//                .map(SgFrameworkContext::getRequestParams)
//                .map(map -> map.get("itemIdList"))
//                .orElse(Lists.newArrayList())));
        params.put("itemIds", Optional.ofNullable(sgFrameworkContextContent)
                .map(SgFrameworkContext::getRequestParams)
                .map(map -> map.get("itemIdList"))
                .map(Object::toString)
                .orElse(""));
        logger.info("[Scene Word]: ItemIds: " + params.get("itemIds"));
        // 曝光过滤开关
        params.put("exposureSwitch", "true");
        params.put("maxItemReturn", "21");
        // 设备id
        params.put("manufacturerId", Optional.ofNullable(sgFrameworkContextContent)
                .map(SgFrameworkContext::getUserDO)
                .map(UserDO::getUtdid)
                .orElse(""));
        recommendRequest.setParams(params);

        logger.info("[SceneContentOriginDataRequestExtPt] tppRequest: " + JSON.toJSONString(recommendRequest));
        return recommendRequest;
    }
}
