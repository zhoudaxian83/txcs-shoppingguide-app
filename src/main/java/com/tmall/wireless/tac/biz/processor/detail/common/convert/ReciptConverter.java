package com.tmall.wireless.tac.biz.processor.detail.common.convert;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;

import com.google.common.collect.Lists;
import com.tmall.tcls.gs.sdk.framework.model.ContentVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.wireless.tac.biz.processor.detail.common.config.DetailSwitch;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.RecTypeEnum;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecContentResultVO;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecommendRequest;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailTextComponentVO;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailTextComponentVO.Style;
import com.tmall.wireless.tac.biz.processor.detail.model.config.SizeDTO;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.RenderContentTypeEnum;
import com.tmall.wireless.tac.client.domain.Context;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

/**
 * @author: guichen
 * @Data: 2021/9/13
 * @Description:
 */
@Component
public class ReciptConverter extends AbstractConverter<DetailRecContentResultVO> {

    @Override
    public boolean isAccess(String recType) {
        return RecTypeEnum.RECIPE.getType().equals(recType);
    }


    @Override
    public DetailRecContentResultVO convert(Context context,SgFrameworkResponse sgFrameworkResponse) {
        DetailRecommendRequest recommendRequest=DetailRecommendRequest.getDetailRequest(context);
        return recipeConvert(recommendRequest,sgFrameworkResponse.getItemAndContentList());
    }

    private DetailRecContentResultVO recipeConvert(DetailRecommendRequest recommendRequest,
        List<ContentVO> itemAndContentList) {

        //取大小限制
        SizeDTO sizeDTO = DetailSwitch.requestConfigMap.get(recommendRequest.getRecType()).getSizeDTO();

        DetailRecContentResultVO detailRecContentResultVO=new DetailRecContentResultVO();
        detailRecContentResultVO.setEnableScroll(true);
        detailRecContentResultVO.setShowArrow(true);

        //曝光埋点
        JSONObject exposureExtraParam=new JSONObject();
        List<String> scmJoin=new ArrayList<>();
        exposureExtraParam.put("scmJoin",String.join(",",scmJoin));

        detailRecContentResultVO.setExposureExtraParam(exposureExtraParam);


        List<ContentVO> recipeContents = itemAndContentList.stream()
            .filter(v -> RenderContentTypeEnum.recipeContent.getType().equals(v.getString("contentType")))
            .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(recipeContents) && recipeContents.size() >= sizeDTO.getMin()) {
            //标题名称
            detailRecContentResultVO.setTitle(
                Lists.newArrayList(new DetailTextComponentVO("菜谱推荐", new Style("12", "#111111", "true"))));
            //推荐内容
            detailRecContentResultVO.setResult(
                super.convertContentResult(recommendRequest, recipeContents.subList(0, Math.min(sizeDTO.getMax(), recipeContents.size())),
                    scmJoin));
            exposureExtraParam.put("scmJoin",String.join(",",scmJoin));
            return detailRecContentResultVO;
        }

        itemAndContentList.removeAll(recipeContents);
        if (CollectionUtils.isNotEmpty(itemAndContentList) && itemAndContentList.size() >= sizeDTO.getMin()) {
            //标题名称
            detailRecContentResultVO.setTitle(
                Lists.newArrayList(new DetailTextComponentVO("为你推荐", new Style("12", "#111111", "true"))));
            //推荐内容
            detailRecContentResultVO.setResult(super
                .convertContentResult(recommendRequest, itemAndContentList.subList(0, Math.min(sizeDTO.getMax(), itemAndContentList.size())),
                    scmJoin));
            exposureExtraParam.put("scmJoin",String.join(",",scmJoin));
            return detailRecContentResultVO;
        }

        return null;
    }
}
