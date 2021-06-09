package com.tmall.wireless.tac.biz.processor.firstScreenMind.origindatarequest;

import java.util.Map;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.txcs.gs.model.spi.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.common.RequestKeyConstantApp;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.common.FirstScreenConstant;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author guijian
 */
public class OriginDataRequestFactory {
    Logger LOGGER = LoggerFactory.getLogger(OriginDataRequestFactory.class);

    @Autowired
    TacLogger tacLogger;

    public RecommendRequest getRecommendRequest(String requestFrom, SgFrameworkContext sgFrameworkContext){
        OriginDataRequest originDataRequest = null;
        switch (requestFrom){
            case FirstScreenConstant.CONTENT_CONTENT_FEEDS:
                if(isMind(sgFrameworkContext)){
                    originDataRequest = new OriginDataRequestContentFeedsMind();
                }else{
                    originDataRequest = new OriginDataRequestContentFeeds();
                }
                break;
            case FirstScreenConstant.ITEM_CONTENT_FEEDS:
                originDataRequest = new OriginDataRequestContentFeeds();
                break;
            case FirstScreenConstant.ITEM_ITEM_FEEDS:
                originDataRequest = new OriginDataRequestItemFeeds();
                break;
            case FirstScreenConstant.GCS_CONTENT_CONTENT_FEEDS:
                originDataRequest = new OriginDataRequestGcsContentFeeds();
                break;
            case FirstScreenConstant.GCS_ITEM_CONTENT_FEEDS:
                originDataRequest = new OriginDataRequestGcsContentFeeds();
                break;
            case FirstScreenConstant.GCS_ITEM_ITEM_FEEDS:
                originDataRequest = new OriginDataRequestGcsItemFeeds();
                break;
            default:
                originDataRequest = new OriginDataRequestGcsContentFeeds();
                break;

        }
        return originDataRequest.buildRecommendRequest(sgFrameworkContext);
    }
    private boolean isMind(SgFrameworkContext sgFrameworkContext) {
        Boolean isMind = false;
        if(sgFrameworkContext == null){
            return isMind;
        }
        Map<String,Object> requestParams = sgFrameworkContext.getRequestParams();

        Long mindContentCode = MapUtil.getLongWithDefault(requestParams,
            RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_MIND, 0L);
        if (mindContentCode <= 0) {
            return isMind;
        }
        Object isFixPositionBanner = requestParams.get("isFixPositionBanner");

        if (isFixPositionBanner == null || "".equals(isFixPositionBanner)) {
            isMind = true;
        } else if (isFixPositionBanner instanceof Boolean) {
            isMind = (Boolean)isFixPositionBanner;
        } else if (isFixPositionBanner instanceof String && "true".equals(isFixPositionBanner)) {
            isMind = true;
        }
        if(isItemFeeds(requestParams)){
            isMind = false;
        }
        return isMind;
    }
    private boolean isItemFeeds(Map<String, Object> requestParams){
        Boolean isItemFeeds = false;
        String requestFrom = MapUtil.getStringWithDefault(requestParams,"requestFrom","");
        if("itemFeeds".equals(requestFrom)){
            isItemFeeds = true;
        }
        return isItemFeeds;
    }
}
