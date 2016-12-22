package com.tmall.wireless.tac.biz.processor;


import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.handler.TacHandler;

import java.util.HashMap;
import java.util.Map;

public class SampleProcessor implements TacHandler {

    public TacResult<Map<String, Object>> execute(Context context) throws Exception {

        Map<String, Object> resutlMap = new HashMap<String, Object>();
        TacResult<Map<String, Object>> tacResult = TacResult.newResult(resutlMap);

        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("userId", context.getUserInfo().getUserId());
        dataMap.put("version", context.getDeviceInfo().getVersion());
        dataMap.put("platform", context.getDeviceInfo().getPlatform());
        dataMap.put("utdid", context.getDeviceInfo().getUtdid());

        dataMap.put("imgUrl", "http://www.tmall.com/1.jpg");

        dataMap.put("imgUrl2", "http://www.tmall.com/2.jpg");

        resutlMap.put("data", dataMap);

        tacResult.setData(resutlMap);

        return tacResult;
    }

}
