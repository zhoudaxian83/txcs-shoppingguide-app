package com.tmall.wireless.tac.biz.processor.extremeItem;

import com.alibaba.fastjson.JSON;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkItemService;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * Created from template by 言武 on 2021-09-10 14:36:48.
 *
 */

@Component
public class ExtremeItemSdkItemHandler extends RpmReactiveHandler<SgFrameworkResponse<ItemEntityVO>> {

    @Autowired
    ShoppingguideSdkItemService shoppingguideSdkItemService;
    @Autowired
    TacLogger tacLogger;

    @Override
    public Flowable<TacResult<SgFrameworkResponse<ItemEntityVO>>> executeFlowable(Context context) throws Exception {
        tacLogger.info("context:" + JSON.toJSONString(context));
        return null;

    }
}
