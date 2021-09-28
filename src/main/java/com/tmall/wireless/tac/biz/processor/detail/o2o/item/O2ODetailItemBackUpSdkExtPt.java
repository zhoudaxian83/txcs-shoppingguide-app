package com.tmall.wireless.tac.biz.processor.detail.o2o.item;

import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextItem;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.DetailConstant;
import com.tmall.wireless.tac.biz.processor.detail.common.extabstract.AbstractDetailItemBackUpSdkExtPt;

/**
 * @author: guichen
 * @Data: 2021/9/28
 * @Description:
 */
@SdkExtension(bizId = DetailConstant.BIZ_ID,
    useCase = DetailConstant.USE_CASE_O2O,
    scenario = DetailConstant.ITEM_SCENERIO)
public class O2ODetailItemBackUpSdkExtPt extends AbstractDetailItemBackUpSdkExtPt {
    @Override
    public SgFrameworkContextItem process(SgFrameworkContextItem sgFrameworkContextItem) {
        return super.process(sgFrameworkContextItem);
    }
}
