package com.tmall.tcls.gs.sdk.framework.extensions.content.atemp;


import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.context.ContentContextCheckSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.item.contextcheck.ContextCheckResult;
import com.tmall.wireless.tac.client.domain.Context;

/**
 * Created from template by 归晨 on 2021-09-10 11:08:02.
 */

@SdkExtension(
        bizId = "supermarket",
        useCase = "o2o",
        scenario = "o2odetailrec"
)
public class O2odetailrecContentContextCheckSdkExtPt extends Register implements ContentContextCheckSdkExtPt {
    @Override
    public ContextCheckResult process(Context context) {
        return null;
    }
}
