package com.tmall.wireless.tac.biz.processor.icon.level2.ext;

import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.context.ContentMetaInfoBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.meta.ContentMetaInfo;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.domain.Context;
import org.springframework.stereotype.Service;

@Service
@SdkExtension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C
        , scenario = ScenarioConstantApp.ICON_CONTENT_LEVEL2
)
public class IconLevel2ContentMetaInfoBuildSdkExtPt extends Register  implements ContentMetaInfoBuildSdkExtPt {
    @Override
    public ContentMetaInfo process(Context context) {
        ContentMetaInfo contentMetaInfo = new ContentMetaInfo();
        contentMetaInfo.setQueryItemList(false);
        return contentMetaInfo;
    }
}
