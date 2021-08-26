package com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.impl;


import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.alibaba.fastjson.JSON;
import com.alipay.tradecsa.common.service.spi.response.PageFloorAtomicResultDTO;
import com.google.common.collect.Lists;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.AliPayServiceImpl;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.AtomicCardProcessRequest;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.IAtomicCardProcessor;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.tmall.wireless.tac.biz.processor.alipay.service.impl.AliPayServiceImpl.cardBgPicAldKey;
import static com.tmall.wireless.tac.biz.processor.alipay.service.impl.AliPayServiceImpl.fpIconPicAldKey;

@Service
public class CardHeadIAtomicCardProcessor implements IAtomicCardProcessor {


    public static final String PLACE_HOLDER_TITTLE_PIC = "$titlePic";
    public static final String PLACE_HOLDER_TITTLE = "$title";
    public static final String PLACE_HOLDER_SUB_TITTLE = "$subTitle";
    public static final String PLACE_HOLDER_BG_COLOR = "$bgColor";
    public static final String PLACE_HOLDER_BG_IP = "$bgPic";




    public static final String TEMPLATE_JSON_NEW =
            "{\n" +
            "        \"//\":\"图片标题 - 即今日福利\",\n" +
            "        \"image\": \"https://gw.alipayobjects.com/mdn/rms_5bd46e/afts/img/A*gKwGToTcNHEAAAAAAAAAAAAAARQnAQ\",\n" +
            "        \"//\":\"不用动\",\n" +
            "        \"imageRatio\": \"4.93\",\n" +
            "        \"title\": \"<span style=\\\"font-size:15sp;color:#FFFFFF;font-weight:700\\\">$title</span>\",\n" +
            "        \"//\":\"今日福利后面的 · 看有没有换的需求\",\n" +
            "        \"dot\": \"<span style=\\\"font-size:12sp;color:#FFFFFF\\\">·</span>\",\n" +
            "        \"//\":\"今日福利后面的 这里随便写一句 看有没有换的需求\",\n" +
            "        \"subtitle\": \"<span style=\\\"font-size:12sp;color:#FFFFFF\\\">$subTitle</span>\",\n" +
            "        \"scm\": \"xxxx\",\n" +
            "        \"spmC\": \"xxx\",\n" +
            "        \"//\":\"区域背景色，按照现在的来配置就好\",\n" +
            "        \"containerStyle\": {\n" +
            "            \"backgroundColor\": \"$bgColor\"\n" +
            "        },\n" +
            "        \"hasBottomDivider\": \"true\",\n" +
            "        \"//\":\"右侧金钱图片，可以直接copy这个值\",\n" +
            "        \"rightImage\": \"$bgPic\"\n" +
            "    }";

    @Override
    public String atomicCardId() {
        return "CSDTemplate_Special_Supply_Header";
    }

    @Override
    public PageFloorAtomicResultDTO process(AtomicCardProcessRequest atomicCardProcessRequest) {

        String title = Optional.of(atomicCardProcessRequest).map(AtomicCardProcessRequest::getAldData).map(a -> a.getString(AliPayServiceImpl.cardTitleAldKey)).orElse("");
        String subTitle = Optional.of(atomicCardProcessRequest).map(AtomicCardProcessRequest::getAldData).map(a -> a.getString(AliPayServiceImpl.cardSubTitleAldKey)).orElse("");
        GeneralItem aldDate = Optional.of(atomicCardProcessRequest).map(AtomicCardProcessRequest::getAldData).orElse(new GeneralItem());
        PageFloorAtomicResultDTO pageFloorAtomicResultDTO = new PageFloorAtomicResultDTO();
        pageFloorAtomicResultDTO.setAtomCardTemplateId(atomicCardProcessRequest.getPageFloorAtomicDTO().getAtomCardTemplateId());
        String replace = TEMPLATE_JSON_NEW
                .replace(PLACE_HOLDER_TITTLE, title)
                .replace(PLACE_HOLDER_SUB_TITTLE, subTitle)
                .replace(PLACE_HOLDER_TITTLE_PIC, "https://gw.alipayobjects.com/mdn/rms_5bd46e/afts/img/A*gKwGToTcNHEAAAAAAAAAAAAAARQnAQ")
                .replace(PLACE_HOLDER_BG_COLOR, "#FF3D29")
                .replace(PLACE_HOLDER_BG_IP, aldDate.getString(cardBgPicAldKey));

        pageFloorAtomicResultDTO.setCardData(Lists.newArrayList(JSON.parseObject(replace)));
        return pageFloorAtomicResultDTO;
    }


}
