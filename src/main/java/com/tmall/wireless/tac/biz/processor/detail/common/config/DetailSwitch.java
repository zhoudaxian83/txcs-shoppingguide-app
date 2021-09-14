package com.tmall.wireless.tac.biz.processor.detail.common.config;

import java.util.HashMap;
import java.util.Map;

import com.taobao.csp.switchcenter.annotation.AppSwitch;
import com.taobao.csp.switchcenter.annotation.NameSpace;
import com.taobao.csp.switchcenter.bean.Switch;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.RecTypeEnum;

/**
 * @author: guichen
 * @Data: 2021/9/10
 * @Description:
 */
@NameSpace(nameSpace = "supermarket.detail")
public class DetailSwitch {

    @AppSwitch(des = "人工选品推荐选品集id", level = Switch.Level.p3)
    public static Map<String,Long> appIdMap =new HashMap<String, Long>() {
        {
            put(RecTypeEnum.RECIPE.getType(),23198L);
            put(RecTypeEnum.SIMILAR_ITEM_CONTENT.getType(), 23198L);
            put(RecTypeEnum.SIMILAR_ITEM_ITEM.getType(),25385L);
        }
    };

}
