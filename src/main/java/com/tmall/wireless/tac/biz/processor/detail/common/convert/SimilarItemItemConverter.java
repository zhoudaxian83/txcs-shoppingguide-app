package com.tmall.wireless.tac.biz.processor.detail.common.convert;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.alibaba.fastjson.JSONObject;

import com.google.common.collect.Lists;
import com.taobao.igraph.client.model.AtomicQuery;
import com.taobao.igraph.client.model.KeyList;
import com.taobao.igraph.client.model.MatchRecord;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tmallwireless.tac.spi.context.SPIResult;
import com.tmall.wireless.store.spi.third.IGraphSpi;
import com.tmall.wireless.tac.biz.processor.detail.common.config.DetailSwitch;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.RecTypeEnum;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecItemResultVO;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecommendItemVO;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailTextComponentVO;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailTextComponentVO.Style;
import com.tmall.wireless.tac.biz.processor.detail.model.config.SizeDTO;
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

    @Resource
    private IGraphSpi iGraphSpi;

    @Override
    public RecTypeEnum getRecTypeEnum() {
        return RecTypeEnum.SIMILAR_ITEM_ITEM;
    }

    @Override
    public DetailRecItemResultVO convert(Context context,SgFrameworkResponse sgFrameworkResponse) {

        //取大小限制
        SizeDTO sizeDTO = DetailSwitch.requestConfigMap.get(getRecTypeEnum().getType()).getSizeDTO();

        //开始构建
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
        if (CollectionUtils.isEmpty(sgFrameworkResponse.getItemAndContentList()) ||
            sgFrameworkResponse.getItemAndContentList().size() < sizeDTO.getMin()) {
            return null;
        }

        //推荐内容
        List itemAndContentList = sgFrameworkResponse.getItemAndContentList();
        List list = itemAndContentList.subList(0, sizeDTO.getMax());
        detailRecItemResultVO.setResult(super.convertItems(RecTypeEnum.SIMILAR_ITEM_ITEM.getType(),list, scmJoin));

        //卖点的拼装
        processSellingPoint(detailRecItemResultVO.getResult());

        exposureExtraParam.put("scmJoin",String.join(",",scmJoin));

        return detailRecItemResultVO;
    }

    private void processSellingPoint(List<DetailRecommendItemVO> recommendItemVOS){

        List<KeyList> collect = recommendItemVOS.stream().filter(
            v -> CollectionUtils.isEmpty(v.getPromotionAtmosphereList()))
            .map(v -> {
                return new KeyList(String.valueOf(v.getItemId()));
            })
            .collect(Collectors.toList());

        AtomicQuery atomicQuery = new AtomicQuery( "aws_ascp_apl_tmcs_item_stat_element1",collect);

        SPIResult<List<MatchRecord>> search = iGraphSpi.search(atomicQuery);
        if(!search.isSuccess()||CollectionUtils.isEmpty(search.getData())){
            return;
        }

        search.getData().forEach(v -> {
            recommendItemVOS.stream().filter(item -> item.getItemId().equals(v.getLong("item_id")))
                .findFirst()
                .ifPresent(itemVO -> itemVO
                    .setSubTitle(
                        Lists.newArrayList(
                            new DetailTextComponentVO(v.getString("element_content"),
                                new Style("12", "#111111", "true")))));
        });
    }
}
