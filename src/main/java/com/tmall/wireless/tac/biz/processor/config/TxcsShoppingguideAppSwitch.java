package com.tmall.wireless.tac.biz.processor.config;

import com.taobao.csp.switchcenter.annotation.AppSwitch;
import com.taobao.csp.switchcenter.annotation.NameSpace;
import com.taobao.csp.switchcenter.bean.Switch;

/**
 * @author haixiao.zhang
 * @date 2021/6/17
 */
@NameSpace(nameSpace = "txcs-shoppingguide-app")
public class TxcsShoppingguideAppSwitch {


    @AppSwitch(des = "人工选品推荐选品集id", level = Switch.Level.p4)
    public static Boolean openSmartUiInIconCategory = true;


}
