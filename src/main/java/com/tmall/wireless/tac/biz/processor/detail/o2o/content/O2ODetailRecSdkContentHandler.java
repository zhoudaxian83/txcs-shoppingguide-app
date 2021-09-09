package com.tmall.wireless.tac.biz.processor.detail.o2o.content;

import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.framework.model.ContentVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkContentService;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.wireless.tac.biz.processor.common.PackageNameKey;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created from template by 归晨 on 2021-09-09 21:01:31.
 *
 */

@Service
public class O2ODetailRecSdkContentHandler extends RpmReactiveHandler<SgFrameworkResponse<ContentVO>> {

    @Autowired
    ShoppingguideSdkContentService shoppingguideSdkContentService;

    @Override
    public Flowable<TacResult<SgFrameworkResponse<ContentVO>>> executeFlowable(Context context) throws Exception {
        BizScenario b = BizScenario.valueOf(
                 "supermarket",
                 "o2o",
                 "contentRec"
        );
         b.addProducePackage(PackageNameKey.CONTENT_FEEDS);

        return shoppingguideSdkContentService.recommend0(context, b).map(TacResult::newResult);
    }
}
