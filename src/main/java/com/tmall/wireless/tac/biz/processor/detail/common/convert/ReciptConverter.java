package com.tmall.wireless.tac.biz.processor.detail.common.convert;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;

import com.google.common.collect.Lists;
import com.tmall.tcls.gs.sdk.framework.model.ContentVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.wireless.tac.biz.processor.detail.common.config.DetailSwitch;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.RecTypeEnum;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecContentResultVO;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecommendContentVO;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecommendRequest;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecommendVO.DetailEvent;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailTextComponentVO;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailTextComponentVO.Style;
import com.tmall.wireless.tac.biz.processor.detail.model.config.DetailRequestConfig;
import com.tmall.wireless.tac.biz.processor.detail.model.config.SizeDTO;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.FrontBackMapEnum;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.RenderContentTypeEnum;
import com.tmall.wireless.tac.client.domain.Context;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
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

        //???????????????
        SizeDTO sizeDTO = DetailRequestConfig.parse(recommendRequest.getRecType()).getSizeDTO();

        DetailRecContentResultVO detailRecContentResultVO=new DetailRecContentResultVO();
        detailRecContentResultVO.setEnableScroll(true);
        detailRecContentResultVO.setShowArrow(true);

        //????????????
        JSONObject exposureExtraParam=new JSONObject();
        List<String> scmJoin=new ArrayList<>();
        exposureExtraParam.put("scmJoin",String.join(",",scmJoin));

        detailRecContentResultVO.setExposureExtraParam(exposureExtraParam);


        List<ContentVO> recipeContents = itemAndContentList.stream()
            .filter(v -> RenderContentTypeEnum.recipeContent.getType().equals(v.getString("contentType")))
            .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(recipeContents) && recipeContents.size() >= sizeDTO.getMin()) {
            //????????????
            detailRecContentResultVO.setTitle(
                Lists.newArrayList(new DetailTextComponentVO("????????????", new Style("12", "#111111", "true"))));
            //????????????
            detailRecContentResultVO.setResult(
                super.convertContentResult(recommendRequest, recipeContents.subList(0, Math.min(sizeDTO.getMax(), recipeContents.size())),
                    scmJoin));
            exposureExtraParam.put("scmJoin",String.join(",",scmJoin));
            return detailRecContentResultVO;
        }

        itemAndContentList.removeAll(recipeContents);
        if (DetailSwitch.enableReciptCommonContent && CollectionUtils.isNotEmpty(itemAndContentList)
            && itemAndContentList.size() >= sizeDTO.getMin()) {
            //????????????
            detailRecContentResultVO.setTitle(
                Lists.newArrayList(new DetailTextComponentVO("????????????", new Style("12", "#111111", "true"))));
            //????????????
            detailRecContentResultVO.setResult(super
                .convertContentResult(recommendRequest, itemAndContentList.subList(0, Math.min(sizeDTO.getMax(), itemAndContentList.size())),
                    scmJoin));
            exposureExtraParam.put("scmJoin",String.join(",",scmJoin));
            return detailRecContentResultVO;
        }

        return null;
    }

    @Override
    public List<DetailEvent> getContentEvents(DetailRecommendRequest recommendRequest, ContentVO contentVO, int index) {
        String jumpUrl = contentVO.getString(FrontBackMapEnum.contentCustomLink.getFront());
        if (StringUtils.isEmpty(jumpUrl)) {
            jumpUrl = new StringBuilder(DetailSwitch.reciptCommonContentJumpUrl)
                .append("&contentType=")
                .append(contentVO.getString("contentType"))
                .append("&itemSetIds=")
                .append(contentVO.getString("itemSetIds")).toString();
        }
        return super.getEvents(recommendRequest.getRecType(), contentVO.getLong("contentId"), jumpUrl
            , index + 1,
            contentVO.getString("scm"));
    }
}
