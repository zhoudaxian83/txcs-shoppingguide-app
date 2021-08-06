package com.tmall.wireless.tac.biz.processor.newTemplate;


import com.taobao.pandora.pandolet.annotation.Service;
import com.tmall.tcls.gs.sdk.ext.boot.ExtensionPointRegister;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextItem;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkItemService;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by yangqing.byq on 2021/7/15.
 */
@Component
public class NewTemplateTestHandler extends RpmReactiveHandler<SgFrameworkResponse<ItemEntityVO>> {

    @Resource
    ExtensionPointRegister extensionPointRegister;
//    @Resource
//    ShoppingguideSdkItemService shoppingguideSdkItemService;

//    @Resource
//    TeItemContextCheckSdkExtPt teItemContextCheckSdkExtPt;
    @Override
    public Flowable<TacResult<SgFrameworkResponse<ItemEntityVO>>> executeFlowable(Context context) throws Exception {
//        SgFrameworkContextItem sgFrameworkContextItem = new SgFrameworkContextItem();
//        return shoppingguideSdkItemService.recommend0(sgFrameworkContextItem).map(
//                TacResult::newResult
//        );
        extensionPointRegister.logExtensionResult();
        SgFrameworkResponse<ItemEntityVO> response = new SgFrameworkResponse<>();
        return Flowable.just(TacResult.newResult(response));
    }
}
