package com.tmall.wireless.tac.biz.processor.detail.common.convert;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

import com.google.common.collect.Lists;
import com.tmall.tcls.gs.sdk.framework.model.ContentVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.wireless.tac.biz.processor.detail.common.config.DetailSwitch;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.RecTypeEnum;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecContentResultVO;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecommendContentVO;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailTextComponentVO;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailTextComponentVO.Style;
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
    public RecTypeEnum getRecTypeEnum() {
        return RecTypeEnum.SIMILAR_ITEM_CONTENT;
    }

    @Override
    public DetailRecContentResultVO convert(SgFrameworkResponse sgFrameworkResponse) {

        return similarItemConvert(sgFrameworkResponse.getItemAndContentList());
    }

    private DetailRecContentResultVO similarItemConvert(List<ContentVO> itemAndContentList) {
        String scene=getRecTypeEnum().getType();

        DetailRecContentResultVO detailRecContentResultVO=new DetailRecContentResultVO();
        detailRecContentResultVO.setEnableScroll(true);
        detailRecContentResultVO.setShowArrow(true);

        //曝光埋点
        JSONObject exposureExtraParam=new JSONObject();
        List<String> scmJoin=new ArrayList<>();
        exposureExtraParam.put("scmJoin",String.join(",",scmJoin));
        detailRecContentResultVO.setExposureExtraParam(exposureExtraParam);

        detailRecContentResultVO.setResult(new ArrayList<>(3));

        //默认写入第一个tab是相似商品，默认contentId=-1
        DetailRecommendContentVO contentVO = new DetailRecommendContentVO();
        contentVO.setContentId(-1L);
        contentVO.setTitle(Lists.newArrayList(new DetailTextComponentVO("相似商品", new Style("12", "#111111", "true"))));
        detailRecContentResultVO.getResult().add(contentVO);

        if (CollectionUtils.isNotEmpty(itemAndContentList)) {
            //推荐内容
            detailRecContentResultVO.getResult().addAll(super
                .convertContentResult(scene, itemAndContentList.subList(0,
                    Math.min(DetailSwitch.contentSizeMap.get(getRecTypeEnum().getType()).getMax()
                    , itemAndContentList.size())),
                    scmJoin));
        }

        return detailRecContentResultVO;
    }
}