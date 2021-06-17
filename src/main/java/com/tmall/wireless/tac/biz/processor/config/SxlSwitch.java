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

    @AppSwitch(des = "召回商品数", level = Switch.Level.p4)
    public static Integer ITEM_PAGE_SIZE = 60;

    public static Object getValue(String key){

        return SwitchManager.getValue("",key);
    }

}
