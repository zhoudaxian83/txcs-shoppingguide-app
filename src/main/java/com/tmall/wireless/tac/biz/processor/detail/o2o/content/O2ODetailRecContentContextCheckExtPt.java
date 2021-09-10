package com.tmall.wireless.tac.biz.processor.detail.o2o.content;

import java.util.Set;

import com.alibaba.cola.extension.Extension;
import com.alibaba.metrics.StringUtils;

import com.google.common.collect.Sets;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.context.ContentContextCheckSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.item.contextcheck.ContextCheckResult;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.DetailConstant;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecommendRequest;
import com.tmall.wireless.tac.client.domain.Context;
import org.springframework.stereotype.Service;

/**
 * @author: guichen
 * @Data: 2021/9/10
 * @Description:
 */
@Extension(bizId = DetailConstant.BIZ_ID,
    useCase = DetailConstant.USE_CASE_O2O,
    scenario = DetailConstant.CONTENT_SCENERIO)
@Service
public class O2ODetailRecContentContextCheckExtPt extends Register implements ContentContextCheckSdkExtPt {
    @Override
    public ContextCheckResult process(Context context) {
        ContextCheckResult contextCheckResult = new ContextCheckResult();
        Set<String> checkParams = Sets.newHashSet("recType", "detailItemId", "smAreaId", "locType",
            "pageSize", "index");

        StringBuilder errorMsg = new StringBuilder();
        checkParams.forEach(v -> {
            String s = checkParam(context, v);
            if (s != null) {
                errorMsg.append(s);
            }
        });

        if (errorMsg.length() > 0) {
            contextCheckResult.setErrorMsg(errorMsg.toString());
            contextCheckResult.setSuccess(false);
            return contextCheckResult;
        }

        //将转换后的模型写入
        DetailRecommendRequest detailRecommendRequest=new DetailRecommendRequest(context.getParams());
        context.getParams().put(DetailConstant.REQUEST,detailRecommendRequest);

        contextCheckResult.setSuccess(true);
        return contextCheckResult;
    }

    private String checkParam(Context context, String key) {

        if (!context.getParams().containsKey(key)) {
            return key + " is invalid;";
        }

        Object o = context.getParams().get(key);
        boolean checkResult = true;
        if (o instanceof String) {
            checkResult = StringUtils.isNotBlank((String)o);
        }

        if(o instanceof Integer ){
            checkResult = (Integer)o > 0;
        }

        if(o instanceof Long ){
            checkResult = (Long)o > 0;
        }

        if(!checkResult){
            return key + " is invalid;";
        }

        return null;
    }
}
