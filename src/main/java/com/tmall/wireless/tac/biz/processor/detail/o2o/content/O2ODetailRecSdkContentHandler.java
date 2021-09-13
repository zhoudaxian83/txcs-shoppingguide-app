package com.tmall.wireless.tac.biz.processor.detail.o2o.content;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;

import com.google.common.collect.Lists;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.framework.model.ContentVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkContentService;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.wireless.tac.biz.processor.common.PackageNameKey;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.DetailConstant;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.RecTypeEnum;
import com.tmall.wireless.tac.biz.processor.detail.common.convert.AbstractConverter;
import com.tmall.wireless.tac.biz.processor.detail.common.convert.ReciptConverter;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecContentResultVO;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecommendRequest;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created from template by 归晨 on 2021-09-09 21:01:31.
 *
 */

@Service
public class O2ODetailRecSdkContentHandler extends RpmReactiveHandler<DetailRecContentResultVO> implements
    InitializingBean {

    @Resource
    List<AbstractConverter> converterList;

    public static Map<String, AbstractConverter> converters;
    static {

    }
    @Autowired
    ShoppingguideSdkContentService shoppingguideSdkContentService;

    @Override
    public Flowable<TacResult<DetailRecContentResultVO>> executeFlowable(Context context) throws Exception {
        BizScenario b = BizScenario.valueOf(
            DetailConstant.BIZ_ID,
                 DetailConstant.USE_CASE_O2O,
                 DetailConstant.CONTENT_SCENERIO
        );
         b.addProducePackage(PackageNameKey.CONTENT_FEEDS);

        return shoppingguideSdkContentService.recommend0(context, b)
            .map(response -> {
                String recType =(String) context.getParams().get("recType");
               return converters.get(recType).convert(recType,response);
            })
            .map(TacResult::newResult);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        converters=new HashMap<>(converterList.size());
        converterList.forEach(abstractConverter -> {
            converters.put(abstractConverter.getRecTypeEnum().getType(),abstractConverter);
        });
    }
}
