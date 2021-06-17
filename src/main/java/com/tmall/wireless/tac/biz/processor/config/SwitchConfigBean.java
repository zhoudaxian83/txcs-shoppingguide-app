package com.tmall.wireless.tac.biz.processor.config;

import com.taobao.csp.switchcenter.core.SwitchManager;


/**
 * @author haixiao.zhang
 * @date 2021/6/17
 */
public class SwitchConfigBean {

    private static final String APP_NAME = "txcs-shoppingguide";

    public void init() {
        try {

            SwitchManager.init(APP_NAME, SxlSwitch.class);

        } catch (Throwable e) {
        }
    }
}
