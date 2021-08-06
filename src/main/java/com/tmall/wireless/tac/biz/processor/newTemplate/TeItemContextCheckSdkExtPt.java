package com.tmall.wireless.tac.biz.processor.newTemplate;

import com.tmall.tcls.gs.sdk.framework.extensions.item.contextcheck.ContextCheckResult;
import com.tmall.tcls.gs.sdk.framework.extensions.item.contextcheck.ItemContextCheckSdkExtPt;
import com.tmall.txcs.gs.sdk.ext.annotation.Extension;
import com.tmall.txcs.gs.sdk.ext.extension.Register;
import com.tmall.wireless.tac.client.domain.Context;
import org.springframework.stereotype.Service;

/**
 * Created by yangqing.byq on 2021/8/6.
 */
@Service
@Extension(bizId = "cc")
public class TeItemContextCheckSdkExtPt extends Register implements ItemContextCheckSdkExtPt {
    @Override
    public ContextCheckResult process(Context context) {
        return null;
    }
}
