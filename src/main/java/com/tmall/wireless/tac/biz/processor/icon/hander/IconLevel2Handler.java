package com.tmall.wireless.tac.biz.processor.icon.hander;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.tmall.aself.shoppingguide.client.cat.model.LabelDTO;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
        level3Request.setLevel1Id(Optional.ofNullable(context.get("iconType")).map(Object::toString).orElse(""));
        level3Request.setLevel2Id(Optional.ofNullable(context.get("level2Id")).map(Object::toString).orElse(""));

//        Map<String, Object> result = Maps.newHashMap();

        IconResponse iconResponse = new IconResponse();

        return level3RecommendService.recommend(level3Request, context).map(level3TabDtoList -> {
//                        LOGGER.info("level3RecommendService.recommend returnObj:{}", JSON.toJSONString(level3TabDtoList));
            iconResponse.setThrirdList(level3TabDtoList);
            return iconResponse;
        }).flatMap(re -> {
                    ItemRequest itemRequest = new ItemRequest();
                    itemRequest.setLevel1Id(level3Request.getLevel1Id());

                    List<LabelDTO> labelDTOS = Optional.of(re).map(IconResponse::getSecondList).orElse(Lists.newArrayList());
                    List<LabelDTO> labelDTOSLevel3 = Optional.of(re).map(IconResponse::getThrirdList).orElse(Lists.newArrayList());

                    itemRequest.setLevel2Id(labelDTOS.stream().findFirst().map(LabelDTO::getId).map(Object::toString).orElse("0"));
                    itemRequest.setLevel3Id(labelDTOSLevel3.stream().findFirst().map(LabelDTO::getId).map(Object::toString).orElse("0"));
                    return itemRecommendService.recommend(itemRequest, context)
                    .map(response -> {
                        iconResponse.setItemList(response);
                        return iconResponse;
                    });
                }).map(TacResult::newResult)
                .onErrorReturn(throwable -> {
                    LOGGER.error("IconLevel1Handler error:{}", JSON.toJSONString(level3Request), throwable);

                    return TacResult.newResult(iconResponse);
                }).defaultIfEmpty(TacResult.newResult(iconResponse));

    }


}
