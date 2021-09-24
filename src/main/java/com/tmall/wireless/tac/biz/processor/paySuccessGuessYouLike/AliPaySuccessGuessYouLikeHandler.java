package com.tmall.wireless.tac.biz.processor.paySuccessGuessYouLike;

import com.alibaba.fastjson.JSON;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkItemService;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.wireless.tac.biz.processor.common.PackageNameKey;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import io.reactivex.Flowable;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * 支付成功页面猜你喜欢商品列表
 */
@Component
public class AliPaySuccessGuessYouLikeHandler extends RpmReactiveHandler<SgFrameworkResponse<ItemEntityVO>> {

    @Autowired
    ShoppingguideSdkItemService shoppingguideSdkItemService;

    @Autowired
    TacLoggerImpl tacLogger;
    @Override
    public Flowable<TacResult<SgFrameworkResponse<ItemEntityVO>>> executeFlowable(Context context) throws Exception {

        tacLogger.info("进入handler");
        BizScenario bizScenario = BizScenario.valueOf(
                ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
                ScenarioConstantApp.LOC_TYPE_B2C,
                ScenarioConstantApp.PAY_FOR_SUCCESS_GUESS_YOU_LIKE);

        bizScenario.addProducePackage(PackageNameKey.OLD_RECOMMEND);
        bizScenario.addProducePackage(PackageNameKey.CONTENT_FEEDS);
        return shoppingguideSdkItemService.recommend(context, bizScenario)
                .map(TacResult::newResult)
                .map(tacResult -> {

                    if(tacResult.getData() == null || tacResult.getData().getItemAndContentList() == null
                            || tacResult.getData().getItemAndContentList().isEmpty()){
                        tacLogger.info("进入tac打底");
                        tacLogger.info("tacresult信息：" + JSON.toJSONString(tacResult));
                        tacResult = TacResult.errorResult("test");
                        HadesLogUtil.stream(ScenarioConstantApp.PAY_FOR_SUCCESS_GUESS_YOU_LIKE)
                                .kv("shoppingguideSdkItemService","recommend")
                                .kv("tacResult", JSON.toJSONString(tacResult))
                                .info();

                        tacLogger.info("tacresult打底结束");
                    }

                    tacLogger.info("tacresult开始判断=====================：1");
                    tacResult.getBackupMetaData().setUseBackup(true);
                    tacLogger.info("tacresult开始判断=====================：2");
                    tacResult.setHasMore(false);
                    tacResult.getData().setHasMore(false);
                    tacLogger.info("tacresult开始判断=====================：3");
                    SgFrameworkResponseChild  child = new SgFrameworkResponseChild();
                    BeanUtils.copyProperties(tacResult, child);
                    child.setMinimumGuarantee(true);
                    tacLogger.info("tacresult开始判断=====================：4");
                    tacResult.setData(child);
                    tacLogger.info("tacresult信息：" + JSON.toJSONString(tacResult));
                    return tacResult;
                })
                .onErrorReturn(r -> TacResult.errorResult(""));
    }
}
