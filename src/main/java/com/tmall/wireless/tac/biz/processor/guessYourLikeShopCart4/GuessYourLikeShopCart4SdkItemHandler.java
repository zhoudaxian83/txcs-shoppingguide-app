package com.tmall.wireless.tac.biz.processor.guessYourLikeShopCart4;

import com.alibaba.fastjson.JSON;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkItemService;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
    @Autowired
    TacLoggerImpl tacLogger;
    @Override
    public Flowable<TacResult<SgFrameworkResponse<ItemEntityVO>>> executeFlowable(Context context) throws Exception {

        BizScenario bizScenario = BizScenario.valueOf(
                "supermarket",
                "b2c",
                "guessYourLikeShopCart4"
        );

        return shoppingguideSdkItemService.recommend(context, bizScenario)
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
    }
}
