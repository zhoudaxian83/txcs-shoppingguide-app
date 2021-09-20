package com.tmall.wireless.tac.biz.processor.detail.common.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.taobao.csp.switchcenter.annotation.AppSwitch;
import com.taobao.csp.switchcenter.annotation.NameSpace;
import com.taobao.csp.switchcenter.bean.Switch;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.RecTypeEnum;
import com.tmall.wireless.tac.biz.processor.detail.model.config.SizeDTO;
import com.tmall.wireless.tac.biz.processor.detail.model.config.TppRequestConfig;

/**
 * @author: guichen
 * @Data: 2021/9/10
 * @Description:
 */
@NameSpace(nameSpace = "supermarket.detail")
public class DetailSwitch {

    @AppSwitch(des = "详情推荐appId", level = Switch.Level.p3)
    public static Map<String, TppRequestConfig> appIdMap = new HashMap<String, TppRequestConfig>() {
        {
            put(RecTypeEnum.RECIPE.getType(), new TppRequestConfig(27924L,"138002","7"));
            put(RecTypeEnum.SIMILAR_ITEM_CONTENT.getType(), new TppRequestConfig(27924L,"138002","7"));
            put(RecTypeEnum.SIMILAR_ITEM_ITEM.getType(), new TppRequestConfig(21174L));
        }
    };

    @AppSwitch(des = "详情推荐的限制推荐size", level = Switch.Level.p3)
    public static Map<String, SizeDTO> contentSizeMap = new HashMap<String, SizeDTO>()

    {
        {
            put(RecTypeEnum.RECIPE.getType(), new SizeDTO(2,6));
            put(RecTypeEnum.SIMILAR_ITEM_CONTENT.getType(), new SizeDTO(1,2));
            put(RecTypeEnum.SIMILAR_ITEM_ITEM.getType(), new SizeDTO(6,6));
        }
    };


    @AppSwitch(des = "忽略的营销类型", level = Switch.Level.p3)
    public static Set<String> ignorePromotionList= Sets.newHashSet("O2O_StraightDown");


}
