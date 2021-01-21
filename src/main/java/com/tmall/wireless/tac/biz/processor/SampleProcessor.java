package com.tmall.wireless.tac.biz.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.taobao.sm.service.NativeIndexService;
import com.taobao.sm.service.client.domain.SiteInfoQueryDO;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.dataservice.TacServiceFactory;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.handler.TacHandler;

public class SampleProcessor implements TacHandler<List<Map<String, Object>>> {

    // 日志工具类，输入日志必须使用平台提供的该类
    TacLogger tacLogger = TacServiceFactory.getLogger();

    // 商超HSF数据源
    NativeIndexService nativeIndexService = TacServiceFactory.getNativeIndexService();

	public TacResult<List<Map<String, Object>>> execute(Context context) throws Exception {
        tacLogger.info("SampleProcessor execute start...");
//        // Step1: 获取上下文参数
//		// 1、获取用户信息
//		long userId = context.getUserInfo().getUserId();       //用户id
//        String userNick = context.getUserInfo().getNick();     // 用户nick
//		// 2、获取设备信息
//		String ip  = context.getDeviceInfo().getIp();           // 设备ip
//        String channel = context.getDeviceInfo().getChannel() ; // 渠道
//        String platform = context.getDeviceInfo().getPlatform();// 平台
//        String version = context.getDeviceInfo().getVersion();  // 版本
//        String utdid = context.getDeviceInfo().getUtdid();      // utdid
//        // 3、获取自定义参数
//        String smAreaId = String.valueOf(context.get("330100")); // 获取商超areaId
//
//        // Step2: 业务逻辑
//        SiteInfoQueryDO siteInfoQueryDO = new SiteInfoQueryDO();
//        siteInfoQueryDO.setSmAreaId(smAreaId);
//        // 调用商超HSF获取城市站信息
//        //Result<Map<String, Object>> result = nativeIndexService.getSiteInfo(siteInfoQueryDO);
//
//        try {
//            int i=1/0;
//        } catch (Exception ex) {
//            // 异常日志记录
//            tacLogger.error("execute error:",ex);
//        }
//
//		List<Map<String,Object>> resultList = new ArrayList<Map<String,Object>>();
        Map<String,Object> dataMap = new HashMap<String, Object>();
        dataMap.put("imgUrl", "http://www.tmall.com/1.jpg");
        dataMap.put("imgUrl2", "http://www.tmall.com/2.jpg");
        dataMap.put("imgUr32", "http://www.tmall.com/32.jpg");
        dataMap.put("title", "慧生活");

        resultList.add(dataMap);
        return TacResult.newResult(resultList);
	}

}
