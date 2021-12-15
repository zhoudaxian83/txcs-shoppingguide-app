package com.tmall.wireless.tac.biz.processor.processtemplate.common.service;

import com.alibaba.fastjson.JSON;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.taobao.tair.ResultCode;
import com.tmall.txcs.gs.spi.factory.CommonFactoryAbs;
import com.tmall.txcs.gs.spi.recommend.TairFactorySpi;
import com.tmall.txcs.gs.spi.recommend.TairManager;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.ProcessTemplateContext;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.config.ProcessTemplateSwitch;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend.ItemSetRecommendModel;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.service.model.recommend.RecommendModel;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.util.MetricsUtil;
import com.tmall.wireless.tac.client.domain.Enviroment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProcessTemplateTppBottomService {

    private static final String WRITE_TPP_BOTTOM_ACTION = "writeTppBottom";
    private static final String READ_TPP_BOTTOM_ACTION = "readTppBottom";
    private static final String TPP_BOTTOM_COUNT_ACTION = "tppBottomCount";
    private static final String TPP_BOTTOM_HIT_ACTION = "tppBottomHit";
    @Autowired
    TairFactorySpi tairFactorySpi;
    @Autowired
    CommonFactoryAbs commonFactoryAbs;
    @Autowired
    Enviroment enviroment;

    public void writeBottomData(ProcessTemplateContext context, String bottomKey, RecommendModel recommendModel) {
        if(!satisfyTppBottom(context, bottomKey)) {
            return;
        }
        String cacheKey = getCacheKey(context, bottomKey);
        long startTime = System.currentTimeMillis();
        try {
            //defaultTair的namespace是184
            TairManager defaultTair = tairFactorySpi.getDefaultTair();
            if (defaultTair == null || defaultTair.getMultiClusterTairManager() == null) {
                MetricsUtil.tppBottomFail(WRITE_TPP_BOTTOM_ACTION, context, "tairManager is null", cacheKey);
                return;
            }
            ResultCode resultCode = defaultTair.getMultiClusterTairManager().put(defaultTair.getNameSpace(), cacheKey, JSON.toJSONString(recommendModel), 0, 60 * 60 * 24);
            if (!resultCode.isSuccess()) {
                MetricsUtil.tppBottomFail(WRITE_TPP_BOTTOM_ACTION, context, resultCode.getMessage(), cacheKey);
            } else {
                MetricsUtil.tppBottomSuccess(WRITE_TPP_BOTTOM_ACTION, context, startTime, cacheKey);
            }
        } catch (Exception e) {
            MetricsUtil.tppBottomException(WRITE_TPP_BOTTOM_ACTION, context, e, cacheKey);
        }
    }

    public RecommendModel readBottomData(ProcessTemplateContext context, String bottomKey, Class clazz) {
        long startTime = System.currentTimeMillis();
        String cacheKey = getCacheKey(context, bottomKey);
        try {
            TairManager defaultTair = tairFactorySpi.getDefaultTair();
            if (defaultTair == null || defaultTair.getMultiClusterTairManager() == null) {
                MetricsUtil.tppBottomFail(READ_TPP_BOTTOM_ACTION, context, "tairManager is null", cacheKey);
                return null;
            }
            Result<DataEntry> dataEntryResult = defaultTair.getMultiClusterTairManager().get(defaultTair.getNameSpace(), cacheKey);
            if (dataEntryResult.isSuccess() && dataEntryResult.getValue() != null
                    && dataEntryResult.getValue().getValue() != null) {
                MetricsUtil.tppBottomSuccess(READ_TPP_BOTTOM_ACTION, context, startTime, cacheKey);
                Object bottomData = dataEntryResult.getValue().getValue();
                if(clazz == ItemSetRecommendModel.class) {
                    return JSON.parseObject(String.valueOf(bottomData), ItemSetRecommendModel.class);
                } else {
                    MetricsUtil.tppBottomFail(READ_TPP_BOTTOM_ACTION, context, "unsupported recommend model", cacheKey);
                }
            } else {
                MetricsUtil.tppBottomFail(READ_TPP_BOTTOM_ACTION, context, "value is null", cacheKey);
                return null;
            }
        } catch (Exception e) {
            MetricsUtil.tppBottomException(READ_TPP_BOTTOM_ACTION, context, e, cacheKey);
        }
        return null;
    }

    private String getCacheKey(ProcessTemplateContext context, String bottomKey) {
        if(enviroment != null && enviroment.isOnline()) {
            return "tpp_bottom_" + context.getCurrentResourceId() + "_" + context.getCurrentScheduleId() + "_" + bottomKey;
        }
        return "pre_tpp_bottom_" + context.getCurrentResourceId() + "_" + context.getCurrentScheduleId() + "_" + bottomKey;
    }

    private String getCounterKey(ProcessTemplateContext context, String bottomKey) {
        if(enviroment != null && enviroment.isOnline()) {
            return "tpp_counter_" + context.getCurrentResourceId() + "_" + context.getCurrentScheduleId() + "_" + bottomKey;
        }
        return "pre_tpp_counter_" + context.getCurrentResourceId() + "_" + context.getCurrentScheduleId() + "_" + bottomKey;
    }

    public boolean satisfyTppBottom(ProcessTemplateContext context, String bottomKey) {
        if(!ProcessTemplateSwitch.openTPPSampleBottom) {
            return false;
        }
        String cacheKey = getCounterKey(context, bottomKey);
        TairManager defaultTair = tairFactorySpi.getDefaultTair();
        if (defaultTair == null || defaultTair.getMultiClusterTairManager() == null) {
            MetricsUtil.tppBottomFail(TPP_BOTTOM_COUNT_ACTION, context, "tairManager is null", cacheKey);
            return false;
        }
        long startTime = System.currentTimeMillis();
        try {
            Result<Integer> counterResult = defaultTair.getMultiClusterTairManager().incr(defaultTair.getNameSpace(), cacheKey, 1, -1, 60 * 60 * 24);
            if (counterResult.isSuccess() && counterResult.getValue() != null) {
                MetricsUtil.tppBottomSuccess(TPP_BOTTOM_COUNT_ACTION, context, startTime, cacheKey, String.valueOf(counterResult.getValue()));
                boolean satisfy = counterResult.getValue() % ProcessTemplateSwitch.tppBottomCounterCycle == 0;
                if (satisfy) {
                    MetricsUtil.tppBottomSuccess(TPP_BOTTOM_HIT_ACTION, context, startTime, cacheKey, String.valueOf(counterResult.getValue()));
                }
                return satisfy;
            } else {
                MetricsUtil.tppBottomFail(TPP_BOTTOM_COUNT_ACTION, context, "value is null", cacheKey);
            }
        } catch (Exception e) {
            MetricsUtil.tppBottomException(TPP_BOTTOM_COUNT_ACTION, context, e, cacheKey);
        }
        return false;
    }

}
