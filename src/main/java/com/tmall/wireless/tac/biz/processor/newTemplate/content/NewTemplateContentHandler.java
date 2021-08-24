package com.tmall.wireless.tac.biz.processor.newTemplate.content;

import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.framework.model.ContentVO;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkContentService;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.wireless.tac.biz.processor.common.PackageNameKey;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NewTemplateContentHandler extends RpmReactiveHandler<SgFrameworkResponse<ContentVO>> {

    @Autowired
    ShoppingguideSdkContentService shoppingguideSdkContentService;

    @Override
    public Flowable<TacResult<SgFrameworkResponse<ContentVO>>> executeFlowable(Context context) throws Exception {
        BizScenario b = BizScenario.valueOf(
                ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
                ScenarioConstantApp.LOC_TYPE_B2C,
                "CONTENT_TEST"
        );
        b.addProducePackage(PackageNameKey.CONTENT_FEEDS);
        return shoppingguideSdkContentService.recommend0(context, b).map(TacResult::newResult);
    }
}
