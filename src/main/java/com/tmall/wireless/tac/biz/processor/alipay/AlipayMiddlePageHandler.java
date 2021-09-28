package com.tmall.wireless.tac.biz.processor.alipay;

import com.alibaba.fastjson.JSON;
import com.alipay.recmixer.common.service.facade.model.MixerCollectRecResult;
import com.alipay.tradecsa.common.service.spi.request.MiddlePageSPIRequest;
import com.alipay.tradecsa.common.service.spi.response.MiddlePageSPIResponse;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.wireless.tac.biz.processor.alipay.service.IAliPayService;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Created by yangqing.byq on 2021/8/1.
 */
@Component
public class AlipayMiddlePageHandler extends RpmReactiveHandler<MiddlePageSPIResponse> {

    public static final String PARAM_KEY = "paramsKey";

    @Autowired
    IAliPayService aliPayServiceImpl;
    @Override
    public Flowable<TacResult<MiddlePageSPIResponse>> executeFlowable(Context context) throws Exception {
        MiddlePageSPIResponse middlePageSPIResponse = new MiddlePageSPIResponse();
        Object param = Optional.of(context).map(Context::getParams).map(m -> m.get(PARAM_KEY)).orElse(null);

        if (!(param instanceof MiddlePageSPIRequest)) {
            middlePageSPIResponse.setErrorCode("PARAMS_IS_NULL");
            return Flowable.just(TacResult.newResult(middlePageSPIResponse));
        }
        MiddlePageSPIRequest middlePageSPIRequest = (MiddlePageSPIRequest) param;

        HadesLogUtil.stream("AlipayMiddlePageHandler").kv("request", JSON.toJSONString(middlePageSPIRequest)).error();
        return aliPayServiceImpl.processMiddlePage(context, middlePageSPIRequest).map(TacResult::newResult);
    }
}
