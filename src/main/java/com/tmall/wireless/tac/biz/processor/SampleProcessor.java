package com.tmall.wireless.tac.biz.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.handler.TacHandler;

public class SampleProcessor implements TacHandler<List<Map<String, Object>>> {

	public TacResult<List<Map<String, Object>>> execute(Context context)
			throws Exception {

		// 获取用户信息
		context.getUserInfo().getUserId();
		// 获取设备信息
		context.getUserInfo().getUserId();
		
		List<Map<String,Object>> resultList = new ArrayList<Map<String,Object>>();
		
        Map<String,Object> dataMap = new HashMap<String, Object>();

        dataMap.put("imgUrl", "http://www.tmall.com/1.jpg");
        dataMap.put("imgUrl2", "http://www.tmall.com/2.jpg");
        dataMap.put("imgUr32", "http://www.tmall.com/32.jpg");
        dataMap.put("title", "慧生活");

        resultList.add(dataMap);
        //return tacResult;
        return TacResult.newResult(resultList);
	}

}
