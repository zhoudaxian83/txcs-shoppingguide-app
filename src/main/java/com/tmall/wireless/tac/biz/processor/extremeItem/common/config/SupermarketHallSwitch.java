package com.tmall.wireless.tac.biz.processor.extremeItem.common.config;

import com.taobao.csp.switchcenter.annotation.AppSwitch;
import com.taobao.csp.switchcenter.annotation.NameSpace;
import com.taobao.csp.switchcenter.bean.Switch;

@NameSpace(nameSpace = "supermarket-hall")
public class SupermarketHallSwitch {

    @AppSwitch(des = "极致单品打底数据计数周期", level = Switch.Level.p3)
    public static Integer bottomCounterCycle = 100;
}
