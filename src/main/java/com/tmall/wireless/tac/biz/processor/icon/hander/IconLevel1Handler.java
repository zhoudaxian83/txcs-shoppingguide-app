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
public class IconLevel1Handler extends RpmReactiveHandler<IconResponse> {

    Logger LOGGER = LoggerFactory.getLogger(IconLevel1Handler.class);

    @Autowired
    Level2RecommendService level2RecommendService;
    @Autowired
    Level3RecommendService level3RecommendService;
    @Autowired
    ItemRecommendService itemRecommendService;

    @Override
    public Flowable<TacResult<IconResponse>> executeFlowable(Context context) throws Exception {



        Level2Request level2Request = new Level2Request();
        level2Request.setLevel1Id(Optional.ofNullable(context.get("iconType")).map(Object::toString).orElse(""));

//        Map<String, Object> result = Maps.newHashMap();

        IconResponse iconResponse = new IconResponse();

        return level2RecommendService.recommend(level2Request, context)
                .flatMap(level2TabDtoList -> {
                    // todo 如果level2TabDtoList为空直接返回走打底

                    iconResponse.setSecondList(level2TabDtoList);
                    Level3Request level3Request = new Level3Request();
                    level3Request.setLevel1Id(level2Request.getLevel1Id());
                    level3Request.setLevel2Id(level2TabDtoList.stream().findFirst().map(LabelDTO::getId).map(Objects::toString).orElse(""));
                    level3Request.setLevel2Business(level2TabDtoList.stream().findFirst().map(LabelDTO::getBusiness).map(Objects::toString).orElse(""));

                    return level3RecommendService.recommend(level3Request, context).map(level3TabDtoList -> {
//                        LOGGER.info("level3RecommendService.recommend returnObj:{}", JSON.toJSONString(level3TabDtoList));
                        iconResponse.setThrirdList(level3TabDtoList);
                        Object banner = Optional.ofNullable(context.getParams()).map(v ->v.get(ICON_BANNER_KEY)).orElse(null);
                        if (banner != null) {
                            iconResponse.setBanner(banner);
                        }
                        return iconResponse;
                    }).onErrorReturn(throwable -> {
                                LOGGER.error("level3RecommendService.recommend error", throwable);
                                return iconResponse;
                            });

                }).flatMap(re -> {
                    ItemRequest itemRequest = new ItemRequest();
                    itemRequest.setLevel1Id(level2Request.getLevel1Id());

                    List<LabelDTO> labelDTOS = Optional.of(re).map(IconResponse::getSecondList).orElse(Lists.newArrayList());
                    List<LabelDTO> labelDTOSLevel3 = Optional.of(re).map(IconResponse::getThrirdList).orElse(Lists.newArrayList());

                    itemRequest.setLevel2Id(labelDTOS.stream().findFirst().map(LabelDTO::getId).map(Object::toString).orElse("0"));
                    itemRequest.setLevel3Id(labelDTOSLevel3.stream().findFirst().map(LabelDTO::getId).map(Object::toString).orElse("0"));
                    itemRequest.setLevel3Business(labelDTOSLevel3.stream().findFirst().map(LabelDTO::getBusiness).orElse("0"));
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
                        ScenarioConstantApp.ICON_CONTENT_LEVEL2
                    );
                    return tacResultBackup(tacResult,b, level2Request.getLevel1Id());
                    }).onErrorReturn(throwable -> {
                    LOGGER.error("IconLevel1Handler error:{}", JSON.toJSONString(level2Request), throwable);

                    return TacResult.newResult(iconResponse);
                }).defaultIfEmpty(TacResult.newResult(iconResponse))
                .map(tacResult -> {
                    BizScenario b = BizScenario.valueOf(
                        ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
                        ScenarioConstantApp.LOC_TYPE_B2C,
                        ScenarioConstantApp.ICON_CONTENT_LEVEL2
                    );
                    return tacResultBackup(tacResult,b, level2Request.getLevel1Id());
                });

    }
    private TacResult<IconResponse> tacResultBackup(TacResult<IconResponse> tacResult, BizScenario b, String levelId){
        if (tacResult.getData() == null || tacResult.getData() == null || tacResult.getData().getItemList() == null
            || CollectionUtils.isEmpty(tacResult.getData().getItemList().getItemAndContentList())
            || CollectionUtils.isEmpty(tacResult.getData().getSecondList())
            || CollectionUtils.isEmpty(tacResult.getData().getThrirdList())) {

            tacResult = TacResult.errorResult("TacResultBackup");
            HadesLogUtil.stream(b.getUniqueIdentity())
                .kv("key","tacBackup")
                .kv("tacResultBackup", "true")
                .kv("levelId", levelId)
                .info();
        } else {
            HadesLogUtil.stream(b.getUniqueIdentity())
                .kv("key","tacBackup")
                .kv("tacResultBackup", "false")
                .kv("levelId", levelId)
                .info();
        }
        tacResult.getBackupMetaData().setUseBackup(true);
        tacResult.getBackupMetaData().setBackupWithParam(true);
        tacResult.getBackupMetaData().setUseOss(Boolean.FALSE);
        tacResult.getBackupMetaData().setBackupKey(levelId);
        return tacResult;
    }

}
