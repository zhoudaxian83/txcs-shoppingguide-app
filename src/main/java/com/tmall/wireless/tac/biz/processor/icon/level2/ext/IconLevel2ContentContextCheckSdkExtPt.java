package com.tmall.wireless.tac.biz.processor.icon.level2.ext;

import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.context.ContentContextCheckSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.item.contextcheck.ContextCheckResult;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.icon.level2.Level2RecommendService;
import com.tmall.wireless.tac.biz.processor.icon.level2.Level2Request;
import com.tmall.wireless.tac.client.domain.Context;
import org.springframework.stereotype.Service;

@Service
@SdkExtension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C
        , scenario = ScenarioConstantApp.ICON_CONTENT_LEVEL2
)
public class IconLevel2ContentContextCheckSdkExtPt extends Register implements ContentContextCheckSdkExtPt {
    @Override
    public ContextCheckResult process(Context context) {
        Object o = context.get(Level2RecommendService.level2RequestKey);
        if (o instanceof Level2Request) {
            return ContextCheckResult.success();
        } else {
            return ContextCheckResult.fail("level2Request not find");
        }
    }
}
