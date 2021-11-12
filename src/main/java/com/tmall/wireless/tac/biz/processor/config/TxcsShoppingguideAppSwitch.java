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


    @AppSwitch(des = "icon分类开支智能UI", level = Switch.Level.p4)
    public static Boolean openSmartUiInIconCategory = true;

    @AppSwitch(des = "爆款专区获取tpp翻倍开关", level = Switch.Level.p4)
    public static boolean openHotItemDouble = true;

    @AppSwitch(des = "榜单承接页商品兜底文案", level = Switch.Level.p2)
    public static String chengJieDoudi = "{\"COMMENT\":\"近期超市口碑单品\", \"SALE\":\"近期超市热销单品\",\"REPURCHASE\":\"超市热门回购单品\"}";



}
