package com.tmall.wireless.tac.biz.processor.icon.hander;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.tmall.aself.shoppingguide.client.cat.model.LabelDTO;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.icon.item.ItemRecommendService;
import com.tmall.wireless.tac.biz.processor.icon.item.ItemRequest;
import com.tmall.wireless.tac.biz.processor.icon.level2.Level2RecommendService;
import com.tmall.wireless.tac.biz.processor.icon.level2.Level2Request;
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
import java.util.Objects;
import java.util.Optional;

import static com.tmall.wireless.tac.biz.processor.icon.level3.ext.IconLevel3ContentInfoQuerySdkExtPt.ICON_BANNER_KEY;

@Service
public class IconLevel2Handler extends RpmReactiveHandler<IconResponse> {

    Logger LOGGER = LoggerFactory.getLogger(IconLevel2Handler.class);

    @Autowired
    Level2RecommendService level2RecommendService;
    @Autowired
    Level3RecommendService level3RecommendService;
    @Autowired
    ItemRecommendService itemRecommendService;

    @Override
    public Flowable<TacResult<IconResponse>> executeFlowable(Context context) throws Exception {


        Level3Request level3Request = new Level3Request();
        String level2Id = Optional.ofNullable( context.get("level2Id")).map(Object::toString).orElse("");
        String level1Id = Optional.ofNullable(context.get("iconType")).map(Object::toString).orElse("");
        String businessType = Optional.ofNullable(context.get("business")).map(Object::toString).orElse("");

        level3Request.setLevel1Id(level1Id);
        level3Request.setLevel2Id(level2Id);
        level3Request.setLevel2Business(businessType);

//        Map<String, Object> result = Maps.newHashMap();

        IconResponse iconResponse = new IconResponse();
        String backupKey = String.format("%s-%s", level1Id, level2Id);

        return level3RecommendService.recommend(level3Request, context).map(level3TabDtoList -> {
//                        LOGGER.info("level3RecommendService.recommend returnObj:{}", JSON.toJSONString(level3TabDtoList));
            iconResponse.setThrirdList(level3TabDtoList);
            Object banner = Optional.ofNullable(context.getParams()).map(v ->v.get(ICON_BANNER_KEY)).orElse(null);
            if (banner != null) {
                iconResponse.setBanner(banner);
            }
            return iconResponse;
        }).flatMap(re -> {
                    ItemRequest itemRequest = new ItemRequest();
                    itemRequest.setLevel1Id(level3Request.getLevel1Id());
                    List<LabelDTO> labelDTOSLevel3 = Optional.of(re).map(IconResponse::getThrirdList).orElse(Lists.newArrayList());

                    itemRequest.setLevel2Id(level3Request.getLevel2Id());
                    itemRequest.setLevel3Id(labelDTOSLevel3.stream().findFirst().map(LabelDTO::getId).map(Object::toString).orElse("0"));
                    itemRequest.setLevel3Business(labelDTOSLevel3.stream().findFirst().map(LabelDTO::getBusiness).map(Object::toString).orElse("0"));

                    return itemRecommendService.recommend(itemRequest, context)
                    .map(response -> {
                        iconResponse.setItemList(response);
                        return iconResponse;
                    });
                }).map(TacResult::newResult)
                .map(tacResult -> {
                    BizScenario b = BizScenario.valueOf(
                        ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
                        ScenarioConstantApp.LOC_TYPE_B2C,
                        ScenarioConstantApp.ICON_CONTENT_LEVEL3
                    );
                    return tacResultBackup(tacResult,b, backupKey);
                }).onErrorReturn(throwable -> {
                    LOGGER.error("IconLevel1Handler error:{}", JSON.toJSONString(level3Request), throwable);

                    return TacResult.newResult(iconResponse);
                }).defaultIfEmpty(TacResult.newResult(iconResponse))
                .map(tacResult -> {
                    BizScenario b = BizScenario.valueOf(
                        ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
                        ScenarioConstantApp.LOC_TYPE_B2C,
                        ScenarioConstantApp.ICON_CONTENT_LEVEL3
                    );
                    return tacResultBackup(tacResult,b, backupKey);
                });

    }
    private TacResult<IconResponse> tacResultBackup(TacResult<IconResponse> tacResult, BizScenario b, String backupKey){

        if (tacResult.getData() == null || tacResult.getData() == null || tacResult.getData().getItemList() == null
            || CollectionUtils.isEmpty(tacResult.getData().getItemList().getItemAndContentList())
            || CollectionUtils.isEmpty(tacResult.getData().getThrirdList())) {
            tacResult = TacResult.errorResult("TacResultBackup");

            HadesLogUtil.stream(b.getUniqueIdentity())
                .kv("key","tacBackup")
                .kv("tacResultBackup", "true")
                .info();
        } else {
            HadesLogUtil.stream(b.getUniqueIdentity())
                .kv("key","tacBackup")
                .kv("tacResultBackup", "false")
                .info();
        }
        tacResult.getBackupMetaData().setUseBackup(true);
        tacResult.getBackupMetaData().setBackupWithParam(true);
        tacResult.getBackupMetaData().setUseOss(Boolean.FALSE);
        tacResult.getBackupMetaData().setBackupKey(backupKey);
        return tacResult;
    }

}
