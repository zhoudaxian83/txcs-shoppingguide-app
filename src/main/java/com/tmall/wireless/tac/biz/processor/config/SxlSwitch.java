package com.tmall.wireless.tac.biz.processor.config;

import com.taobao.csp.switchcenter.annotation.AppSwitch;
import com.taobao.csp.switchcenter.annotation.NameSpace;
import com.taobao.csp.switchcenter.bean.Switch;
import com.taobao.csp.switchcenter.core.SwitchManager;

/**
 * @author haixiao.zhang
 * @date 2021/6/17
 */
@NameSpace(nameSpace = "sxl")
public class SxlSwitch {

    private static final String APP_NAME = "txcs-shoppingguide";

    @AppSwitch(des = "商品推荐选品集id", level = Switch.Level.p4)
    public static Long SXL_ITEMSET_ID = 322385L;

    @AppSwitch(des = "商品推荐tppId", level = Switch.Level.p4)
    public static Long SXL_TPP_APP_ID = 25385L;

    @AppSwitch(des = "招商主活动id", level = Switch.Level.p4)
    public static Long SXL_MAIN_ACTIVEX_ID = 870L;

    public static Object getValue(String key){

        return SwitchManager.getStrValue(APP_NAME,"sxl."+key);

    }


}
