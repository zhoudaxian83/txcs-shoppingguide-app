package com.tmall.wireless.tac.biz.processor.gcsfeeds;

import com.alibaba.cola.extension.Extension;
import com.tmall.txcs.gs.framework.extensions.content.ContentInfoQueryExtPt;
import com.tmall.txcs.gs.framework.extensions.content.ContentInfoQueryRequest;
import com.tmall.txcs.gs.model.Response;
import com.tmall.txcs.gs.model.content.ContentDTO;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import io.reactivex.Flowable;

import java.util.Map;

/**
 * Created by yangqing.byq on 2021/4/18.
 */
@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.SCENARIO_GCS_FEEDS)
public class GcsfeedsContentInfoQueryExtPt extends ContentInfoQueryExtPt {
    @Override
    public Flowable<Response<Map<Long, ContentDTO>>> process(ContentInfoQueryRequest contentInfoQueryRequest) {
        return Flowable.just(Response.fail(""));
    }
}
