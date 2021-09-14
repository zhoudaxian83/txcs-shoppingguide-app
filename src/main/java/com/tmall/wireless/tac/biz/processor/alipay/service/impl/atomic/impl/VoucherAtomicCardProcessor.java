package com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPObject;
import com.alipay.tradecsa.common.service.spi.response.PageFloorAtomicResultDTO;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.AtomicCardProcessRequest;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.IAtomicCardProcessor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

// 券原子模板
@Service
public class VoucherAtomicCardProcessor implements IAtomicCardProcessor {


    public static final String ITEM_TEMP = "";

    @Override
    public String atomicCardId() {
        return "CSDTemplate_VoucherCard";
    }

    @Override
    public PageFloorAtomicResultDTO process(AtomicCardProcessRequest atomicCardProcessRequest) {
        return null;
    }

    private JSONObject convert(ItemEntityVO itemEntityVO) {
        return JSON.parseObject(ITEM_TEMP);
    }
}
