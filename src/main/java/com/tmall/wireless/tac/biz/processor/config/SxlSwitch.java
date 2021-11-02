package com.tmall.wireless.tac.biz.processor.config;

import com.taobao.csp.switchcenter.annotation.AppSwitch;
import com.taobao.csp.switchcenter.annotation.NameSpace;
import com.taobao.csp.switchcenter.bean.Switch;
import com.taobao.csp.switchcenter.bean.Switch.Level;
import com.taobao.csp.switchcenter.core.SwitchManager;

/**
 * @author haixiao.zhang
 * @date 2021/6/17
 */
@NameSpace(nameSpace = "sxl")
public class SxlSwitch {


    @AppSwitch(des = "人工选品推荐选品集id", level = Switch.Level.p4)
    public static Long SXL_ITEMSET_ID = 378428L;

    //@AppSwitch(des = "商品推荐选品集id", level = Switch.Level.p4)
    //public static Long SXL_ITEMSET_ID = 322385L;

    @AppSwitch(des = "算法选品推荐选品集id", level = Switch.Level.p4)
    public static Long SXL_ALG_ITEMSET_ID = 387450L;


    @AppSwitch(des = "商品推荐tppId", level = Switch.Level.p4)
    public static Long SXL_TPP_APP_ID = 25385L;

    @AppSwitch(des = "招商主活动id", level = Switch.Level.p4)
    public static Long SXL_MAIN_ACTIVEX_ID = 885L;

    @AppSwitch(des = "爆款专区打底", level = Level.p2)
    public static Integer backUpHotItem = 1000;

    @AppSwitch(des = "o2o榜单跳转链接", level = Switch.Level.p4)
    public static String O2O_BD_JUMP_UTL = "https://pages.tmall.com/wow/an/cs/act/wupr?wh_biz=tm&wh_pid=scenes/17a79a93163&disableNav=YES&contentId=%s&contentType=%s&itemSetIds=%s&entryItemIds=%s";
    /**预发**/
    //public static String O2O_BD_JUMP_UTL = "https://pre-wormhole.wapa.tmall.com/wow/an/cs/act/wupr?wh_biz=tm&wh_pid=go-shopping/1773d7acee3&disableNav=YES&contentId=%s&contentType=%s&itemSetIds=%s&entryItemIds=%s";

    @AppSwitch(des = "买买菜跳转链接", level = Switch.Level.p4)
    public static String MMC_JUMP_UTL = "https://pre-wormhole.tmall.com/wow/an/cs/act/wupr?disableNav=YES&wh_biz=tm&wh_pid=o2o-mmc/dev&sourceChannel=mmc-halfday&channel=halfday&pha=true";

    @AppSwitch(des = "清单会场入口请求tpp个数上线", level = Switch.Level.p4)
    public static Integer inventoryEntranceModuleQueryTppSizeLimit = 6;

    @AppSwitch(des = "上新了超市格物abTest业务域code", level = Switch.Level.p4)
    public static String SM_NEW_ARRIVAL = "SM_NEW_ARRIVAL";

    @AppSwitch(des = "上新了超市算法选品ab实验id", level = Switch.Level.p4)
    public static String SXL_ALG_ITEMSET_ID_AB = "129";

    @AppSwitch(des = "爆款专区获取tpp翻倍开关", level = Switch.Level.p4)
    public static boolean openHotItemDouble = true;




}
