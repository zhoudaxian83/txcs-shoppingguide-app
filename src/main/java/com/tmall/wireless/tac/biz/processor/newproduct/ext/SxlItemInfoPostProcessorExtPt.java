package com.tmall.wireless.tac.biz.processor.newproduct.ext;

import com.tmall.txcs.gs.framework.extensions.itemdatapost.ItemInfoPostProcessorExtPt;
import com.tmall.txcs.gs.framework.extensions.itemdatapost.ItemInfoPostProcessorResp;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.model.Response;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author haixiao.zhang
 * @date 2021/6/10
 */
public class SxlItemInfoPostProcessorExtPt implements ItemInfoPostProcessorExtPt {

    @Autowired
    TacLogger tacLogger;

    @Override
    public Response<ItemInfoPostProcessorResp> process(SgFrameworkContextItem sgFrameworkContextItem) {

        return Response.success(new ItemInfoPostProcessorResp());
    }
}
