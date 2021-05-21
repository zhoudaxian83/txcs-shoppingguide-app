package com.tmall.wireless.tac.biz.processor.wzt;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import com.tmall.wireless.tac.biz.processor.wzt.model.ItemLimitDTO;

/**
 * @Author: luoJunChong
 * @Date: 2021/5/20 16:25
 * description:
 */
public class Test {
    public static void main(String[] args) {
        String s = "{\"userParams-test-1\":\"userParams-test-1\","
            + "\"itemLimitResult\":{\"607615049047\":[{\"itemId\":607615049047,\"totalLimit\":100,"
            + "\"userUsedCount\":0,\"userLimit\":10,\"class\":\"com.tmall.aself.shoppingguide.client.todaycrazyv2"
            + ".result.ItemLimitDTO\",\"skuId\":4435453754419,\"usedCount\":0}]}}";

        JSONObject jsonObject = JSONObject.parseObject(s);
        Map<String, Object> userParams = (Map<String, Object>)jsonObject;
        Map<Long, List<ItemLimitDTO>> limitResult  = (Map<Long, List<ItemLimitDTO>>)userParams.get("itemLimitResult");
        List<ItemLimitDTO> C=  limitResult.get(607615049047L);
        userParams.get("");
        JSONObject jsonObject1 = (JSONObject)userParams.get("itemLimitResult");
        System.out.println(jsonObject1);

    }
}
