package com.tmall.wireless.tac.biz.processor.paySuccessGuessYouLike;

import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkItemService;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * 支付成功页面猜你喜欢商品列表
 */
@Component
public class AliPaySuccessGuessYouLikeHandler extends RpmReactiveHandler<SgFrameworkResponse<ItemEntityVO>> {

    @Autowired
    ShoppingguideSdkItemService shoppingguideSdkItemService;
    @Override
    public Flowable<TacResult<SgFrameworkResponse<ItemEntityVO>>> executeFlowable(Context context) throws Exception {

        BizScenario bizScenario = BizScenario.valueOf(
                ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
                ScenarioConstantApp.LOC_TYPE_B2C,
                ScenarioConstantApp.PAY_FOR_SUCCESS_GUESS_YOU_LIKE);

        Flowable<TacResult<SgFrameworkResponse<ItemEntityVO>>> tacResultFlowable = shoppingguideSdkItemService.recommend(context, bizScenario)
                .map(TacResult::newResult)
                .map(tacResult -> {
                    if(bizScenario == null || StringUtils.isEmpty(bizScenario.getUniqueIdentity())){
                        tacResult.getBackupMetaData().setUseBackup(true);
                        return tacResult;
                    }
                    if(tacResult.getData() == null || tacResult.getData()== null || CollectionUtils.isEmpty(tacResult.getData().getItemAndContentList())){
                        tacResult = TacResult.errorResult("TacResultBackup");

                        HadesLogUtil.stream(bizScenario.getUniqueIdentity())
                                .kv("key","tacBackup")
                                .kv("tacResultBackup","true")
                                .info();
                    }else{
                        HadesLogUtil.stream(bizScenario.getUniqueIdentity())
                                .kv("key","tacBackup")
                                .kv("tacResultBackup","false")
                                .info();
                    }
                    tacResult.getBackupMetaData().setUseBackup(true);
                    return tacResult;
                })
                .onErrorReturn(r -> TacResult.errorResult(""));

        return tacResultFlowable;
    }
}
