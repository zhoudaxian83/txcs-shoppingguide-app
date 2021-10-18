package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab;

import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkItemService;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * Created from template by 罗俊冲 on 2021-09-15 17:51:57.
 */

@Component
public class TodayCrazyRecommendTabSdkItemHandler extends RpmReactiveHandler<SgFrameworkResponse<ItemEntityVO>> {

    @Autowired
    ShoppingguideSdkItemService shoppingguideSdkItemService;

    @Autowired
    TacLoggerImpl tacLogger;

    @Override
    public Flowable<TacResult<SgFrameworkResponse<ItemEntityVO>>> executeFlowable(Context context) throws Exception {
        BizScenario bizScenario = BizScenario.valueOf(
                ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
                ScenarioConstantApp.LOC_TYPE_B2C,
                ScenarioConstantApp.TODAY_CRAZY_RECOMMEND_TAB
        );
        //tac打底,参考TacResultBackupUtil方法自定义，注意根据日志关键字监控
        return shoppingguideSdkItemService.recommend(context, bizScenario)
                .map(TacResult::newResult)
                .map(tacResult -> {
                    if (StringUtils.isEmpty(bizScenario.getUniqueIdentity())) {
                        tacResult.getBackupMetaData().setUseBackup(true);
                        return tacResult;
                    }
                    if (tacResult == null || tacResult.getData() == null || CollectionUtils.isEmpty(tacResult.getData().getItemAndContentList())) {
                        tacResult = TacResult.errorResult("TacResultBackup");
                        HadesLogUtil.stream(bizScenario.getUniqueIdentity())
                                .kv("key", "tacBackup")
                                .kv("tacResultBackup", "true")
                                .info();
                    } else {
                        HadesLogUtil.stream(bizScenario.getUniqueIdentity())
                                .kv("key", "tacBackup")
                                .kv("tacResultBackup", "false")
                                .info();
                    }
                    tacResult.getBackupMetaData().setUseBackup(true);
                    return tacResult;
                });


    }
}
