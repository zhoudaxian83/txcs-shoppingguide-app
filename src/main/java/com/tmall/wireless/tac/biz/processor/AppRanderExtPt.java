package com.tmall.wireless.tac.biz.processor;

import com.alibaba.cola.extension.Extension;
import com.tmall.recommend.framework.ItemRanderExtPt;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * Created by yangqing.byq on 2021/1/24.
 */
@Extension(scenario = "s3")
@Component("appRanderExtPt")
public class AppRanderExtPt implements ItemRanderExtPt<Object> {
    @Override
    public Object renderItems(Object o) {
        return "s3";
    }
}
