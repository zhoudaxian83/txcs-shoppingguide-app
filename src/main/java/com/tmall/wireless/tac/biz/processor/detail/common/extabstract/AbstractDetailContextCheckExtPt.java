package com.tmall.wireless.tac.biz.processor.detail.common.extabstract;

import java.util.Set;

import com.alibaba.metrics.StringUtils;

import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.contextcheck.ContextCheckResult;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.DetailConstant;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecommendRequest;
import com.tmall.wireless.tac.client.domain.Context;

/**
 * @author: guichen
 * @Data: 2021/9/13
 * @Description:
 */
public class AbstractDetailContextCheckExtPt extends Register {

    public ContextCheckResult process(Context context, Set<String> checkParams) {
        ContextCheckResult contextCheckResult = new ContextCheckResult();

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
