package com.tmall.wireless.tac.biz.processor.detail.common.convert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.alibaba.metrics.StringUtils;

import com.google.common.collect.Lists;
import com.tcls.mkt.atmosphere.model.enums.UnifyPriceType;
import com.tcls.mkt.atmosphere.model.response.ItemPromotionResp;
import com.tcls.mkt.atmosphere.model.response.PromotionAtmosphereDTO;
import com.tcls.mkt.atmosphere.model.response.UnifyPriceDTO;
import com.tmall.tcls.gs.sdk.framework.model.ContentVO;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.RecTypeEnum;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecContentResultVO;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecommendContentVO;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecommendItemVO;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecommendVO;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecommendVO.DetailEvent;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecommendVO.DetailLabelVO;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailTextComponentVO;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailTextComponentVO.Style;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.FrontBackMapEnum;
import org.apache.commons.collections4.CollectionUtils;

/**
 * @author: guichen
 * @Data: 2021/9/13
 * @Description:
 */
public abstract class AbstractConverter {

    public abstract RecTypeEnum getRecTypeEnum();

    public abstract DetailRecContentResultVO convert( SgFrameworkResponse sgFrameworkResponse);


    public List<DetailRecommendVO> convertContentResult(String scene, List<ContentVO> itemAndContentList,
        List<String> scmJoin) {

        List<DetailRecommendVO> detailRecommendVOS = new ArrayList<>();
        for (int index = 0; index < itemAndContentList.size(); index++) {
            ContentVO contentVO = itemAndContentList.get(index);

            //基本场景信息
            DetailRecommendContentVO recommendContentVO = convertContent(contentVO, scmJoin);

            //事件
            recommendContentVO.setEvent(getContentEvents(scene, contentVO, index));

            //场景商品信息
            recommendContentVO.setRecommendItemVOS(convertItems(scene, contentVO.get("items"),scmJoin));

            detailRecommendVOS.add(recommendContentVO);

            //scm拼接
            scmJoin.add(contentVO.getString("scm"));
        }
        return detailRecommendVOS;
    }

    private DetailRecommendContentVO convertContent(ContentVO contentVO, List<String> scmJoin) {
        DetailRecommendContentVO recommendContentVO = new DetailRecommendContentVO();
        recommendContentVO.setContentId(contentVO.getLong("contentId"));
        recommendContentVO.setItemSetIds(contentVO.getString("itemSetIds"));

        String shortTile = contentVO.getString(FrontBackMapEnum.contentShortTitle.getFront());
        String title = StringUtils.isNotBlank(shortTile) ? shortTile : contentVO.getString("contentTitle");
        String subTitle = contentVO.getString("contentSubtitle");
        //标题
        recommendContentVO.setTitle(
            Lists.newArrayList(new DetailTextComponentVO(title, new Style("12", "#111111", "true"))));
        //副标题
        recommendContentVO.setSubTitle(
            Lists.newArrayList(new DetailTextComponentVO(subTitle, new Style("12", "#111111", "true"))));

        recommendContentVO.setImg(contentVO.getString(FrontBackMapEnum.contentPic.getFront()));

        contentVO.get("items");

        //scm拼接
        scmJoin.add(contentVO.getString("scm"));

        return recommendContentVO;
    }

    public List<DetailRecommendVO> convertItems(String scene,Object items, List<String> scmJoin) {

        if (Objects.nonNull(items) && items instanceof List) {

            List<DetailRecommendVO> detailRecommendVOS=new ArrayList<>();
            for(int index=0;index<((List<ItemEntityVO>)items).size();index++){
                ItemEntityVO item= ((List<ItemEntityVO>)items).get(index);

                //埋点拼接
                String scm=item.getString("scm");
                scmJoin.add(scm);

                //数据封装
                detailRecommendVOS.add(convertToItem(scene,item,index));


            }

            return detailRecommendVOS;
        }

        return null;
    }

    public DetailRecommendItemVO convertToItem(String scene,ItemEntityVO itemInfoBySourceCaptainDTO,int index) {
        DetailRecommendItemVO detailRecommendItemVO = new DetailRecommendItemVO();

        detailRecommendItemVO.setItemId(itemInfoBySourceCaptainDTO.getLong("itemId"));
        detailRecommendItemVO.setImg(itemInfoBySourceCaptainDTO.getString("itemImg"));
        String title = itemInfoBySourceCaptainDTO.getString("title");
        detailRecommendItemVO.setTitle(
            Lists.newArrayList(new DetailTextComponentVO(title, new Style("12", "#111111", "true"))));

        ItemPromotionResp itemPromotionResp = (ItemPromotionResp)itemInfoBySourceCaptainDTO.get("itemPromotionResp");
        if (Objects.nonNull(itemPromotionResp)) {
            Optional.ofNullable(itemPromotionResp.getUnifyPrice())
                .map(UnifyPriceDTO::getShowPrice)
                .ifPresent(v -> {
                    if (UnifyPriceType.PREDICT_PRICE.equalTo(v.getType())) {
                        detailRecommendItemVO.setPrice(buildPrice(v.getShortText(), v.getPrice()));
                    } else {
                        detailRecommendItemVO.setPrice(buildPrice(null, v.getPrice()));
                    }
                });

            detailRecommendItemVO.setPromotionAtmosphereList(
                convertLabel(detailRecommendItemVO, itemPromotionResp.getAtmosphereList()));
        }

        //事件
        detailRecommendItemVO.setEvent(getItemEvents(scene,itemInfoBySourceCaptainDTO,index));

        return detailRecommendItemVO;
    }

    private List<DetailEvent> getItemEvents(String scene, ItemEntityVO itemEntityVO, int index) {
        return getEvents(scene, itemEntityVO.getItemId(), itemEntityVO.getString("itemUrl"),
            index + 1, itemEntityVO.getString("scm"));
    }

    private static List<DetailLabelVO> convertLabel(DetailRecommendVO detailRecommendVO,
        List<PromotionAtmosphereDTO> atmosphereList) {

        if (CollectionUtils.isEmpty(atmosphereList)) {
            return null;
        }

        List<DetailLabelVO> detailItemLabelVOS = new ArrayList<>(2);

        if (CollectionUtils.isNotEmpty(atmosphereList)) {
            atmosphereList.forEach(atmosphereDTO -> {
                DetailLabelVO detailItemLabelVO = detailRecommendVO.new DetailLabelVO();
                detailItemLabelVO.setTextColor("#A44A02");
                detailItemLabelVO.setText(atmosphereDTO.getText().getContent());
                detailItemLabelVO.setBgColor("#FFDDAC");
                detailItemLabelVO.setCornerRadius("2");
                if (atmosphereDTO.getPromotionType().contains("Coupon")) {
                    detailItemLabelVO.setTitle("券");
                }

                detailItemLabelVOS.add(detailItemLabelVO);
            });
        }

        return detailItemLabelVOS;
    }

    private List<DetailEvent> getContentEvents(String scene, ContentVO contentVO, int index) {
        return getEvents(scene, contentVO.getLong("contentId"),
            contentVO.getString(FrontBackMapEnum.contentCustomLink.getFront()), index + 1,
            contentVO.getString("scm"));
    }

    private static List<DetailEvent> getEvents(String scene, Long id, String jumpUrl, Integer index, String scm) {

        DetailEvent eventView1 = new DetailEvent("openUrl");

        eventView1.addFiledsParam("url", jumpUrl + "&scm=" + scm);

        DetailEvent eventView2 = new DetailEvent("userTrack");

        eventView2.addFiledsParam("page", "Page_Detail");
        eventView2.addFiledsParam("eventId", "2101");
        eventView2.addFiledsParam("arg1", "Page_Detail_Button-" + scene);

        Map<String, String> argsMap = new HashMap(4);
        argsMap.put("spm", "a2141.7631564." + scene + "." + index);
        argsMap.put("refer_id", String.valueOf(id));
        argsMap.put("index", String.valueOf(index));
        argsMap.put("type", scene);
        argsMap.put("scm", scm);

        eventView2.addFiledsParam("args", argsMap);

        return Lists.newArrayList(eventView1, eventView2);
    }

    private static List<DetailTextComponentVO> buildPrice(String shortText, String price) {
        if (com.taobao.usa.util.StringUtils.isNotEmpty(shortText)) {
            shortText = shortText + "￥";
        } else {
            shortText = "￥";
        }
        DetailTextComponentVO prefix = new DetailTextComponentVO(shortText,
            new Style("12", "#FF4027", "false", "false", "#ffffff", "false"));

        Style priceStyle = new Style("14", "#FF4027", "true", "false", "#ffffff", "false");
        priceStyle.setBold("true");
        DetailTextComponentVO text = new DetailTextComponentVO(price, priceStyle);

        return Lists.newArrayList(prefix, text);
    }
}
