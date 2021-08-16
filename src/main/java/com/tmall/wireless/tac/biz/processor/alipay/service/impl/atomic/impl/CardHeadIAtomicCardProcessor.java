package com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.impl;


import com.alibaba.fastjson.JSON;
import com.alipay.tradecsa.common.service.spi.response.PageFloorAtomicResultDTO;
import com.google.common.collect.Lists;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.AtomicCardProcessRequest;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.IAtomicCardProcessor;
import org.springframework.stereotype.Service;

@Service
public class CardHeadIAtomicCardProcessor implements IAtomicCardProcessor {

    public static final String TEMPLATE_JSON = "{\n" +
            "\t\"containerStyle\": {\n" +
            "\t\t\"backgroundColor\": \"#FFE5D3\"\n" +
            "\t},\n" +
            "\t\"title\": \"<span style=\\\"font-size:15sp;color:#FF2F2D;font-weight:700\\\">特价门票</span>\",\n" +
            "\t\"dot\": \"<span style=\\\"font-size:12sp;color:#FE392F\\\">·</span>\",\n" +
            "\t\"subtitle\": \"<span style=\\\"font-size:12sp;color:#FE392F\\\">全网景点最低价</span>\",\n" +
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
        PageFloorAtomicResultDTO pageFloorAtomicResultDTO = new PageFloorAtomicResultDTO();
        pageFloorAtomicResultDTO.setAtomCardTemplateId(atomicCardProcessRequest.getPageFloorAtomicDTO().getAtomCardTemplateId());
        pageFloorAtomicResultDTO.setCardData(Lists.newArrayList(JSON.parseObject("TEMPLATE_JSON")));
        return pageFloorAtomicResultDTO;
    }
}
