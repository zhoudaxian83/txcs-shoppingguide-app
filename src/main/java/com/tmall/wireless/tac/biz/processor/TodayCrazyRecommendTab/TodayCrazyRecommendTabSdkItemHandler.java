package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;

import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkItemService;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.config.SxlSwitch;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.tmall.wireless.tac.biz.processor.icon.item.ext.IconItemItemMetaInfoBuildSdkExtPt.AB_TEST_CODE;

/**
 * Created from template by 罗俊冲 on 2021-09-15 17:51:57.
 */

@Component
public class TodayCrazyRecommendTabSdkItemHandler extends RpmReactiveHandler<SgFrameworkResponse<ItemEntityVO>> {

    /**
     * ab实验分桶结果
     **/
    private final static String AB_TEST_RESULT = "abTestVariationsResult";

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
        String todayCrazyItemPicUrlField = getTodayCrazyItemPicUrlField(context);
        //tac打底,参考TacResultBackupUtil方法自定义，注意根据日志关键字监控
        return shoppingguideSdkItemService.recommend(context, bizScenario)
            .map(response -> {
                response.getExtInfos().put("todayCrazyItemPicUrlField", todayCrazyItemPicUrlField);
                return response;
            })
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

    /**
     * 获取今日疯抢tab页出白底图还是主图的ab测试结果
     *
     * @param context
     * @return
     */
    private String getTodayCrazyItemPicUrlField(Context context) {
        StringBuilder itemPicUrl = new StringBuilder();
        try {
            if (context.getParams().get(AB_TEST_RESULT) == null || StringUtils.isBlank(context.getParams().get(AB_TEST_RESULT).toString())) {
                HadesLogUtil.stream(ScenarioConstantApp.TODAY_CRAZY_TAB_ITEM)
                    .kv("TodayCrazyRecommendTabSdkItemHandler context.getParams()", JSON.toJSONString(context.getParams()))
                    .info();
                return itemPicUrl.toString();
            }
            List<Map<String, Object>> abTestRest = (List<Map<String, Object>>)context.getParams().get(AB_TEST_RESULT);
            if (CollectionUtils.isEmpty(abTestRest)) {
                HadesLogUtil.stream(ScenarioConstantApp.TODAY_CRAZY_TAB_ITEM)
                    .kv("TodayCrazyRecommendTabSdkItemHandler context.getParams().get(AB_TEST_RESULT)", JSON.toJSONString(context.getParams()))
                    .info();
                return itemPicUrl.toString();
            }
            HadesLogUtil.stream(ScenarioConstantApp.TODAY_CRAZY_TAB_ITEM)
                .kv("TodayCrazyRecommendTabSdkItemHandler abTestRest", JSON.toJSONString(abTestRest))
                .info();
            abTestRest.forEach(variation -> {
                String todayCrazyAbTest = AB_TEST_CODE;
                String todayCrazyItemPicUrlIdAb = SxlSwitch.TODAY_CRAZY_ITEM_PIC_URL_ID_AB;
                HadesLogUtil.stream(ScenarioConstantApp.TODAY_CRAZY_TAB_ITEM)
                    .kv("TodayCrazyRecommendTabSdkItemHandler", "getSxlTrialMergeAbData")
                    .kv("TODAY_CRAZY_AB_TEST", todayCrazyAbTest)
                    .kv("sxlAlgItemsetIdAb", todayCrazyItemPicUrlIdAb)
                    .info();
                if (todayCrazyAbTest.equals(variation.get("bizType")) &&
                    todayCrazyItemPicUrlIdAb.equals(variation.get("tclsExpId"))) {
                    if (variation.get("hasTrialMoudle") != null) {
                        itemPicUrl.append(variation.get("hasTrialMoudle"));
                    }
                }
            });
        } catch (Exception e) {
            HadesLogUtil.stream(ScenarioConstantApp.TODAY_CRAZY_TAB_ITEM)
                .kv("TodayCrazyRecommendTabSdkItemHandler getAbData", JSON.toJSONString(context.getParams()))
                .kv("e.getMessage()", JSON.toJSONString(e))
                .info();
        }
        HadesLogUtil.stream(ScenarioConstantApp.TODAY_CRAZY_TAB_ITEM)
            .kv("TodayCrazyRecommendTabSdkItemHandler itemSetIdType", itemPicUrl.toString())
            .info();
        return itemPicUrl.toString();
    }
}
