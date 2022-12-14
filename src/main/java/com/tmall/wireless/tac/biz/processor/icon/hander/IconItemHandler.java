package com.tmall.wireless.tac.biz.processor.icon.hander;

import com.alibaba.fastjson.JSON;

import com.google.common.collect.Lists;
import com.taobao.util.CollectionUtil;
import com.tmall.aself.shoppingguide.client.cat.model.LabelDTO;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.icon.item.ItemRecommendService;
import com.tmall.wireless.tac.biz.processor.icon.item.ItemRequest;
import com.tmall.wireless.tac.biz.processor.icon.level2.Level2RecommendService;
import com.tmall.wireless.tac.biz.processor.icon.level3.Level3RecommendService;
import com.tmall.wireless.tac.biz.processor.icon.level3.Level3Request;
import com.tmall.wireless.tac.biz.processor.icon.model.IconResponse;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class IconItemHandler extends RpmReactiveHandler<IconResponse> {

    Logger LOGGER = LoggerFactory.getLogger(IconItemHandler.class);

    @Autowired
    Level2RecommendService level2RecommendService;
    @Autowired
    Level3RecommendService level3RecommendService;
    @Autowired
    ItemRecommendService itemRecommendService;

    @Override
    public Flowable<TacResult<IconResponse>> executeFlowable(Context context) throws Exception {

        ItemRequest itemRequest = new ItemRequest();
        String level2Id = Optional.ofNullable(context.get("level2Id")).map(Object::toString).orElse("");
        String level3Id = Optional.ofNullable(context.get("level3Id")).map(Object::toString).orElse("");
        String level1Id = Optional.ofNullable(context.get("iconType")).map(Object::toString).orElse("");

        itemRequest.setLevel1Id(level1Id);
        itemRequest.setLevel2Id(level2Id);
        itemRequest.setLevel3Id(level3Id);
        itemRequest.setLevel3Business(Optional.ofNullable(context.get("business")).map(Object::toString).orElse(""));

        IconResponse iconResponse = new IconResponse();
        String backupKey = String.format("%s-%s-%s", level1Id, level2Id, level3Id);
        return itemRecommendService.recommend(itemRequest, context)
            .map(response -> {
                iconResponse.setItemList(response);
                return iconResponse;
            }).map(TacResult::newResult)
            .map(tacResut -> {
                BizScenario b = BizScenario.valueOf(
                    ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
                    ScenarioConstantApp.LOC_TYPE_B2C,
                    ScenarioConstantApp.ICON_CONTENT_LEVEL2
                );
                return tacResultBackup(tacResut, b, backupKey);
            }).onErrorReturn(throwable -> {
                LOGGER.error("IconLevel1Handler error:{}", JSON.toJSONString(itemRequest), throwable);
                return TacResult.newResult(iconResponse);
            }).defaultIfEmpty(TacResult.newResult(iconResponse))
            .map(tacResult -> {
                BizScenario b = BizScenario.valueOf(
                    ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
                    ScenarioConstantApp.LOC_TYPE_B2C,
                    ScenarioConstantApp.ICON_CONTENT_LEVEL2
                );
                return tacResultBackup(tacResult, b, backupKey);
            });

    }

    private TacResult<IconResponse> tacResultBackup(TacResult<IconResponse> tacResult, BizScenario b, String backupKey) {
        if (tacResult.getData() == null || tacResult.getData() == null || tacResult.getData().getItemList() == null
            || CollectionUtils.isEmpty(tacResult.getData().getItemList().getItemAndContentList())) {

            tacResult = TacResult.errorResult("TacResultBackup");
            HadesLogUtil.stream(b.getUniqueIdentity())
                .kv("key", "tacBackup")
                .kv("tacResultBackup", "true")
                .kv("backupKey", backupKey)
                .info();
        } else {
            HadesLogUtil.stream(b.getUniqueIdentity())
                .kv("key", "tacBackup")
                .kv("tacResultBackup", "false")
                .kv("backupKey", backupKey)
                .info();
        }
        tacResult.getBackupMetaData().setUseBackup(true);
        tacResult.getBackupMetaData().setUseOss(Boolean.FALSE);
        tacResult.getBackupMetaData().setBackupWithParam(true);
        tacResult.getBackupMetaData().setBackupKey(backupKey);
        return tacResult;
    }

}
