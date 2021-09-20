package com.tmall.wireless.tac.biz.processor.detail.common.convert;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

import com.google.common.collect.Lists;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.RecTypeEnum;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecContentResultVO;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecItemResultVO;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecommendItemVO;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailTextComponentVO;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailTextComponentVO.Style;
import com.tmall.wireless.tac.client.domain.Context;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

/**
 * @author: guichen
 * @Data: 2021/9/14
 * @Description:
 */
@Component
public class SimilarItemItemConverter extends AbstractConverter<DetailRecItemResultVO> {
    @Override
    public RecTypeEnum getRecTypeEnum() {
        return RecTypeEnum.SIMILAR_ITEM_ITEM;
    }

    @Override
    public DetailRecItemResultVO convert(Context context,SgFrameworkResponse sgFrameworkResponse) {

        DetailRecItemResultVO detailRecItemResultVO=new DetailRecItemResultVO();

        detailRecItemResultVO.setEnableScroll(false);
        detailRecItemResultVO.setShowArrow(false);

        //曝光埋点
        JSONObject exposureExtraParam=new JSONObject();
        List<String> scmJoin=new ArrayList<>();
        detailRecItemResultVO.setExposureExtraParam(exposureExtraParam);

        //标题名称,无标题名称
        detailRecItemResultVO.setTitle(null);

        //如果没有推荐结果就空返回
        if(CollectionUtils.isEmpty(sgFrameworkResponse.getItemAndContentList())){
            return detailRecItemResultVO;
        }

        //推荐内容
        List itemAndContentList = sgFrameworkResponse.getItemAndContentList();
        List list = itemAndContentList.subList(0, Math.min(6, itemAndContentList.size()));
        detailRecItemResultVO.setResult(super.convertItems(RecTypeEnum.SIMILAR_ITEM_ITEM.getType(),list, scmJoin));

        exposureExtraParam.put("scmJoin",String.join(",",scmJoin));

        return detailRecItemResultVO;
    }

    @Override
    public DetailRecommendItemVO convertToItem(String scene, ItemEntityVO itemInfoBySourceCaptainDTO, int index) {
        DetailRecommendItemVO detailRecommendItemVO = super.convertToItem(scene, itemInfoBySourceCaptainDTO, index);

        if (CollectionUtils.isNotEmpty(detailRecommendItemVO.getPromotionAtmosphereList()) &&
            (detailRecommendItemVO.getPromotionAtmosphereList().size() > 1)) {
            String sellPointMock = "销量排名第一";
            detailRecommendItemVO.setSubTitle(
                Lists.newArrayList(new DetailTextComponentVO(sellPointMock, new Style("12", "#111111", "true"))));
        } else {
            if (CollectionUtils.isNotEmpty(detailRecommendItemVO.getPromotionAtmosphereList()) &&detailRecommendItemVO.getPromotionAtmosphereList().size() == 1 &&
                detailRecommendItemVO.getPromotionAtmosphereList().get(1).getText().contains("满")) {
                detailRecommendItemVO.getPromotionAtmosphereList().get(1)
                    .setTitle("券");
            }
        }

        return detailRecommendItemVO;
    }
}
