package com.tmall.wireless.tac.biz.processor.firstScreenMind;

import java.util.Map;

import com.alibaba.cola.extension.BizScenario;
import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;

import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.extensions.origindata.request.ContentOriginDataRequestExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextContent;
import com.tmall.txcs.gs.model.spi.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.common.FirstScreenConstant;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.origindatarequest.OriginDataRequestFactory;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_CONTENT)
@Service
@Slf4j
public class FirstScreenMindContentOriginDataRequestExPt implements ContentOriginDataRequestExtPt {
    Logger LOGGER = LoggerFactory.getLogger(FirstScreenMindContentOriginDataRequestExPt.class);

    @Autowired
    TacLogger tacLogger;
    @Autowired
    OriginDataRequestFactory originDataRequestFactory;

    @Override
    public RecommendRequest process(SgFrameworkContextContent sgFrameworkContextContent) {
        BizScenario bizScenario = sgFrameworkContextContent.getBizScenario();
        Map<String,Object> requestParams = sgFrameworkContextContent.getRequestParams();
        RecommendRequest tppRequest = null;
        /**前端没有传递，默认是首页内容**/
        String requestFrom = MapUtil.getStringWithDefault(requestParams,FirstScreenConstant.REQUEST_FROM,
            FirstScreenConstant.CONTENT_FEEDS);

        if(FirstScreenConstant.CONTENT_FEEDS.equals(requestFrom)){
            tppRequest = originDataRequestFactory.getRecommendRequest(FirstScreenConstant.FIR_CONTENT_FEEDS,sgFrameworkContextContent);
        }else if (FirstScreenConstant.ITEM_FEEDS.equals(requestFrom)){
            tppRequest = originDataRequestFactory.getRecommendRequest(FirstScreenConstant.SUB_CONTENT_FEEDS,sgFrameworkContextContent);
        }else if(FirstScreenConstant.GCS_CONTENT_FEEDS.equals(requestFrom)){
            tppRequest = originDataRequestFactory.getRecommendRequest(FirstScreenConstant.GCS_FIR_CONTENT_FEEDS,sgFrameworkContextContent);
        }else if(FirstScreenConstant.GCS_ITEM_FEEDS.equals(requestFrom)){
            tppRequest = originDataRequestFactory.getRecommendRequest(FirstScreenConstant.GCS_SUB_CONTENT_FEEDS,sgFrameworkContextContent);
        }
        log.error("FirstScreenMindContentOriginDataRequestExPt.biz:{},tppRequest:{}",bizScenario.getUniqueIdentity(), JSON.toJSONString(tppRequest));
        return tppRequest;
    }

}
