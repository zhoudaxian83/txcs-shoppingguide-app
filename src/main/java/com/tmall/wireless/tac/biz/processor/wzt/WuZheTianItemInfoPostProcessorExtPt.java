package com.tmall.wireless.tac.biz.processor.wzt;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.boot.hsf.annotation.HSFConsumer;
import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;

import com.tmall.aself.shoppingguide.client.todaycrazyv2.TodayCrazyLimitFacade;
import com.tmall.aself.shoppingguide.client.todaycrazyv2.query.ItemLimitInfoQuery;
import com.tmall.aself.shoppingguide.client.todaycrazyv2.result.ItemLimitResult;
import com.tmall.txcs.gs.framework.extensions.itemdatapost.ItemInfoPostProcessorExtPt;
import com.tmall.txcs.gs.framework.extensions.itemdatapost.ItemInfoPostProcessorResp;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.model.Response;
import com.tmall.txcs.gs.spi.recommend.RpcSpi;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: luoJunChong
 * @Date: 2021/5/18 18:57
 * description:
 */
@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.WU_ZHE_TIAN)
@Service
public class WuZheTianItemInfoPostProcessorExtPt implements ItemInfoPostProcessorExtPt {

    @Autowired
    RpcSpi rpcSpi;

    @Autowired
    TacLogger tacLogger;

    @HSFConsumer(serviceVersion = "1.0.0")
    private TodayCrazyLimitFacade todayCrazyLimitFacade;

    @Override
    public Response<ItemInfoPostProcessorResp> process(SgFrameworkContextItem sgFrameworkContextItem) {
        tacLogger.info("ItemInfoPostProcessorExtPt扩展点测试=" + JSON.toJSONString(sgFrameworkContextItem));

        ItemLimitInfoQuery itemLimitInfoQuery = new ItemLimitInfoQuery();
        itemLimitInfoQuery.setUserId(1681359525L);
        itemLimitInfoQuery.addSku(600819862645L, 623789407071L);
        ItemLimitResult itemLimitResult = todayCrazyLimitFacade.query(itemLimitInfoQuery);
        tacLogger.info("itemLimitInfoQuery返回结果=" + JSON.toJSONString(itemLimitResult));

        Map<String, Object> paramsValue = new HashMap<>(16);
        paramsValue.put("name", "itemLimitInfoQuery");
        paramsValue.put("type", "com.tmall.aself.shoppingguide.client.todaycrazyv2.query.ItemLimitInfoQuery");
        paramsValue.put("nullable", "false");
        try {
            tacLogger.info("测试返回结果begin");
            Object o = rpcSpi.invokeHsf("todayCrazyLimit", paramsValue);
            tacLogger.info("测试返回结果=" + JSON.toJSONString(o));
        } catch (Exception e) {
            e.printStackTrace();
        }
        ItemInfoPostProcessorResp itemInfoPostProcessorResp = new ItemInfoPostProcessorResp();
        return Response.success(itemInfoPostProcessorResp);
    }
}
