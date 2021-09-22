package com.tmall.wireless.tac.biz.processor.todayCrazyTab;

import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.contentinfo.ContentInfoQuerySdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.Response;
import com.tmall.tcls.gs.sdk.framework.model.context.ContentInfoDTO;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextContent;
import io.reactivex.Flowable;

import java.util.Map;

/**
 * Created from template by 进舟 on 2021-09-22 16:00:05.
 */

@SdkExtension(
        bizId = "supermarket",
        useCase = "b2c",
        scenario = "todayCrazyTab"
)
public class TodayCrazyTabContentInfoQuerySdkExtPt extends Register implements ContentInfoQuerySdkExtPt {

    @Override
    public Flowable<Response<Map<Long, ContentInfoDTO>>> process(SgFrameworkContextContent sgFrameworkContextContent) {
        return null;
    }
}
