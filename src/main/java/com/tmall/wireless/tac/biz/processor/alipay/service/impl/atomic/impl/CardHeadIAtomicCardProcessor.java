package com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.impl;


import com.alibaba.fastjson.JSON;
import com.alipay.tradecsa.common.service.spi.response.PageFloorAtomicResultDTO;
import com.google.common.collect.Lists;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.AliPayServiceImpl;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.AtomicCardProcessRequest;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.IAtomicCardProcessor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CardHeadIAtomicCardProcessor implements IAtomicCardProcessor {

    public static final String PLACE_HOLDER_TITTLE = "$title";
    public static final String PLACE_HOLDER_SUB_TITTLE = "$subTitle";

    public static final String TEMPLATE_JSON = "{\n" +
            "\t\"containerStyle\": {\n" +
            "\t\t\"backgroundColor\": \"#FFE5D3\"\n" +
            "\t},\n" +
            "\t\"title\": \"<span style=\\\"font-size:15sp;color:#FF2F2D;font-weight:700\\\">$title</span>\",\n" +
            "\t\"dot\": \"<span style=\\\"font-size:12sp;color:#FE392F\\\">·</span>\",\n" +
            "\t\"subtitle\": \"<span style=\\\"font-size:12sp;color:#FE392F\\\">$subTitle</span>\",\n" +
            "\t\"scm\": \"xxxx\",\n" +
            "\t\"spmC\": \"xxx\",\n" +
            "\t\"hasBottomDivider\": \"true\"\n" +
            "}";

    @Override
    public String atomicCardId() {
        return "CSDTemplate_Topic_Floor_HeaderV2";
    }

    @Override
    public PageFloorAtomicResultDTO process(AtomicCardProcessRequest atomicCardProcessRequest) {
        String title = Optional.of(atomicCardProcessRequest).map(AtomicCardProcessRequest::getAldData).map(a -> a.getString(AliPayServiceImpl.cardTitleAldKey)).orElse("");
        String subTitle = Optional.of(atomicCardProcessRequest).map(AtomicCardProcessRequest::getAldData).map(a -> a.getString(AliPayServiceImpl.cardSubTitleAldKey)).orElse("");
        PageFloorAtomicResultDTO pageFloorAtomicResultDTO = new PageFloorAtomicResultDTO();
        pageFloorAtomicResultDTO.setAtomCardTemplateId(atomicCardProcessRequest.getPageFloorAtomicDTO().getAtomCardTemplateId());
        String replace = TEMPLATE_JSON.replace(PLACE_HOLDER_TITTLE, title).replace(PLACE_HOLDER_SUB_TITTLE, subTitle);
        pageFloorAtomicResultDTO.setCardData(Lists.newArrayList(JSON.parseObject(replace)));
        return pageFloorAtomicResultDTO;
    }


}
