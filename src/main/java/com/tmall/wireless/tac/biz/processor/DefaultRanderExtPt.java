package com.tmall.wireless.tac.biz.processor;

import com.alibaba.cola.extension.Extension;
import com.tmall.recommend.framework.ItemRanderExtPt;
import org.springframework.stereotype.Component;

/**
 * Created by yangqing.byq on 2021/1/24.
 */
@Extension
@Component("defaultRanderExtPt")
public class DefaultRanderExtPt implements ItemRanderExtPt<Object> {
    @Override
    public Object renderItems(Object o) {
        return "default";
    }
}
