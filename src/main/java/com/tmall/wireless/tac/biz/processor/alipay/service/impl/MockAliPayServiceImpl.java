package com.tmall.wireless.tac.biz.processor.alipay.service.impl;

import com.alipay.recmixer.common.service.facade.model.CategoryContentRet;
import com.alipay.recmixer.common.service.facade.model.MixerCollectRecRequest;
import com.alipay.recmixer.common.service.facade.model.MixerCollectRecResult;
import com.alipay.recmixer.common.service.facade.model.ServiceContentRec;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.wireless.tac.biz.processor.alipay.constant.AliPayConstant;
import com.tmall.wireless.tac.biz.processor.alipay.service.IAliPayService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by yangqing.byq on 2021/8/6.
 */
@Service
public class MockAliPayServiceImpl implements IAliPayService {
    @Override
    public MixerCollectRecResult processFirstPage(MixerCollectRecRequest mixerCollectRecRequest) {

        MixerCollectRecResult mixerCollectRecResult = new MixerCollectRecResult();
        mixerCollectRecResult.setSuccess(true);

        CategoryContentRet categoryContentRet = new CategoryContentRet();
        Map<String, CategoryContentRet> categoryContentRetMap = Maps.newHashMap();
        categoryContentRetMap.put(AliPayConstant.CATEGORY_CODE, categoryContentRet);
        mixerCollectRecResult.setCategoryContentMap(categoryContentRetMap);
        categoryContentRetMap.put(AliPayConstant.CATEGORY_CODE, categoryContentRet);


        List<ServiceContentRec>	serviceContentRecList = Lists.newArrayList();
        categoryContentRet.setTitle("天猫超市");
        categoryContentRet.setSubTitle("送货上门 品质保证");
        categoryContentRet.setServiceList(serviceContentRecList);

        serviceContentRecList.add(getServiceContent("商品1"));
        serviceContentRecList.add(getServiceContent("商品2"));
        serviceContentRecList.add(getServiceContent("商品3"));
        return mixerCollectRecResult;
    }

    private ServiceContentRec getServiceContent(String title) {
        ServiceContentRec serviceContentRec = new ServiceContentRec();
        serviceContentRec.setItemId("12345");
        serviceContentRec.setBizCode(AliPayConstant.BIZ_CODE);
        serviceContentRec.setSource(AliPayConstant.SOURCE);
        serviceContentRec.setTitle(title);
        return serviceContentRec;
    }
}
