package com.tmall.wireless.tac.biz.processor.icon.item.ext;

import com.alibaba.fastjson.JSON;

import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.filter.ItemProcessBeforeReturnSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextItem;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;

/**
 * @author zhongwei
 * @date 2021/11/10
 */
@SdkExtension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.ICON_ITEM
)
public class IconItemProcessBeforeReturnSdkExtPt extends Register implements ItemProcessBeforeReturnSdkExtPt {
    @Override
    public SgFrameworkContextItem process(SgFrameworkContextItem sgFrameworkContextItem) {
        LOGGER.error("IconItemProcessBeforeReturnSdkExtPt:{}", JSON.toJSONString(sgFrameworkContextItem.getEntityVOSgFrameworkResponse()));
        return sgFrameworkContextItem;
    }
}
