package com.tmall.wireless.tac.biz.processor.icon;

import com.taobao.mtop.api.agent.MtopContext;
import com.tmall.aself.shoppingguide.client.functionalgrayscale.FunGrayRequest;
import com.tmall.aself.shoppingguide.client.functionalgrayscale.FunGrayResponse;
import org.apache.commons.lang3.StringUtils;

public class GrayUtils {
    public static boolean checkGray(Long userId,String grayKey) {
        FunGrayRequest funGrayRequest = new FunGrayRequest();
        funGrayRequest.setName(grayKey);
        if(userId == null){
            userId = 0L;
        }
        funGrayRequest.setUserId(userId);
        String cna = MtopContext.getCookie("cna");
        funGrayRequest.setCna(StringUtils.isBlank(cna) ? "" : cna);
        FunGrayResponse judge = GrayUtil.judge(funGrayRequest);
        return judge != null && judge.isTargetUser();
    }
}
