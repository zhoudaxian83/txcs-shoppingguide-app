package com.tmall.wireless.tac.biz.processor.detail.common.convert;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;

import com.google.common.collect.Lists;
import com.tmall.tcls.gs.sdk.framework.model.ContentVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.RecTypeEnum;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecContentResultVO;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailTextComponentVO;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailTextComponentVO.Style;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.RenderContentTypeEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

/**
 * @author: guichen
 * @Data: 2021/9/13
 * @Description:
 */
@Component
public class ReciptConverter extends AbstractConverter {

    @Override
    public RecTypeEnum getRecTypeEnum() {
            return RecTypeEnum.RECIPE;
    }

    @Override
    public DetailRecContentResultVO convert(SgFrameworkResponse sgFrameworkResponse) {

        return recipeConvert(sgFrameworkResponse.getItemAndContentList());
    }

    private DetailRecContentResultVO recipeConvert(List<ContentVO> itemAndContentList) {
        String scene=getRecTypeEnum().getType();

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

        if (CollectionUtils.isNotEmpty(recipeContents) && recipeContents.size() > 2) {
            //标题名称
            detailRecContentResultVO.setTitle(
                Lists.newArrayList(new DetailTextComponentVO("菜谱推荐", new Style("12", "#111111", "true"))));
            //推荐内容
            detailRecContentResultVO.setResult(
                super.convertContentResult(scene, recipeContents.subList(0, Math.min(6, recipeContents.size())),
                    scmJoin));
            return detailRecContentResultVO;
        }

        itemAndContentList.removeAll(recipeContents);
        if (CollectionUtils.isNotEmpty(itemAndContentList) && recipeContents.size() > 2) {
            //标题名称
            detailRecContentResultVO.setTitle(
                Lists.newArrayList(new DetailTextComponentVO("为你推荐", new Style("12", "#111111", "true"))));
            //推荐内容
            detailRecContentResultVO.setResult(super
                .convertContentResult(scene, itemAndContentList.subList(0, Math.min(6, itemAndContentList.size())),
                    scmJoin));
            return detailRecContentResultVO;
        }

        return null;
    }
}
