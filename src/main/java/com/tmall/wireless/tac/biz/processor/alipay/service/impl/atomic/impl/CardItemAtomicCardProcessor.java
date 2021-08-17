package com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.tradecsa.common.service.spi.response.PageFloorAtomicResultDTO;
import com.google.common.collect.Lists;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.AtomicCardProcessRequest;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.IAtomicCardProcessor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

// 卡片六宫格商品
@Service
public class CardItemAtomicCardProcessor implements IAtomicCardProcessor {


    public static final String PLACE_HOLDER_ITEM_TITTLE = "$itemTitle";
    public static final String PLACE_HOLDER_ITEM_IMG = "$itemImg";
    public static final String PLACE_HOLDER_ITEM_URL = "$url";
    public static final String PLACE_HOLDER_ITEM_PROMOTION_LABEL = "$promotionLabel";
    public static final String PLACE_HOLDER_ITEM_ORIGIN_PRICE = "$originPrice";


    public static final String TEMPLATE_ITEM = "{\n" +
            "\t\"saleTags\": [{\n" +
            "\t\t\"text\": \"<span style='font-size:10sp;color:#FF6010'>$promotionLabel</span>\",\n" +
            "\t\t\"textStyle\": {\n" +
            "\t\t\t\"borderColor\": \"#FF6010\"\n" +
            "\t\t}\n" +
            "\t}],\n" +
            "\t\"title\": \"<span style='font-size:26;color:#333333'>$itemTitle</span>\",\n" +
            "\t\"image\": \"//img.alicdn.com/imgextra/i3/6000000007992/O1CN01VRuIZA28uO03vhrlZ_!!6000000007992-0-at.jpg\",\n" +
            "\t\"remoteLogExt\": \"{\\\"pageBizCode\\\":\\\"product\\\",\\\"cityCode\\\":\\\"330100\\\",\\\"bizCode\\\":\\\"product\\\",\\\"latitude\\\":\\\"30.265642\\\",\\\"source\\\":\\\"homeFeeds\\\",\\\"longitude\\\":\\\"120.108739\\\",\\\"scene\\\":\\\"SSU\\\"}\",\n" +
            "\t\"action\": \"{\\\"link\\\":\\\"$url\\\",\\\"scm\\\":\\\"\\\",\\\"type\\\":\\\"jump\\\"}\",\n" +
            "\t\"topLabelStyle\": {\n" +
            "\t\t\"backgroundImage\": \"linear-gradient(tobottom,#FF1919,#FF683C)\"\n" +
            "\t},\n" +
            "\t\"defaultTemplateId\": \"\",\n" +
            "\t\"tagImageV2\": \"https://gw.alipayobjects.com/mdn/rms_5bd46e/afts/img/A*IL4aRamkbjIAAAAAAAAAAAAAARQnAQ\",\n" +
            "\t\"complexTitle\": \"<span style='font-size:13sip;color:#333333'>超长标题看看超过两行会怎么样</span>\",\n" +
            "\t\"tagLeftTextV2\": \"<span style='font-size:12sp;color:#FFFFFF;'>$promotionLabel</span>\",\n" +
            "\t\"tagRightTextV2\": \"<span style='font-size:15sp;color:#FF2F23'>抢</span>\",\n" +
            "\t\"originalPrice\": \"$originPrice\",\n" +
            "\t\"originalPriceStyle\": {\n" +
            "\t\t\"color\": \"#ccffffff\",\n" +
            "\t\t\"fontSize\": \"10sp\",\n" +
            "\t\t\"textDecoration\": \"line-through\"\n" +
            "\t},\n" +
            "\t\"scm\": \"a1001.b1001.product.631609210471.0be91e0416121510059374417eb10a.KOUBEI.CSDTemplate_koubei_5zhe_2line6.tradecsa__a933df203250ad1ff35c..222757029\",\n" +
            "\t\"tagImage\": \"https://gw.alipayobjects.com/mdn/rms_5bd46e/afts/img/A*7PK3R6MjqtMAAAAAAAAAAAAAARQnAQ\"\n" +
            "}";

//    private static String PLACE_HOLDER_IMG_URL =
    public static final String CARD_TEMPLATE =
        "{\n" +
        "\t\"containerStyle\": {\n" +
        "\t\t\"//\": \"整个grid背景框的底色，依赖服务端下发,写成这样是为了方便写css样式代码\",\n" +
        "\t\t\"backgroundImage\": \"linear-gradient(to bottom,#FFE5D3,#FFDCC4)\"\n" +
        "\t},\n" +
        "\t\"//\": \"是否有头部间隔，因为头部卡片背景里有图片，所以只在头部卡片背景里有图片的case下发为true，默认false\",\n" +
        "\t\"hideTopDivider\": \"true\",\n" +
        "\t\"items\": [],\n" +
        "\t\"spmC\": \"xxx\"\n" +
        "}";


    private static final String ITEM_TEMPLATE =
            "{\n" +
                    "\t\"image\": \"$itemImg\",\n" +
                    "\t\"action\": \"{\\\"type\\\":\\\"jump\\\",\\\"link\\\":\\\"$url\\\"}\",\n" +
                    "\t\"title\": \"<span style=\\\\\\\"font-size:26;color:#333333\\\\\\\">$itemTitle</span>\",\n" +
                    "\t\"topLabelStyle\": {\n" +
                    "\t\t\"backgroundColor\": \"#FFFFFF\"\n" +
                    "\t},\n" +
                    "\t\"originalPrice\": \"￥$originPrice\",\n" +
                    "\t\"originalPriceStyle\": {\n" +
                    "\t\t\"color\": \"#FFFFFF\",\n" +
                    "\t\t\"fontSize\": \"10sp\",\n" +
                    "\t\t\"textDecoration\": \"line-through\"\n" +
                    "\t},\n" +
                    "\t\"pureStringTextStyle\": {\n" +
                    "\t\t\"backgroundColor\": \"#FFFFFF\"\n" +
                    "\t},\n" +
                    "\t\"scm\": \"xxxx\",\n" +
                    "\t\"remoteLogExt\": \"xxx\",\n" +
                    "\t\"saleTags\": [{\n" +
                    "\t\t\t\"text\": \"<span style=\\\"font-size:10sp;color:#FF6010\\\">$promotionLabel</span>\",\n" +
                    "\t\t\t\"textStyle\": {\n" +
                    "\t\t\t\t\"borderColor\": \"#FF6010\"\n" +
                    "\t\t\t}\n" +
                    "\t\t}\n" +
                    "\t]\n" +
                    "}";



//        result.put("title", iteminfo.getItemDTO().getTitle());
//        result.put("specDetail", iteminfo.getItemDTO().getSpecDetail());
//        result.put("shortTitle", iteminfo.getItemDTO().getShortTitle());
//        result.put("itemMPrice", iteminfo.getItemDTO().getDisplayPrice());
//        result.put("itemMPriceLong", iteminfo.getItemDTO().getDisplayPriceCent());
//        result.put("itemPromotionResp", iteminfo.getItemDTO().getItemPromotionResp());
//        result.put("selfSupportProperties", iteminfo.getItemDTO().getSelfSupportProperties());
//        result.put("itemImg", iteminfo.getItemDTO().getItemImg());
//        result.put("itemUrl", iteminfo.getItemDTO().getDetailUrl());
//        result.put("skuId", iteminfo.getItemDTO().getSkuId());
//        result.put("sellOut", iteminfo.getItemDTO().isSellOut());
//        result.put("canBuy", iteminfo.getItemDTO().isCanBuy());

    @Override
    public String atomicCardId() {
        return "CSDTemplate_Topic_Floor_Grid";
    }

    @Override
    public PageFloorAtomicResultDTO process(AtomicCardProcessRequest atomicCardProcessRequest) {
        List<ItemEntityVO> itemAndContentList = atomicCardProcessRequest.getItemAndContentList();
        PageFloorAtomicResultDTO pageFloorAtomicResultDTO = new PageFloorAtomicResultDTO();
        pageFloorAtomicResultDTO.setAtomCardTemplateId(atomicCardProcessRequest.getPageFloorAtomicDTO().getAtomCardTemplateId());
        JSONObject jsonObject = JSON.parseObject(CARD_TEMPLATE);
        List<JSONObject> collect = itemAndContentList.subList(0, Math.min(6, itemAndContentList.size())).stream().map(this::convert).collect(Collectors.toList());
        jsonObject.put("items", collect);
        pageFloorAtomicResultDTO.setCardData(Lists.newArrayList(jsonObject));
        return pageFloorAtomicResultDTO;
    }

    private JSONObject convert(ItemEntityVO itemEntityVO) {
        String replace = TEMPLATE_ITEM.replace(PLACE_HOLDER_ITEM_IMG, itemEntityVO.getString("itemImg"))
                .replace(PLACE_HOLDER_ITEM_TITTLE, itemEntityVO.getString("shortTitle"))
                .replace(PLACE_HOLDER_ITEM_URL, itemEntityVO.getString("itemUrl"))
                .replace(PLACE_HOLDER_ITEM_ORIGIN_PRICE, "678")
                .replace(PLACE_HOLDER_ITEM_PROMOTION_LABEL, "超市热卖")
                ;


        return JSON.parseObject(replace);
    }


}
