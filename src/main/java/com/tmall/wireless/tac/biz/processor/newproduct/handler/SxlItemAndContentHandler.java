package com.tmall.wireless.tac.biz.processor.newproduct.handler;

import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.txcs.gs.framework.model.ContentVO;
import com.tmall.txcs.gs.framework.model.EntityVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.wireless.tac.biz.processor.newproduct.service.SxlContentRecService;
import com.tmall.wireless.tac.biz.processor.newproduct.service.SxlItemRecService;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author haixiao.zhang
 * @date 2021/6/8
 */
public class SxlItemAndContentHandler extends RpmReactiveHandler<SgFrameworkResponse<EntityVO>> {


    @Autowired
    private SxlItemRecService sxlItemRecService;

    @Autowired
    TacLogger tacLogger;

    @Autowired
    private SxlContentRecService sxlContentRecService;


    @Override
    public Flowable<TacResult<SgFrameworkResponse<EntityVO>>> executeFlowable(Context context) throws Exception{


        Flowable<TacResult<SgFrameworkResponse<ContentVO>>> content = sxlContentRecService.recommend(context);

        Flowable<TacResult<SgFrameworkResponse<EntityVO>>> item = sxlItemRecService.recommend(context);

        return Flowable.zip(content,item, (contentInfo, itemInfo) -> mergeContentAndItem(contentInfo, itemInfo));

    }


    private TacResult<SgFrameworkResponse<EntityVO>>  mergeContentAndItem(TacResult<SgFrameworkResponse<ContentVO>> contentInfo,
                                                           TacResult<SgFrameworkResponse<EntityVO>> itemInfo) {

        EntityVO entityVO = new EntityVO();
        entityVO.put("banner",contentInfo.getData().getItemAndContentList().get(0));
        itemInfo.getData().getItemAndContentList().add(0,entityVO);

        return itemInfo;

    }
}
