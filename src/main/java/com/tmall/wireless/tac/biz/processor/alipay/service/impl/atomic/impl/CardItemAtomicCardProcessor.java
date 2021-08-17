package com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alipay.tradecsa.common.service.spi.response.PageFloorAtomicResultDTO;
import com.google.common.collect.Lists;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.AtomicCardProcessRequest;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.IAtomicCardProcessor;
import org.springframework.stereotype.Service;
import sun.security.krb5.internal.PAData;

import java.util.List;
import java.util.stream.Collectors;

// 卡片六宫格商品
@Service
public class CardItemAtomicCardProcessor implements IAtomicCardProcessor {

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

    public static final String PLACE_HOLDER_ITEM_TITTLE = "$itemTitle";
    public static final String PLACE_HOLDER_ITEM_IMG = "$itemImg";
    public static final String PLACE_HOLDER_ITEM_URL = "$url";
    public static final String PLACE_HOLDER_ITEM_PROMOTION_LABEL = "$promotionLabel";
    public static final String PLACE_HOLDER_ITEM_ORIGIN_PRICE = "$originPrice";


    private static final String ITEM_TEMPLATE =
            "{\n" +
                    "\t\"//\": \"item图片的url\",\n" +
                    "\t\"image\": \"$itemImg\",\n" +
                    "\t\"//\": \"图片点击跳转链接\",\n" +
                    "\t\"action\": \"{\\\"type\\\":\\\"jump\\\",\\\"link\\\":\\\"$url\\\"}\",\n" +
                    "\t\"//\": \"小item标题\",\n" +
                    "\t\"title\": \"<span style=\\\\\\\"font-size:26;color:#333333\\\\\\\">$itemTitle</span>\",\n" +
                    "\t\"//\": \"小item标题（两行样式）\",\n" +
                    "\t\"complexTitle\": \"<span style=\\\\\\\"font-size:26;color:#333333\\\\\\\">红烧牛肉面</span>\",\n" +
                    "\t\"topLabelStyle\": {\n" +
                    "\t\t\"backgroundColor\": \"#FFFFFF\"\n" +
                    "\t},\n" +
                    "\t\"//\": \"标签顶部图片\",\n" +
                    "\t\"tagTopImage\": \"imageUrl\",\n" +
                    "\t\"//\": \"标签底部文字\",\n" +
                    "\t\"originalPrice\": \"￥$originPrice\",\n" +
                    "\t\"originalPriceStyle\": {\n" +
                    "\t\t\"color\": \"#FFFFFF\",\n" +
                    "\t\t\"fontSize\": \"10sp\",\n" +
                    "\t\t\"textDecoration\": \"line-through\"\n" +
                    "\t},\n" +
                    "\t\"//\": \"标签'纯文案'文字\",\n" +
                    "\t\"pureStringText\": \"<span style=\\\\\\\"font-size:12;color:#999999\\\\\\\">领劵更优惠</span>\",\n" +
                    "\t\"pureStringTextStyle\": {\n" +
                    "\t\t\"backgroundColor\": \"#FFFFFF\"\n" +
                    "\t},\n" +
                    "\t\"//\": \"单个小item埋点scm\",\n" +
                    "\t\"scm\": \"xxxx\",\n" +
                    "\t\"//\": \"单个小item埋点扩展参数\",\n" +
                    "\t\"remoteLogExt\": \"xxx\",\n" +
                    "\t\"//\": \"满减价格标签（二房字段控制，下发就展示）\",\n" +
                    "\t\"saleTags\": [{\n" +
                    "\t\t\t\"text\": \"<span style=\\\"font-size:10sp;color:#FF6010\\\">$promotionLabel</span>\",\n" +
                    "\t\t\t\"textStyle\": {\n" +
                    "\t\t\t\t\"borderColor\": \"#FF6010\"\n" +
                    "\t\t\t}\n" +
                    "\t\t},\n" +
                    "\t\t{\n" +
                    "\t\t\t\"text\": \"<span style=\\\"font-size:10sp;color:#FF6010\\\"></span>\",\n" +
                    "\t\t\t\"textStyle\": {\n" +
                    "\t\t\t\t\"borderColor\": \"#FF6010\"\n" +
                    "\t\t\t}\n" +
                    "\t\t}\n" +
                    "\t],\n" +
                    "\t\"defaultFooterText\": \"<span style=\\\"font-size:12sp;color:#999999\\\">’30分钟送达\n" +
                    "\t'</span>\"\n" +
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
        String replace = ITEM_TEMPLATE.replace(PLACE_HOLDER_ITEM_IMG, itemEntityVO.getString("itemImg"))
                .replace(PLACE_HOLDER_ITEM_TITTLE, itemEntityVO.getString("shortTitle"))
                .replace(PLACE_HOLDER_ITEM_URL, itemEntityVO.getString("itemUrl"))
                .replace(PLACE_HOLDER_ITEM_ORIGIN_PRICE, itemEntityVO.getString("678"))
                .replace(PLACE_HOLDER_ITEM_PROMOTION_LABEL, itemEntityVO.getString("超市热卖"))
                ;
        return JSON.parseObject(replace);
    }
}
