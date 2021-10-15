package com.tmall.wireless.tac.biz.processor.extremeItem.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.taobao.eagleeye.EagleEye;
import com.tmall.tcls.gs.sdk.biz.uti.MapUtil;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.util.DateTimeUtil;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.util.Logger;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.util.LoggerProxy;
import com.tmall.wireless.tac.biz.processor.huichang.common.utils.PageUrlUtil;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

import static com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallCommonAldConstant.*;

@Data
public class SupermarketHallContext {
    private static Logger logger = LoggerProxy.getLogger(SupermarketHallContext.class);

    private Long userId;
    private String userNick;
    private String smAreaId;
    private String currentResourceId;
    private String currentScheduleId;
    private String currentPageUrl;

    private String previewTime;
    /**
     * 运营手工配置的数据
     */
    private List<Map<String, Object>> aldManualConfigDataList;

    /**
     * 流程模板上配置的参数
     */
    private JSONObject tacParamsMap;


    public static SupermarketHallContext init(RequestContext4Ald requestContext4Ald) {
        //logger.info("SupermarketHallContext_requestContext4Ald" + JSON.toJSONString(requestContext4Ald));

        SupermarketHallContext supermarketHallContext = new SupermarketHallContext();

        //初始化用户信息
        if(requestContext4Ald.getUserInfo() != null) {
            supermarketHallContext.setUserId(requestContext4Ald.getUserInfo().getUserId());
            supermarketHallContext.setUserNick(requestContext4Ald.getUserInfo().getNick());
        }
        if(requestContext4Ald.getAldParam() != null) {
            //初始化区域ID
            String smAreaId = (String)requestContext4Ald.getAldParam().getOrDefault(SM_AREAID, "330100");
            supermarketHallContext.setSmAreaId(smAreaId);

            //初始化预览时间
            String previewTimeStampStr = PageUrlUtil.getParamFromCurPageUrl(requestContext4Ald.getAldParam(), "previewTime");
            if(StringUtils.isNotBlank(previewTimeStampStr)) {
                try {
                    supermarketHallContext.setPreviewTime(DateTimeUtil.formatTimestamp(Long.parseLong(previewTimeStampStr)));
                } catch (Exception e) {
                    logger.error("SupermarketHallContext.init error, traceId:" + EagleEye.getTraceId() + ", previewTimeStampStr" + previewTimeStampStr, e);
                    //ignore
                    supermarketHallContext.setPreviewTime(null);
                }
            }

        }

        //初始化当前资源位ID
        supermarketHallContext.setCurrentResourceId(String.valueOf(requestContext4Ald.getAldContext().get(ALD_CURRENT_RES_ID)));
        //初始化当前排期ID
        supermarketHallContext.setCurrentScheduleId(String.valueOf(requestContext4Ald.getAldContext().get(ALD_SCHEDULE_ID)));

        //初始化当前页面URL
        String curPageUrl = MapUtil.getStringWithDefault(requestContext4Ald.getAldParam(), CUR_PAGE_URL, "");
        supermarketHallContext.setCurrentPageUrl(curPageUrl);

        //初始化tac参数
        String tacParams = MapUtil.getStringWithDefault(requestContext4Ald.getAldParam(), TAC_PARAMS, "");
        if(StringUtils.isNotBlank(tacParams)){
            JSONObject tacParamsMap = JSON.parseObject(tacParams);
            supermarketHallContext.setTacParamsMap(tacParamsMap);
        }

        //初始化运营手工配置的数据
        supermarketHallContext.setAldManualConfigDataList((List<Map<String, Object>>) requestContext4Ald.getAldContext().get(STATIC_SCHEDULE_DATA));

        logger.info("SupermarketHallContext_supermarketHallContext" + JSON.toJSONString(supermarketHallContext));
        return supermarketHallContext;
    }
}
