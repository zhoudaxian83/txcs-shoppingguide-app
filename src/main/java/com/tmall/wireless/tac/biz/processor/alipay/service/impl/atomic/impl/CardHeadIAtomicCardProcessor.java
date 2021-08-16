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
            "  \"containerStyle\":{\n" +
            "    \"backgroundColor\":\"#FFE5D3\",//背景色\n" +
            "  },\n" +
            "  \"image\": \"xxxxxx\",//百亿补贴图片的cloudid,\n" +
            "  \"title\":\"<span style=\\\"font-size:15sp;color:#FF2F2D;font-weight:700\\\">特价门票</span>\",\n" +
            "  \"dot\":\"<span style=\\\"font-size:12sp;color:#FE392F\\\">·</span>\",\n" +
            "  \"subtitle\":\"<span style=\\\"font-size:12sp;color:#FE392F\\\">全网景点最低价</span>\",\n" +
            "  \"scm\":\"xxxx\",\n" +
            "  \"spmC\":\"xxx\",\n" +
            "  \"hasBottomDivider\":\"true\",\n" +
            "  \"rightImage\":\"\"\n" +
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
