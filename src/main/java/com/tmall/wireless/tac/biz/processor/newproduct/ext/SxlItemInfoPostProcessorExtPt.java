package com.tmall.wireless.tac.biz.processor.newproduct.ext;

import com.tmall.txcs.gs.framework.extensions.itemdatapost.ItemInfoPostProcessorExtPt;
import com.tmall.txcs.gs.framework.extensions.itemdatapost.ItemInfoPostProcessorResp;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.model.Response;

/**
 * @author haixiao.zhang
 * @date 2021/6/10
 */
public class SxlItemInfoPostProcessorExtPt implements ItemInfoPostProcessorExtPt {
    @Override
    public Response<ItemInfoPostProcessorResp> process(SgFrameworkContextItem sgFrameworkContextItem) {

        sgFrameworkContextItem.getItemInfoGroupResponseMap();

        return null;
    }
}
