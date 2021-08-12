package com.tmall.wireless.tac.biz.processor.alipay.service.impl;

import com.alipay.recmixer.common.service.facade.model.CategoryContentRet;
import com.alipay.recmixer.common.service.facade.model.MixerCollectRecRequest;
import com.alipay.recmixer.common.service.facade.model.MixerCollectRecResult;
import com.alipay.recmixer.common.service.facade.model.ServiceContentRec;
import com.alipay.tradecsa.common.service.spi.request.MiddlePageSPIRequest;
import com.alipay.tradecsa.common.service.spi.response.MiddlePageSPIResponse;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.wireless.tac.biz.processor.alipay.constant.AliPayConstant;
import com.tmall.wireless.tac.biz.processor.alipay.service.IAliPayService;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by yangqing.byq on 2021/8/6.
 */
@Service("mockAliPayServiceImpl")
public class MockAliPayServiceImpl implements IAliPayService {
    @Override
    public Flowable<MixerCollectRecResult> processFirstPage(Context context, MixerCollectRecRequest mixerCollectRecRequest) {

        MixerCollectRecResult mixerCollectRecResult = new MixerCollectRecResult();
        mixerCollectRecResult.setSuccess(true);

        CategoryContentRet categoryContentRet = new CategoryContentRet();
        Map<String, CategoryContentRet> categoryContentRetMap = Maps.newHashMap();
        categoryContentRetMap.put(AliPayConstant.CATEGORY_CODE, categoryContentRet);
        mixerCollectRecResult.setCategoryContentMap(categoryContentRetMap);


        List<ServiceContentRec>	serviceContentRecList = Lists.newArrayList();
        categoryContentRet.setTitle("天猫超市");
        categoryContentRet.setSubTitle("送货上门 品质保证");
        categoryContentRet.setServiceList(serviceContentRecList);

        serviceContentRecList.add(getServiceContent("商品1"));
        serviceContentRecList.add(getServiceContent("商品2"));
        serviceContentRecList.add(getServiceContent("商品3"));
        return Flowable.just(mixerCollectRecResult);
    }

    @Override
    public Flowable<MiddlePageSPIResponse> processMiddlePage(Context context, MiddlePageSPIRequest middlePageSPIResponse) {
        return null;
    }


    private ServiceContentRec getServiceContent(String title) {
        ServiceContentRec serviceContentRec = new ServiceContentRec();
        serviceContentRec.setItemId("600561956069");
        serviceContentRec.setImgUrl("//img.alicdn.com/imgextra/i2/725677994/O1CN01OcEXyM28vIqflAKlN_!!2-item_pic.png");
        serviceContentRec.setSubTitle("正品保障");
        serviceContentRec.setTitle("按压式无氟1-12岁换牙期香橙味儿童牙膏");
        serviceContentRec.setActionLink("//detail.tmall.com/item.htm?&id=638125586345&locType=B2C&scm=1007.35385.224506.0.FF-hyhsfZ_appId-25385_I-_Q-7d00643e-eb7c-4728-8a0a-2ee55ae9df71_D-638125586345_T-ITEM_businessType-B2C_predCTR-0_predCVR-0_predCTCVR-0_calibCTR-0_calibCVR-0_calibCTCVR-0_finalScore-0-FF");
        serviceContentRec.setBizCode(AliPayConstant.BIZ_CODE);
        serviceContentRec.setSource(AliPayConstant.SOURCE);
        serviceContentRec.setTitle(title);
        return serviceContentRec;
    }




}
