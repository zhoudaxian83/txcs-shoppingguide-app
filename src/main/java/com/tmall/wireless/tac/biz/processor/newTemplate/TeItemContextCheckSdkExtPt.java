package com.tmall.wireless.tac.biz.processor.newTemplate;

import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkPackage;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.contextcheck.ContextCheckResult;
import com.tmall.tcls.gs.sdk.framework.extensions.item.contextcheck.ItemContextCheckSdkExtPt;
import com.tmall.wireless.tac.client.domain.Context;

/**
 * Created by yangqing.byq on 2021/8/6.
 */

@SdkExtension(bizId = "cc")
@SdkPackage(packageName = "package")
public class TeItemContextCheckSdkExtPt extends Register implements ItemContextCheckSdkExtPt {
    @Override
    public ContextCheckResult process(Context context) {
        return null;
    }
}
