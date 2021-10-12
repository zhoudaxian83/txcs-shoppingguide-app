package com.tmall.wireless.tac.biz.processor.config;

import com.tmall.wireless.tac.biz.processor.detail.common.config.DetailSwitch;
import com.tmall.wireless.tac.config.TacConfigManager;

/**
 * @author haixiao.zhang
 * @date 2021/6/17
 */
public class SwitchConfigBean {

    private static final String APP_NAME = "txcs-shoppingguide";

    public void init() {
        try {

            /*SwitchManager.init(APP_NAME, SxlSwitch.class);*/
            TacConfigManager.switchRegister(SxlSwitch.class);
            TacConfigManager.switchRegister(TxcsShoppingguideAppSwitch.class);
            TacConfigManager.switchRegister(DetailSwitch.class);

        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
