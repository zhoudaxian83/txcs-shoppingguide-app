package com.tmall.wireless.tac.biz.processor.detail.common.convert;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.RecTypeEnum;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecContentResultVO;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecItemResultVO;

/**
 * @author: guichen
 * @Data: 2021/9/14
 * @Description:
 */
public class SimilarItemItemConverter extends AbstractConverter<DetailRecItemResultVO> {
    @Override
    public RecTypeEnum getRecTypeEnum() {
        return RecTypeEnum.SIMILAR_ITEM_ITEM;
    }

    @Override
    public DetailRecItemResultVO convert(SgFrameworkResponse sgFrameworkResponse) {

        DetailRecItemResultVO detailRecItemResultVO=new DetailRecItemResultVO();

        detailRecItemResultVO.setEnableScroll(false);
        detailRecItemResultVO.setShowArrow(false);

        //曝光埋点
        JSONObject exposureExtraParam=new JSONObject();
        List<String> scmJoin=new ArrayList<>();
        exposureExtraParam.put("scmJoin",String.join(",",scmJoin));
        detailRecItemResultVO.setExposureExtraParam(exposureExtraParam);

        //标题名称,无标题名称
        detailRecItemResultVO.setTitle(null);

        //推荐内容
        detailRecItemResultVO.setResult(super.convertItems(RecTypeEnum.SIMILAR_ITEM_ITEM.getType(),
            sgFrameworkResponse.getItemAndContentList(), scmJoin));

        return detailRecItemResultVO;
    }
}
