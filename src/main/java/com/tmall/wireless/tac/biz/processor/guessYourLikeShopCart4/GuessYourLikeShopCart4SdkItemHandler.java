package com.tmall.wireless.tac.biz.processor.guessYourLikeShopCart4;

import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkItemService;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * Created from template by 程斐斐 on 2021-09-14 21:01:19.
 *
 */

@Component
public class GuessYourLikeShopCart4SdkItemHandler extends RpmReactiveHandler<SgFrameworkResponse<ItemEntityVO>> {

    @Autowired
    ShoppingguideSdkItemService shoppingguideSdkItemService;
    @Override
    public Flowable<TacResult<SgFrameworkResponse<ItemEntityVO>>> executeFlowable(Context context) throws Exception {

        BizScenario b = BizScenario.valueOf(
                "supermarket",
                "b2c",
                "guessYourLikeShopCart4"
        );

         
        return shoppingguideSdkItemService.recommend(context, b)
        .map(TacResult::newResult);

    }
}
