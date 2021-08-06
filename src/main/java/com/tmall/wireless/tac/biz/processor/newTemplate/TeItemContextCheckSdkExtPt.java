package com.tmall.wireless.tac.biz.processor.newTemplate;

import com.tmall.tcls.gs.sdk.framework.extensions.item.contextcheck.ContextCheckResult;
import com.tmall.tcls.gs.sdk.framework.extensions.item.contextcheck.ItemContextCheckSdkExtPt;
import com.tmall.txcs.gs.sdk.ext.annotation.Extension;
import com.tmall.wireless.tac.client.domain.Context;
import org.springframework.stereotype.Service;

/**
 * Created by yangqing.byq on 2021/8/6.
 */
@Service
@Extension
public class TeItemContextCheckSdkExtPt implements ItemContextCheckSdkExtPt {
    @Override
    public ContextCheckResult process(Context context) {
        return null;
    }
}
