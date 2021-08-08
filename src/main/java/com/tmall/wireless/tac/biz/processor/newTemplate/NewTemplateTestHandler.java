package com.tmall.wireless.tac.biz.processor.newTemplate;


import com.taobao.pandora.pandolet.annotation.Service;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.ext.boot.ExtensionPointRegister;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextItem;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkItemService;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
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
    ShoppingguideSdkItemService shoppingguideSdkItemService;
    @Override
    public Flowable<TacResult<SgFrameworkResponse<ItemEntityVO>>> executeFlowable(Context context) throws Exception {

        BizScenario bizScenario = BizScenario.valueOf(ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
                ScenarioConstantApp.LOC_TYPE_B2C,
                ScenarioConstantApp.SCENARIO_SHANG_XIN_CONTENT);

        return shoppingguideSdkItemService.recommend(context, bizScenario)
        .map(TacResult::newResult);

    }
}
