package com.tmall.tcls.gs.sdk.framework.extensions.content.atemp;

import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.context.ContentMetaInfoBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.meta.ContentMetaInfo;
import com.tmall.wireless.tac.client.domain.Context;

/**
 * Created from template by 进舟 on 2021-09-22 16:00:05.
 */

@SdkExtension(
        bizId = "supermarket",
        useCase = "b2c",
        scenario = "todayCrazyTab"
)
public class TodayCrazyTabContentMetaInfoBuildSdkExtPt extends Register implements ContentMetaInfoBuildSdkExtPt {
    @Override
    public ContentMetaInfo process(Context context) {
        ContentMetaInfo contentMetaInfo = new ContentMetaInfo();
        contentMetaInfo.setQueryItemList(false);
        return contentMetaInfo;
    }
}
