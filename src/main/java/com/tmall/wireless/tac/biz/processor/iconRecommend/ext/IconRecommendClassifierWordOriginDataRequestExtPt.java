package com.tmall.wireless.tac.biz.processor.iconRecommend.ext;

import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.tmall.txcs.gs.framework.extensions.origindata.request.ContentOriginDataRequestExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextContent;
import com.tmall.txcs.gs.framework.model.constant.ScenarioConstant;
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
 * @date 2021/8/9 3:00 下午
 */
@Extension(bizId = ScenarioConstant.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstant.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.SCENARIO_ICON_RECOMMEND_CLASSIFIER
)
@Service
public class IconRecommendClassifierWordOriginDataRequestExtPt implements ContentOriginDataRequestExtPt {

    @Autowired
    TacLogger logger;

    public static final Long appId = 23843L;

    @Override
    public RecommendRequest process(SgFrameworkContextContent sgFrameworkContextContent) {

        // https://tui.taobao.com/recommend?appid=23843&detailItemIdList=625556764230&pageSize=10
        // &contentSetIdList=1&contentSetSource=contentPlatform2000

        RecommendRequest recommendRequest = new RecommendRequest();

        recommendRequest.setAppId(appId);
        recommendRequest.setUserId(Optional.ofNullable(sgFrameworkContextContent)
                .map(SgFrameworkContext::getUserDO)
                .map(UserDO::getUserId)
                .orElse(0L));
        recommendRequest.setLogResult(true);

        Map<String, String> params = Maps.newHashMap();
        params.put("pageSize", "4");
        params.put("detailItemIdList", Optional.ofNullable(sgFrameworkContextContent)
                .map(SgFrameworkContext::getRequestParams)
                .map(map -> map.get("itemIdList"))
                .map(Object::toString)
                .orElse(""));
        logger.info("[Classifier Word]: ItemIds: " + params.get("detailItemIdList"));
        // 曝光过滤
        boolean firstPage = Optional.ofNullable(sgFrameworkContextContent)
                .map(SgFrameworkContext::getUserPageInfo)
                .map(PageInfoDO::getIndex).orElse(1) == 1;
        params.put("firstPage", String.valueOf(firstPage));
        params.put("itemCountPerContent", "20");
        params.put("contentSetIdList", "1");
        params.put("contentSetSource", "contentPlatform2000");
        recommendRequest.setParams(params);

        logger.info("[ClassifierWordOriginDataRequestExtPt] tppRequest: " + JSON.toJSONString(recommendRequest));
        return recommendRequest;
    }

    public static void main(String[] args) {
        RecommendRequest recommendRequest = new RecommendRequest();
        System.out.println("a" + Optional.of(recommendRequest).map(RecommendRequest::getAppId).orElse(777L));
    }
}
