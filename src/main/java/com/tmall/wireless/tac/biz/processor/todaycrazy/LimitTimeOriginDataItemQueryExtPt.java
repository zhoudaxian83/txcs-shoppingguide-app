package com.tmall.wireless.tac.biz.processor.todaycrazy;

import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataItemQueryExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import io.reactivex.Flowable;

public class LimitTimeOriginDataItemQueryExtPt implements OriginDataItemQueryExtPt {
    @Override
    public Flowable<OriginDataDTO<ItemEntity>> process(SgFrameworkContextItem sgFrameworkContextItem) {
        return null;
    }
}
