package com.tmall.wireless.tac.biz.processor.detail.common.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.taobao.csp.switchcenter.annotation.AppSwitch;
import com.taobao.csp.switchcenter.annotation.NameSpace;
import com.taobao.csp.switchcenter.bean.Switch;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.RecTypeEnum;
import com.tmall.wireless.tac.biz.processor.detail.model.config.DetailRequestConfig;
import com.tmall.wireless.tac.biz.processor.detail.model.config.SizeDTO;

/**
 * @author: guichen
 * @Data: 2021/9/10
 * @Description:
 */
@NameSpace(nameSpace = "supermarket.detail")
public class DetailSwitch {

    @AppSwitch(des = "详情推荐的限制推荐size", level = Switch.Level.p3)
    public static Map<String, DetailRequestConfig> requestConfigMap=new HashMap<String,DetailRequestConfig>(){
        {
            put(RecTypeEnum.RECIPE.getType(), new DetailRequestConfig(28151L,new SizeDTO(3,6)));
            put(RecTypeEnum.SIMILAR_ITEM_CONTENT.getType(),new DetailRequestConfig(28155L, new SizeDTO(1,2)));
            put(RecTypeEnum.SIMILAR_ITEM_ITEM.getType(), new DetailRequestConfig(21174L,new SizeDTO(6,6)));
            put(RecTypeEnum.SIMILAR_ITEM_CONTENT_ITEM.getType(), new DetailRequestConfig(27506L,new SizeDTO(6,6),true,1));

        }
    };

    @AppSwitch(des = "忽略的营销类型", level = Switch.Level.p3)
    public static Set<String> ignorePromotionList= Sets.newHashSet("O2O_StraightDown");

    @AppSwitch(des="详情透传参数",level= Switch.Level.p3)
    public static Set<String> detailThoughParams = Sets.newHashSet("sourceChannel");

    @AppSwitch(des="详情菜谱推荐模块是否出普通场景推荐开关",level= Switch.Level.p3)
    public static boolean enableReciptCommonContent=false;

    @AppSwitch(des="详情菜谱推荐模块普通场景定制承接页URL",level= Switch.Level.p3)
    public static String reciptCommonContentJumpUrl="https://pages.tmall.com/wow/an/cs/act/wupr?spm=a3204.21125900.9715263030.d_b2cNormalContent_2020053220241_623375402419&wh_biz=tm&wh_pid=go-shopping%2F1774bde7dd7&disableNav=YES&recommendForUTab=true&position=0";
}
