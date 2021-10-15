package com.tmall.wireless.tac.biz.processor.detail.o2o.item;

import java.util.Set;

import com.alibaba.cola.extension.Extension;

import com.google.common.collect.Sets;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.framework.extensions.item.contextcheck.ContextCheckResult;
import com.tmall.tcls.gs.sdk.framework.extensions.item.contextcheck.ItemContextCheckSdkExtPt;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.DetailConstant;
import com.tmall.wireless.tac.biz.processor.detail.common.extabstract.AbstractDetailContextCheckExtPt;
import com.tmall.wireless.tac.client.domain.Context;
import org.springframework.stereotype.Service;

/**
 * @author: guichen
 * @Data: 2021/9/13
 * @Description:
 */
@SdkExtension(bizId = DetailConstant.BIZ_ID,
    useCase = DetailConstant.USE_CASE_O2O,
    scenario = DetailConstant.ITEM_SCENERIO)
public class O2ODetailRecItemContextCheckExtPt  extends AbstractDetailContextCheckExtPt implements ItemContextCheckSdkExtPt {

    @Override
    public ContextCheckResult process(Context context) {

        Set<String> checkParams = Sets.newHashSet("recType", "detailItemId", "smAreaId", "locType",
            "pageSize", "index");
        return super.process(context,checkParams);
    }
}
