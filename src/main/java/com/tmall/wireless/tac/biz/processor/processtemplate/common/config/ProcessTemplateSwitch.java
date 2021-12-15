package com.tmall.wireless.tac.biz.processor.processtemplate.common.config;

import com.taobao.csp.switchcenter.annotation.AppSwitch;
import com.taobao.csp.switchcenter.annotation.NameSpace;
import com.taobao.csp.switchcenter.bean.Switch;

@NameSpace(nameSpace = "supermarket-processtemplate")
public class ProcessTemplateSwitch {

    @AppSwitch(des = "TPP打底数据计数周期", level = Switch.Level.p3)
    public static Integer tppBottomCounterCycle = 1000;

    @AppSwitch(des = "TPP采样打底逻辑是否开启", level = Switch.Level.p3)
    public static boolean openTPPSampleBottom = true;

    @AppSwitch(des = "是否mock tpp挂了【生产环境禁用】", level = Switch.Level.p3)
    public static boolean mockTppCrash = false;

    @AppSwitch(des = "是否mock captain挂了【生产环境禁用】", level = Switch.Level.p3)
    public static boolean mockCaptainCrash = false;
}