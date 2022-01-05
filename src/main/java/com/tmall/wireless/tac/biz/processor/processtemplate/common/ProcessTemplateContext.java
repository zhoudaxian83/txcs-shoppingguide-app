package com.tmall.wireless.tac.biz.processor.processtemplate.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.taobao.eagleeye.EagleEye;
import com.tmall.aself.shoppingguide.client.loc.domain.AddressDTO;
import com.tmall.aself.shoppingguide.client.loc.util.AddressUtil;
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
public class ProcessTemplateContext {
    private static Logger logger = LoggerProxy.getLogger(ProcessTemplateContext.class);

    private Long userId;
    private String userNick;
    private String smAreaId;
    private String logicAreaId;
    private String currentResourceId;
    private String currentScheduleId;
    private String currentPageUrl;

    private String previewTime;
    private String sceneCode;//场景码
    /**
     * 运营手工配置的数据
     */
    private List<Map<String, Object>> aldManualConfigDataList;

    /**
     * 流程模板上配置的参数
     */
    private JSONObject tacParamsMap;

    /**
     * 来源类的简单类名，主要用于记日志时进行区分
     */
    private String sourceClassSimpleName;


    public static ProcessTemplateContext init(RequestContext4Ald requestContext4Ald, Class clazz) {

        ProcessTemplateContext context = new ProcessTemplateContext();
        context.setSourceClassSimpleName(clazz.getSimpleName());

        //初始化用户信息
        if(requestContext4Ald.getUserInfo() != null) {
            context.setUserId(requestContext4Ald.getUserInfo().getUserId());
            context.setUserNick(requestContext4Ald.getUserInfo().getNick());
        }
        if(requestContext4Ald.getAldParam() != null) {
            //初始化区域ID
            String smAreaId = (String)requestContext4Ald.getAldParam().getOrDefault(SM_AREAID, "");
            context.setSmAreaId(smAreaId);

            String csa = (String)requestContext4Ald.getAldParam().getOrDefault(CSA, "");
            if(StringUtils.isNotBlank(csa)) {
                AddressDTO addressDTO = AddressUtil.parseCSA(csa);
                if(addressDTO != null && addressDTO.getRegionCode() != null && !"0".equals(addressDTO.getRegionCode())) {
                    context.setLogicAreaId(addressDTO.getRegionCode());
                }
            }

            //初始化预览时间
            String previewTimeStampStr = PageUrlUtil.getParamFromCurPageUrl(requestContext4Ald.getAldParam(), "previewTime");
            if(StringUtils.isNotBlank(previewTimeStampStr)) {
                try {
                    context.setPreviewTime(DateTimeUtil.formatTimestamp(Long.parseLong(previewTimeStampStr)));
                } catch (Exception e) {
                    logger.error("ProcessTemplateContext.init error, traceId:" + EagleEye.getTraceId() + ", previewTimeStampStr" + previewTimeStampStr, e);
                    //ignore
                    context.setPreviewTime(null);
                }
            }

        }

        //初始化当前资源位ID
        context.setCurrentResourceId(String.valueOf(requestContext4Ald.getAldContext().get(ALD_CURRENT_RES_ID)));
        //初始化当前排期ID
        context.setCurrentScheduleId(String.valueOf(requestContext4Ald.getAldContext().get(ALD_SCHEDULE_ID)));

        //初始化当前页面URL
        String curPageUrl = MapUtil.getStringWithDefault(requestContext4Ald.getAldParam(), CUR_PAGE_URL, "");
        context.setCurrentPageUrl(curPageUrl);

        //初始化tac参数
        String tacParams = MapUtil.getStringWithDefault(requestContext4Ald.getAldParam(), TAC_PARAMS, "");
        if(StringUtils.isNotBlank(tacParams)){
            JSONObject tacParamsMap = JSON.parseObject(tacParams);
            context.setTacParamsMap(tacParamsMap);
        }

        //初始化运营手工配置的数据
        context.setAldManualConfigDataList((List<Map<String, Object>>) requestContext4Ald.getAldContext().get(STATIC_SCHEDULE_DATA));

        logger.info(context.getSourceClassSimpleName() + "_ProcessTemplateContext:" + JSON.toJSONString(context));
        return context;
    }
}
