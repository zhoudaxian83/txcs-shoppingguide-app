package com.tmall.wireless.tac.biz.processor.guessWhatYouLikeMenu.handler;

import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.framework.model.ContentVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkContentService;

import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.wireless.tac.biz.processor.common.PackageNameKey;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Yushan
 * @date 2021/8/31 4:13 下午
 */
@Component
public class GuessWhatYouLikeMenuHandler extends RpmReactiveHandler<SgFrameworkResponse<ContentVO>> {

    @Resource
    private ShoppingguideSdkContentService shoppingguideSdkContentService;

    @Override
    public Flowable<TacResult<SgFrameworkResponse<ContentVO>>> executeFlowable(Context context) throws Exception {
        BizScenario bizScenario = BizScenario.valueOf(ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
                ScenarioConstantApp.LOC_TYPE_B2C,
                ScenarioConstantApp.B2C_CNXH_MENU_FEEDS);
        bizScenario.addProducePackage(PackageNameKey.CONTENT_FEEDS);
        return shoppingguideSdkContentService.recommend(context, bizScenario).map(TacResult::newResult);
    }
}
