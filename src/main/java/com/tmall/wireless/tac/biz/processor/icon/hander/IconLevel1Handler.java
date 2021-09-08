package com.tmall.wireless.tac.biz.processor.icon.hander;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.tmall.aself.shoppingguide.client.cat.model.LabelDTO;
import com.tmall.tcls.gs.sdk.framework.model.ContentVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.wireless.tac.biz.processor.icon.level2.Level2RecommendService;
import com.tmall.wireless.tac.biz.processor.icon.level2.Level2Request;
import com.tmall.wireless.tac.biz.processor.icon.level3.Level3RecommendService;
import com.tmall.wireless.tac.biz.processor.icon.level3.Level3Request;
import com.tmall.wireless.tac.biz.processor.todaycrazy.LimitTimeBuyScene;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class IconLevel1Handler extends RpmReactiveHandler<Map<String, Object>> {

    Logger LOGGER = LoggerFactory.getLogger(IconLevel1Handler.class);

    @Autowired
    Level2RecommendService level2RecommendService;
    @Autowired
    Level3RecommendService level3RecommendService;
    @Override
    public Flowable<TacResult<Map<String, Object>>> executeFlowable(Context context) throws Exception {


        Level2Request level2Request = new Level2Request();
        level2Request.setLevel1Id(Optional.ofNullable(context.get("iconType")).map(Object::toString).orElse(""));

        Map<String, Object> result = Maps.newHashMap();

        return level2RecommendService.recommend(level2Request, context)
                .flatMap(level2TabDtoList -> {

                    // todo 如果level2TabDtoList为空直接返回走打底

                    result.put("secondList", level2TabDtoList);
                    Level3Request level3Request = new Level3Request();
                    level3Request.setLevel1Id(level2Request.getLevel1Id());
                    level3Request.setLevel2Id(level2TabDtoList.stream().findFirst().map(LabelDTO::getId).map(Objects::toString).orElse(""));

                    return level3RecommendService.recommend(level3Request, context).map(level3TabDtoList -> {
                        LOGGER.info("level3RecommendService.recommend returnObj:{}", JSON.toJSONString(level3TabDtoList));
                        result.put("thrirdList", level3TabDtoList);
                        return result;
                    }).map(TacResult::newResult);

                }).onErrorReturn(throwable -> {
                    LOGGER.error("IconLevel1Handler error:{}", JSON.toJSONString(level2Request), throwable);
                    result.put("errorMsg", throwable.getMessage());
                    return TacResult.newResult(result);
                }).defaultIfEmpty(TacResult.newResult(result));

    }
}
