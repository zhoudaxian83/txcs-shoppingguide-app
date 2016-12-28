package com.tmall.wireless.tac.biz.processor;

import java.util.List;
import java.util.Map;

import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.handler.TacHandler;



public class SampleProcessor implements TacHandler<List<Map<String,Object>>> {

	public TacResult<List<Map<String,Object>>> execute(Context context)
			throws Exception {
		// 获取用户信息
		context.getUserInfo().getUserId();
		// 获取设备信息
		context.getUserInfo().getUserId();
		return null;
	}
	 

}
