package com.tmall.wireless.tac.biz.processor.todayCrazyTab;

import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.framework.model.ContentVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkContentService;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.wireless.tac.biz.processor.common.PackageNameKey;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created from template by 进舟 on 2021-09-22 11:17:10.
 *
 */

@Service
public class TodayCrazyTabSdkContentHandler extends RpmReactiveHandler<SgFrameworkResponse<ContentVO>> {

    @Autowired
    ShoppingguideSdkContentService shoppingguideSdkContentService;

    @Override
    public Flowable<TacResult<SgFrameworkResponse<ContentVO>>> executeFlowable(Context context) throws Exception {
        BizScenario b = BizScenario.valueOf(
                 "supermarket",
                 "b2c",
                 "todayCrazyTab"
        );

        b.addProducePackage(HallScenarioConstant.HALL_CONTENT_SDK_PACKAGE);
        b.addProducePackage(PackageNameKey.OLD_RECOMMEND);

        return shoppingguideSdkContentService.recommend0(context, b).map(TacResult::newResult);
    }
}
