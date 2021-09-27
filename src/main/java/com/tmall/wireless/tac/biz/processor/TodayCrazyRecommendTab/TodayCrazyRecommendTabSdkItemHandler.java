package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab;

import com.alibaba.fastjson.JSON;
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
        BizScenario b = BizScenario.valueOf(
                ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
                ScenarioConstantApp.LOC_TYPE_B2C,
                ScenarioConstantApp.TODAY_CRAZY_RECOMMEND_TAB
        );
        //tac打底
        return shoppingguideSdkItemService.recommend(context, b)
                .map(TacResult::newResult)
                .map(tacResult -> {
                    if (tacResult.getData() == null || tacResult.getData().getItemAndContentList() == null
                            || tacResult.getData().getItemAndContentList().isEmpty()) {
                        tacLogger.info("tac打底,tacresult信息：" + JSON.toJSONString(tacResult));
                        tacResult = TacResult.errorResult("test");
                        HadesLogUtil.stream(ScenarioConstantApp.TODAY_CRAZY_RECOMMEND_TAB)
                                .kv("shoppingguideSdkItemService", "recommend")
                                .kv("tacResult", JSON.toJSONString(tacResult))
                                .info();
                    } else {
                        tacResult.setHasMore(tacResult.getData().isHasMore());
                    }
                    tacResult.getBackupMetaData().setUseBackup(true);
                    return tacResult;
                })
                .onErrorReturn(r -> TacResult.errorResult(""));

    }
}
