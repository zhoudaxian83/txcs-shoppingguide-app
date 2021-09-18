package com.tmall.wireless.tac.biz.processor.detail.common.convert;

import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecContentResultVO;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;

/**
 * @author: guichen
 * @Data: 2021/9/14
 * @Description:
 */
public class ResultConverter  {

    public static TacResult convertToTacResult(SgFrameworkResponse response, Context context){
        if(response.isSuccess()){
            String recType = (String)context.getParams().get("recType");
            return TacResult.newResult(DetailConverterFactory.instance.getConverter(recType).convert(
                context,response));
        }

        return TacResult.errorResult(response.getErrorCode(),response.getErrorMsg());

    }
}
