package com.tmall.wireless.tac.biz.processor.detail.common.config;

import java.util.HashMap;
import java.util.Map;

import com.taobao.csp.switchcenter.annotation.AppSwitch;
import com.taobao.csp.switchcenter.annotation.NameSpace;
import com.taobao.csp.switchcenter.bean.Switch;

/**
 * @author: guichen
 * @Data: 2021/9/10
 * @Description:
 */
@NameSpace(nameSpace = "supermarket.detail")
public class DetailSwitch {

    @AppSwitch(des = "人工选品推荐选品集id", level = Switch.Level.p3)
    public static Map<String,Long> appIdMap = new HashMap<>();

}
