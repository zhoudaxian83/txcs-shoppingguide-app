package com.tmall.wireless.tac.biz.processor.detail.common.convert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


import com.google.common.collect.Lists;
import com.tcls.mkt.atmosphere.model.enums.UnifyPriceType;
import com.tcls.mkt.atmosphere.model.response.ItemPromotionResp;
import com.tcls.mkt.atmosphere.model.response.PromotionAtmosphereDTO;
import com.tcls.mkt.atmosphere.model.response.UnifyPriceDTO;
import com.tmall.tcls.gs.sdk.framework.model.ContentVO;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.wireless.tac.biz.processor.detail.common.config.DetailSwitch;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.RecTypeEnum;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecommendContentVO;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecommendItemVO;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecommendRequest;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecommendVO;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecommendVO.DetailEvent;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecommendVO.DetailLabelVO;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailTextComponentVO;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailTextComponentVO.Style;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.FrontBackMapEnum;
import com.tmall.wireless.tac.client.domain.Context;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @author: guichen
 * @Data: 2021/9/13
 * @Description:
 */
public abstract class AbstractConverter<T> {

    public abstract boolean isAccess(String recType);

    public abstract T convert(Context context, SgFrameworkResponse sgFrameworkResponse);

    public String getRecType(Context context){
        DetailRecommendRequest recommendRequest=DetailRecommendRequest.getDetailRequest(context);
        return recommendRequest.getRecType();
    }

    public List<DetailRecommendContentVO> convertContentResult(DetailRecommendRequest recommendRequest, List<ContentVO> itemAndContentList,
        List<String> scmJoin) {

        List<DetailRecommendContentVO> detailRecommendVOS = new ArrayList<>();
        for (int index = 0; index < itemAndContentList.size(); index++) {
            ContentVO contentVO = itemAndContentList.get(index);

            //scm拼接
            Optional.ofNullable(contentVO.getString("scm")).ifPresent(scmJoin::add);

            //基本场景信息
            DetailRecommendContentVO recommendContentVO = convertContent(contentVO);

            //事件
            recommendContentVO.setEvent(getContentEvents(recommendRequest, contentVO, index));

            //场景商品信息
            recommendContentVO.setRecommendItemVOS(convertItems(recommendRequest.getRecType(), contentVO.get("items"),scmJoin));

            detailRecommendVOS.add(recommendContentVO);
        }
        return detailRecommendVOS;
    }

    private DetailRecommendContentVO convertContent(ContentVO contentVO) {
        DetailRecommendContentVO recommendContentVO = new DetailRecommendContentVO();
        recommendContentVO.setContentId(contentVO.getLong("contentId"));
        recommendContentVO.setItemSetIds(contentVO.getString("itemSetIds"));

        String shortTile = contentVO.getString(FrontBackMapEnum.contentShortTitle.getFront());
        String title = StringUtils.isNotBlank(shortTile) ? shortTile : contentVO.getString("contentTitle");
        //标题
        recommendContentVO.setTitle(
            Lists.newArrayList(new DetailTextComponentVO(title, new Style("12", "#111111", "true"))));

        //副标题
        Optional.ofNullable(contentVO.getString("contentSubtitle"))
        .ifPresent(subTitle->{
            recommendContentVO.setSubTitle(
                Lists.newArrayList(new DetailTextComponentVO(subTitle, new Style("12", "#111111", "true"))));

        });

        recommendContentVO.setImg(contentVO.getString(FrontBackMapEnum.contentPic.getFront()));

        return recommendContentVO;
    }

    public List<DetailRecommendItemVO> convertItems(String scene,Object items, List<String> scmJoin) {

        if (Objects.nonNull(items) && items instanceof List) {

            List<DetailRecommendItemVO> detailRecommendVOS=new ArrayList<>();
            for(int index=0;index<((List<ItemEntityVO>)items).size();index++){
                ItemEntityVO item= ((List<ItemEntityVO>)items).get(index);

                //埋点拼接
                Optional.ofNullable(item.getString("scm")).ifPresent(scmJoin::add);

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
            atmosphereList.stream()
                .filter(v -> !DetailSwitch.ignorePromotionList.contains(v.getPromotionType()))
                .forEach(atmosphereDTO -> {
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

    public List<DetailEvent> getContentEvents(DetailRecommendRequest recommendRequest, ContentVO contentVO, int index) {
        return getEvents(recommendRequest.getRecType(), contentVO.getLong("contentId"),
            contentVO.getString(FrontBackMapEnum.contentCustomLink.getFront()), index + 1,
            contentVO.getString("scm"));
    }

    public List<DetailEvent> getEvents(String scene, Long id, String jumpUrl, Integer index, String scm) {

        DetailEvent eventView1 = new DetailEvent("openUrl");

        eventView1.addFieldsParam("url", jumpUrl);

        DetailEvent userTrackEvent = getUserTrackEvent(scene, id, index, scm);

        return Lists.newArrayList(eventView1, userTrackEvent);
    }

    public DetailEvent getUserTrackEvent(String scene, Long id, Integer index, String scm){

        DetailEvent eventView2 = new DetailEvent("userTrack");

        eventView2.addFieldsParam("page", "Page_Detail");
        eventView2.addFieldsParam("eventId", "2101");
        eventView2.addFieldsParam("arg1", "Page_Detail_Button-" + scene);

        Map<String, String> argsMap = new HashMap(4);
        argsMap.put("spm", "a2141.7631564." + scene + "." + index);
        argsMap.put("refer_id", String.valueOf(id));
        argsMap.put("index", String.valueOf(index));
        argsMap.put("type", scene);
        argsMap.put("scm", scm);

        eventView2.addFieldsParam("args", argsMap);

        return eventView2;
    }
    private List<DetailTextComponentVO> buildPrice(String shortText, String price) {

        List<DetailTextComponentVO> priceComponentVOS=new ArrayList<>(3);
        //价格符号
        if (StringUtils.isNotEmpty(shortText)) {
            shortText = shortText + "￥";
        } else {
            shortText = "￥";
        }
        DetailTextComponentVO prefix = new DetailTextComponentVO(shortText,
            new Style("12", "#FF4027", "false", "false", "#ffffff", "false"));

        priceComponentVOS.add(prefix);


        //价格的样式
        Style priceStyle = new Style("14", "#FF4027", "true", "false", "#ffffff", "false");
        priceStyle.setBold("true");

        //价格切分为元和小数点后
        if (!price.contains(".")) {
            priceComponentVOS.add(new DetailTextComponentVO(price, priceStyle));
        } else {

            String[] split = StringUtils.split(price,".");
            //元
            priceComponentVOS.add(new DetailTextComponentVO(split[0], priceStyle));
            //小数
            priceComponentVOS.add(new DetailTextComponentVO(split[1], priceStyle));
        }

        return priceComponentVOS;
    }
}
