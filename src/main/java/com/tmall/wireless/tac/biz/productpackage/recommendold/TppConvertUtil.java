package com.tmall.wireless.tac.biz.productpackage.recommendold;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.tcls.gs.sdk.framework.model.context.*;
import com.tmall.tcls.gs.sdk.framework.suport.LogUtil;
import com.tmall.txcs.gs.model.model.dto.RecommendResponseEntity;
import com.tmall.txcs.gs.model.model.dto.tpp.RecommendContentEntityDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TppConvertUtil {

    private static Map<String, String> tppO2oTypeConvertMap;

    static {
        tppO2oTypeConvertMap = Maps.newHashMap();
        tppO2oTypeConvertMap.putIfAbsent("one_hour", com.tmall.txcs.gs.model.item.O2oType.O2OOneHour.name());
        tppO2oTypeConvertMap.putIfAbsent("half_day", com.tmall.txcs.gs.model.item.O2oType.O2OHalfDay.name());
        tppO2oTypeConvertMap.putIfAbsent("next_day", com.tmall.txcs.gs.model.item.O2oType.O2ONextDay.name());
        tppO2oTypeConvertMap.putIfAbsent("B2C", com.tmall.txcs.gs.model.item.O2oType.B2C.name());
    }

    public static OriginDataDTO<ItemEntity> processItemEntity(String res) {
        JSONObject resObj = JSON.parseObject(res);
        OriginDataDTO<ItemEntity> responseEntity = processResponse(resObj);

        JSONArray result = resObj.getJSONArray("result");
        if (CollectionUtils.isEmpty(result)) {
            responseEntity.setErrorCode("TPP_RETURN_ITEM_LIST_IS_EMPTY");
            return responseEntity;
        }
        List<ItemEntity> itemEntityList = processItemEntityJson(result, responseEntity.getScm());

        responseEntity.setResult(itemEntityList);

        return responseEntity;
    }
    public static OriginDataDTO<ContentEntity> processContentEntity(String res) {

        JSONObject resObj = JSON.parseObject(res);
        OriginDataDTO<ContentEntity> responseEntity = processResponse(resObj);
        List<ContentEntity> list = Lists.newArrayList();
        responseEntity.setResult(list);
        JSONArray result = resObj.getJSONArray("result");

        if (org.apache.commons.collections4.CollectionUtils.isEmpty(result)) {
            responseEntity.setErrorCode("TPP_RETURN_CONTENT_LIST_IS_EMPTY");
            return responseEntity;
        }
        for (int i = 0; i < result.size(); i++) {
            JSONObject jsonObject = result.getJSONObject(i);

            ContentEntity contentEntity = new ContentEntity();

            contentEntity.setRn(jsonObject.getInteger("rn"));
            contentEntity.setTrack_point(responseEntity.getScm() + "." + jsonObject.getString("trackPoint"));
            contentEntity.setContentId(jsonObject.getLong("contentId"));
            JSONArray itemSets = jsonObject.getJSONArray("itemSets");
            if(itemSets != null){
                List<String> strings = JSON.parseObject(JSON.toJSONString(itemSets),
                        new TypeReference<List<String>>() {});
                contentEntity.setItemSets(strings);
            }

            if (jsonObject.getJSONArray("items") == null || org.apache.commons.collections.CollectionUtils.isEmpty(jsonObject.getJSONArray("items"))) {
                LogUtil.errorCode("TPP_RECOMMEND_CONTENT_HAS_NO_ITEM", contentEntity.getContentId());
                continue;
            }
            contentEntity.setItems(processItemEntityJson(jsonObject.getJSONArray("items"), responseEntity.getScm()));
            list.add(contentEntity);
        }
        return responseEntity;
    }

    public static List<ItemEntity> processItemEntityJson(JSONArray itemJsonList, String scm) {

        List<ItemEntity> list = Lists.newArrayList();

        if (itemJsonList == null) {
            return list;
        }

        for (int i = 0; i < itemJsonList.size(); i++) {


            JSONObject jsonObject = itemJsonList.getJSONObject(i);

            ItemEntity itemEntity = new ItemEntity();

            itemEntity.setItemId(jsonObject.getLong("itemId"));
            itemEntity.setBrandId(jsonObject.getString("brandId"));
            itemEntity.setCateId(jsonObject.getString("brandId"));


            itemEntity.setBizType(BizType.SM.getCode());

            String commerceModel = jsonObject.getString("commerceModel");
            itemEntity.setBusinessType(commerceModel);

            String o2oTypeFromTpp = jsonObject.getString("o2oType");
            String o2oType = StringUtils.isEmpty(o2oTypeFromTpp) ? com.tmall.tcls.gs.sdk.framework.model.context.O2oType.B2C.name() :
                    (StringUtils.isEmpty(tppO2oTypeConvertMap.get(o2oTypeFromTpp)) ?
                            O2oType.B2C.name() : tppO2oTypeConvertMap.get(o2oTypeFromTpp));
            itemEntity.setO2oType(o2oType);

            itemEntity.setTrack_point(jsonObject.getString("trackPoint"));
            itemEntity.setTrack_point(scm + "." + itemEntity.getTrack_point());
            list.add(itemEntity);
        }
        return list;

    }

    public static <T extends EntityDTO> OriginDataDTO<T> processResponse(JSONObject resObj) {
        OriginDataDTO<T> originDataDTO = new OriginDataDTO<T>();

        resObj.getString("pvid");
        Boolean hasMore = Optional.ofNullable(resObj.getInteger("hasMore"))
                .map(integer -> integer.equals(1)).orElse(false);
        originDataDTO.setHasMore(hasMore);
        originDataDTO.setScm(resObj.getString("scm"));
        originDataDTO.setTppBuckets(resObj.getString("tpp_buckets"));

        return originDataDTO;
    }
}
