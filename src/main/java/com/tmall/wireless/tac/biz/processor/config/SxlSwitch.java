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


    @AppSwitch(des = "测试", level = Switch.Level.p4)
    public static Integer ITEM_PAGE_SIZE = 60;

    @AppSwitch(des = "商品推荐选品集id", level = Switch.Level.p4)
    public static String SXL_ITEMSET_ID = "crm_322385";


    @AppSwitch(des = "测试新", level = Switch.Level.p4)
    public static String ceshi = "111";

    public static Object getValue(){

        return SwitchManager.getStrValue("txcs-shoppingguide","sxl.ITEM_PAGE_SIZE");

    }


}
