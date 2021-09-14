package com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.impl;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.tradecsa.common.service.spi.response.PageFloorAtomicResultDTO;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.AtomicCardProcessRequest;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.IAtomicCardProcessor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.tmall.wireless.tac.biz.processor.alipay.service.impl.AliPayServiceImpl.itemLabelAldKey;

// feeds流
@Service
public class FeedsAtomicCardProcessor implements IAtomicCardProcessor {
    @Override
    public String atomicCardId() {
        return "COMMON_DetailMutablecard";
    }

    public static final String PLACE_HOLDER_ITEM_TITTLE = "$itemTitle";
    public static final String PLACE_HOLDER_ITEM_IMG = "$itemImg";
    public static final String PLACE_HOLDER_ITEM_URL = "$url";
    public static final String PLACE_HOLDER_ITEM_PROMOTION_LABEL = "$promotionLabel";
    public static final String PLACE_HOLDER_ITEM_ORIGIN_PRICE = "$originPrice";
    public static final String PLACE_HOLDER_ITEM_PROMOTION_PRICE = "$promotionPrice";
    public static final String PLACE_HOLDER_ITEM_SCM = "$scm";
    public static final String PLACE_HOLDER_ITEM_SPM = "$spm";


    public static final String ITEM_TEMP = "{\n" +
            "\t\"url\": \"{\\\"type\\\":\\\"jump\\\",\\\"link\\\":\\\"$url\\\"}\",\n" +
            "\t\"moreAction\": \"{\\\"type\\\":\\\"feedback\\\",\\\"items\\\":[{\\\"reason\\\":\\\"F05\\\",\\\"icon\\\":\\\" \\\",\\\"name\\\":\\\"内容不感兴趣\\\"},{\\\"reason\\\":\\\"F04\\\",\\\"icon\\\":\\\"https://gw.alipayobjects.com/mdn/wallet_home/afts/img/A*K7xETZ5_tQoAAAAAAAAAAABkARQnAQ\\\",\\\"name\\\":\\\"图片引起不适\\\"}]}\",\n" +
            "\t\"mainImageUrl\": \"$itemImg\",\n" +
            "\t\"aspectRatio\": \"1\",\n" +
            "\t\"topTag\": {\n" +
//            "\t\t\"text\": \"1元抢购\",\n" +
            "\t\t\"textStyle\": {}\n" +
            "\t},\n" +
//            "\t\"topTagImage\": \"https://work.alibaba-inc.com/photo/161767.220x220.jpg\",\n" +
            "\t\"mainTag\": {\n" +
//            "\t\t\"text\": \"西餐·5.5公里\",\n" +
            "\t\t\"textStyle\": {}\n" +
            "\t},\n" +
            "\t\"title\": \"$itemTitle\",\n" +
//            "\t\"scoreValue\": \"<span style=\\\"color:#FF6010\\\">4.7分</span>\",\n" +
//            "\t\"scoreDesc\": \"\\\"鸡汁烩饭好吃\\\"\",\n" +
//            "\t\"description\": \"鸡汁烩饭好吃鸡汁烩饭好吃鸡汁烩饭好吃鸡汁烩饭好吃鸡汁烩饭好吃鸡汁烩饭好吃\",\n" +
            "\t\"tags\": [{\n" +
            "\t\t\t\"text\": \"$promotionLabel\",\n" +
            "\t\t\t\"borderColor\": \"#FF6010\",\n" +
            "\t\t\t\"textStyle\": {\n" +
            "\t\t\t\t\"borderColor\": \"#FF6010\"\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t],\n" +
//            "\t\"shortTags\": [{\n" +
//            "\t\t\"text\": \"满39减6\",\n" +
//            "\t\t\"textStyle\": {\n" +
//            "\t\t\t\"borderColor\": \"#000\"\n" +
//            "\t\t}\n" +
//            "\t}],\n" +
            "\t\"tagImage\": \"xxxx\",\n" +
//            "\t\"tagImageLeftText\": \"首单直降\",\n" +
//            "\t\"tagImageRightText\": \"100元\",\n" +
            "\t\"decideNumber\": \"<span style=\\\"font-size:10sp;\\\">¥</span><span style=\\\"color:#FF6010\\\">$promotionPrice</span>\",\n" +
            "\t\"decideNumberDesc\": \"$originPrice\",\n" +
            "\t\"decideNumberDescStyle\": {\n" +
            "\t\t\"textDecoration\": \"line-through\"\n" +
            "\t},\n" +
            "\t\"padding\": true,\n" +
//            "\t\"decideText\": \"2819条 | ¥130/人\",\n" +
//            "\t\"bottomName\": \"<span>店铺或者频道名称</span>\",\n" +
//            "\t\"bottomIcon\": \"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAGgAAABoCAMAAAAqwkWTAAAABGdBTUEAALGPC/xhBQAAAAFzUkdCAK7OHOkAAABmUExURUdwTPj7//////j7//////j7//3///////v9//////r8//////r8//z9//////////////z9//////z9//v9/////+z0/8Hb//f6/9Ll/93s/+Tv/83i/8ng/+nz/9jo/8Xd//L4/yibMIIAAAAWdFJOUwDtI/cc540DyEvSD9uuLGhXuHeyoz4KpowLAAAEYUlEQVR42sWa63LyKhSGkzHmoLG2HlY4Q+7/JvdXsZumyQtGmfH51dbpvK4jsKB4nOp6rM/tri83m7Lftef6eK2K3HwcDyVboDwcP7KJNN1lyyJsL12Tw5Y6qGCt+lW7upY9SNu94LPPHVvB7rN50pqeraR/xqrqwJ7gUK312mnDnmJzalaZ07KnaVcY1ZXsBcruUbfV7EXqh9y3/2Iv87VPm7NvWQbapFLVsyz0VUJny9ZAXIkRtL+o0n6lPWoYBieBTRHvrY6PG76Ra+PUrM43PtwgkHsoy9fXj/FCHNUT6AdsPcIrjeDjbjHhnuo78iYkUDeqFgK0kAij4NzKMa1kYeHOw3RiM+zgsSyG9kECnGaO2wCvzIJNVnFiAR4V2vx13gEE2iMiQeFxmw/pjKMhQEHnr7QKv6Uzr+mXO5kenC9+NbOS/+4OkmH6pgh8Msyoh3+Y0AmcvP1Fj/7jYDDgswjsWAT6MYm01yFz/+H/5sBi7ECEQJrT6LNdGyIy3Dt0ZEwEx6ajhJt28A53PjSGbth7iY0h6SBtcecj3aR/sPSDD9TgwjqB+Xi0a5vBoyUFjDdq1lNxF2+2KZ37l7c0RSqv44jF2TY4FeYtjxuaIXQo33Q6XOKZoILXFhD3go4bdbkJRT1H7uYcQcuEUMmo73zOJXWUIUQIlU3l3TGpYymBDd0PcJwsEKB/C4JMi0rFF4sS54ELOgmMiiuVRVHF96GDpMfgce9VxRXXD7IHK+GMuOJcIJAHcSUDs6GOOU7RGpRfDkG7O8f2a4bWYBx23hmuRRo6DiNwK2+LHTZI01oUNGlX9Hits7QWCU3qQb2OMEJpk+RyxW6g5zitR6Cq3QAhDmo1hfleUxaFgOs09lzad7Touh6FSNMzcBCkfjm9CYQojQU7iF3R5hUSoJLa5RZknhaSQOhc1Ci7bVaL6uKY1yIBYnQsrihGKqvQFSzlPr0zClVFUcKCzVhHJdpuWdCCkmjfvpe2W8ecTZVArzv6LXG+ZeL2BdFRbPvYwicXdY2ZhUiATX5xQb5z5peMGwbNhZlIW+6mJwADZgGXcBBLbU7U4HFacW4tV0rPz7Vkgee6yNFSTvfDboDw6X7LwKNlUcMNpDbB+RAxMVtFRp4fkYEJ3TEc2OTs9JhkUM7hgYaY+oWkVROxv9khBrQWpUY0dn6sNFIIa/l3Pog/+W7hCalLDp0UOI/Ds4QbwdApNUbz//3w2VJTaozW9FApGJU8l6sRDQYDXWKCriJSgkfn+R0c3k4x7i4lzKIxPu3xpcsBjKNB8nktO0k0KbgO87URj6OnnBjEZ1Todf9QelJSfGSAE7gyAJB1A0RJBmmb1ZcgcrEHOS6AMfASpGg6loDE7ybkVLgfQXSvXFSN9I2XSFDjq7esfDVrLxPzX/rue5aNfp+48M3EtnrvFXYg16X8e58Z5H848danIPkft7z1uU7+B0hvfVKV/5HYu5+95X/I9/6nie9/bJn/+eh//8QNnouSqTMAAAAASUVORK5CYII=\",\n" +
            "\t\"scm\": \"$scm\",\n" +
            "\t\"spmC\": \"$spm\",\n" +
            "\t\"queryDataInfo\": {\n" +
            "\t\t\"bizCode\": \"xxx\",\n" +
            "\t\t\"businessId\": \"xxx\",\n" +
            "\t\t\"extInfo\": \"xxx\"\n" +
            "\t}\n" +
            "}";

    @Override
    public PageFloorAtomicResultDTO process(AtomicCardProcessRequest atomicCardProcessRequest) {
        PageFloorAtomicResultDTO pageFloorAtomicResultDTO = new PageFloorAtomicResultDTO();
        pageFloorAtomicResultDTO.setAtomCardTemplateId(atomicCardProcessRequest.getPageFloorAtomicDTO().getAtomCardTemplateId());
        List<ItemEntityVO> itemEntityVOS =
                Optional.of(atomicCardProcessRequest).map(AtomicCardProcessRequest::getItemAndContentList).orElse(Lists.newArrayList());

        if (MiddlePageUtil.isFirstPage(atomicCardProcessRequest.getMiddlePageSPIRequest())) {
            if (itemEntityVOS.size() > CardItemAtomicCardProcessor.CARD_ITEM_SIZE) {
                itemEntityVOS = itemEntityVOS.subList(CardItemAtomicCardProcessor.CARD_ITEM_SIZE, itemEntityVOS.size());
            }
        }

        List<JSONObject> collect = itemEntityVOS.stream().map(itemEntityVO -> {
            Map<String, String> scmMap = Maps.newHashMap();
            scmMap.put("uid", "357133924");
            scmMap.put("iid", "357133924");
            scmMap.put("abid", "357133924");
            String scm = Joiner.on(",").withKeyValueSeparator(":").join(scmMap);

            String itemUrl = itemEntityVO.getString("itemUrl") + "&flowChannel=smAlipayHomeCard";

            String replace = ITEM_TEMP.replace(PLACE_HOLDER_ITEM_IMG, itemEntityVO.getString("itemImg"))
                    .replace(PLACE_HOLDER_ITEM_TITTLE, itemEntityVO.getString("shortTitle"))
                    .replace(PLACE_HOLDER_ITEM_URL, "https:" + itemUrl)
                    .replace(PLACE_HOLDER_ITEM_ORIGIN_PRICE, itemEntityVO.getString("chaoshiPrice"))
                    .replace(PLACE_HOLDER_ITEM_PROMOTION_LABEL, getPromotionPoint(atomicCardProcessRequest.getAldData(), itemEntityVO))
                    .replace(PLACE_HOLDER_ITEM_PROMOTION_PRICE, itemEntityVO.getString("showPrice"))
                    .replace(PLACE_HOLDER_ITEM_SCM, scm)
                    .replace(PLACE_HOLDER_ITEM_SPM, "")
                    ;
            return JSON.parseObject(replace);

        }).collect(Collectors.toList());
        pageFloorAtomicResultDTO.setCardData(collect);

        return pageFloorAtomicResultDTO;
    }

    private CharSequence getPromotionPoint(GeneralItem aldData, ItemEntityVO itemEntityVO) {
        String promotionPoint = getPromotionPoint(itemEntityVO);
        return StringUtils.isEmpty(promotionPoint) ? aldData.getString(itemLabelAldKey) : promotionPoint;
    }

    private String getPromotionPoint(ItemEntityVO itemEntityVO) {
        try {
            Object promotionPoint = itemEntityVO.get("promotionPoint");
            if (promotionPoint instanceof List) {
                List l = (List) promotionPoint;
                if (l.size() > 0) {
                    return l.get(0).toString();
                }
            }
        } catch (Exception e) {

        }
        return "";
    }
}
