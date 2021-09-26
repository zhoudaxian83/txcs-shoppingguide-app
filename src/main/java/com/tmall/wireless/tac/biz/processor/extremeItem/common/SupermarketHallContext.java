package com.tmall.wireless.tac.biz.processor.extremeItem.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tmall.tcls.gs.sdk.biz.uti.MapUtil;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallCommonAldConstant.SM_AREAID;
import static com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallCommonAldConstant.STATIC_SCHEDULE_DATA;

@Data
public class SupermarketHallContext {
    private static Logger logger = LoggerFactory.getLogger(SupermarketHallContext.class);

    private Long userId;
    private String userNick;
    private String smAreaId;
    /**
     * 运营手工配置的数据
     */
    private List<Map<String, Object>> aldManualConfigDataList;

    /**
     * 流程模板上配置的参数
     */
    private JSONObject tacParamsMap;


    public static SupermarketHallContext init(RequestContext4Ald requestContext4Ald) {
        logger.info("SupermarketHallContext_requestContext4Ald" + JSON.toJSONString(requestContext4Ald));

        SupermarketHallContext supermarketHallContext = new SupermarketHallContext();

        //初始化用户信息
        if(requestContext4Ald.getUserInfo() != null) {
            supermarketHallContext.setUserId(requestContext4Ald.getUserInfo().getUserId());
            supermarketHallContext.setUserNick(requestContext4Ald.getUserInfo().getNick());
        }
        //初始化区域ID
        if(requestContext4Ald.getAldParam() != null) {
            String smAreaId = (String)requestContext4Ald.getAldParam().getOrDefault(SM_AREAID, "330100");
            supermarketHallContext.setSmAreaId(smAreaId);
        }

        //初始化tac参数
        String tacParams = MapUtil.getStringWithDefault(requestContext4Ald.getAldParam(), "tacParams", "");
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
