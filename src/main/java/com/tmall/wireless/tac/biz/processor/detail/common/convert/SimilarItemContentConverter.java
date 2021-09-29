package com.tmall.wireless.tac.biz.processor.detail.common.convert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.tcls.gs.sdk.framework.model.ContentVO;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.wireless.tac.biz.processor.detail.common.config.DetailSwitch;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.RecTypeEnum;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecContentResultVO;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecommendRequest;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecommendVO.DetailEvent;
import com.tmall.wireless.tac.biz.processor.detail.util.CommonUtil;
import com.tmall.wireless.tac.client.domain.Context;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

/**
 * @author: guichen
 * @Data: 2021/9/14
 * @Description:
 */
@Component
public class SimilarItemContentConverter extends AbstractConverter<DetailRecContentResultVO> {

    @Override
    public boolean isAccess(String recType) {
        return RecTypeEnum.SIMILAR_ITEM_CONTENT.getType().equals(recType);
    }

    @Override
    public DetailRecContentResultVO convert(Context context,SgFrameworkResponse sgFrameworkResponse) {
        DetailRecommendRequest recommendRequest=DetailRecommendRequest.getDetailRequest(context);
        return similarItemConvert(recommendRequest,sgFrameworkResponse.getItemAndContentList());
    }

    private DetailRecContentResultVO similarItemConvert(DetailRecommendRequest recommendRequest,
        List<ContentVO> itemAndContentList) {

        DetailRecContentResultVO detailRecContentResultVO=new DetailRecContentResultVO();
        detailRecContentResultVO.setEnableScroll(false);
        detailRecContentResultVO.setShowArrow(false);

        //曝光埋点
        JSONObject exposureExtraParam=new JSONObject();
        List<String> scmJoin=new ArrayList<>();
        detailRecContentResultVO.setExposureExtraParam(exposureExtraParam);

        detailRecContentResultVO.setResult(new ArrayList<>(3));

        if (CollectionUtils.isEmpty(itemAndContentList)) {
            itemAndContentList = new ArrayList<>();
        }

        //默认写入第一个tab是相似商品，默认contentId=-1
        ContentVO contentVO = new ContentVO();
        contentVO.put("contentId", -1L);
        contentVO.put("contentTitle", "相似商品");
        itemAndContentList.add(0, contentVO);

        //推荐内容
        detailRecContentResultVO.getResult().addAll(super
            .convertContentResult(recommendRequest, itemAndContentList.subList(0,
                Math.min(DetailSwitch.requestConfigMap.get(recommendRequest.getRecType()).getSizeDTO().getMax()
                    , itemAndContentList.size())),
                scmJoin));


        exposureExtraParam.put("scmJoin",String.join(",",scmJoin));
        return detailRecContentResultVO;
    }


    @Override
    public List<DetailEvent> getContentEvents(DetailRecommendRequest recommendRequest, ContentVO contentVO, int index) {

        DetailEvent userTrackEvent = getUserTrackEvent(recommendRequest.getRecType(),
            contentVO.getLong("contentId"), index, contentVO.getString("scm"));

        if (CommonUtil.validId(contentVO.getLong("contentId"))) {
            DetailEvent clickEvent = getClickEvent(recommendRequest, contentVO);

            return Lists.newArrayList(userTrackEvent, clickEvent);
        }

        return Lists.newArrayList(userTrackEvent);
    }

    private DetailEvent getClickEvent(DetailRecommendRequest recommendRequest,ContentVO contentVO){
        DetailEvent eventView1 = new DetailEvent("similarContentAction");

        Map<String, Object> paramsMap = Maps.newHashMap();
        eventView1.addFieldsParam("msCode","2021091404");
        eventView1.addFieldsParam("appName","taodetail");

        Arrays.stream(DetailRecommendRequest.class.getDeclaredFields())
            .forEach(field -> {
                try {
                    field.setAccessible(true);
                    paramsMap.put(field.getName(), field.get(recommendRequest));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });

        paramsMap.put("recType",RecTypeEnum.SIMILAR_ITEM_CONTENT_ITEM.getType());
        paramsMap.put("contentId",contentVO.getLong("contentId"));
        paramsMap.put("itemSetIds",contentVO.getString("itemSetIds"));
        paramsMap.put("pageSize",6);


        eventView1.addFieldsParam("params", JSON.toJSONString(paramsMap));

        return eventView1;
    }

}