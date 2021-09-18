package com.tmall.wireless.tac.biz.processor.detail.o2o.item;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.tcls.gs.sdk.biz.extensions.item.origindata.ConvertUtil;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataResponseConvertSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ResponseConvertRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.O2oType;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.DetailConstant;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.TppLocTypeEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author: guichen
 * @Data: 2021/9/18
 * @Description:
 */
@SdkExtension(bizId = DetailConstant.BIZ_ID,
    useCase = DetailConstant.USE_CASE_O2O,
    scenario = DetailConstant.ITEM_SCENERIO)
public class O2ODetailItemOriginDataResponseConvertSdkExtPt extends Register
    implements ItemOriginDataResponseConvertSdkExtPt {

    private static Map<String, String> tppO2oTypeConvertMap;
    
    static {
        tppO2oTypeConvertMap = Maps.newHashMap();
        tppO2oTypeConvertMap.putIfAbsent(TppLocTypeEnum.one_hour.getLocType(), O2oType.O2OOneHour.name());
        tppO2oTypeConvertMap.putIfAbsent(TppLocTypeEnum.half_day.getLocType(), O2oType.O2OHalfDay.name());
        tppO2oTypeConvertMap.putIfAbsent(TppLocTypeEnum.next_day.getLocType(),O2oType.O2ONextDay.name());
        tppO2oTypeConvertMap.putIfAbsent(TppLocTypeEnum.B2C.getLocType(), O2oType.B2C.name());
    }

    @Override
    public OriginDataDTO<ItemEntity> process(ResponseConvertRequest responseConvertRequest) {
        return processItemEntity(responseConvertRequest.getResponse());
    }

    private OriginDataDTO<ItemEntity> processItemEntity(String res) {

        JSONObject resObj = JSON.parseObject(res);
        OriginDataDTO<ItemEntity> responseEntity = ConvertUtil.processResponse(resObj);

        String scm = responseEntity.getScm();
        List<ItemEntity> itemEntityList = processItemEntityJson(resObj.getJSONArray("result"));
        if (CollectionUtils.isEmpty(itemEntityList)) {
            responseEntity.setErrorCode("TPP_ITEM_LIST_IS_EMPTY");
            responseEntity.setErrorMsg("TPP_ITEM_LIST_IS_EMPTY");
            return responseEntity;
        }
        responseEntity.setResult(itemEntityList);
        return responseEntity;
    }


    private List<ItemEntity> processItemEntityJson(JSONArray itemJsonList) {

        List<ItemEntity> list = Lists.newArrayList();

        if (itemJsonList == null) {
            return list;
        }

        for (int i = 0; i < itemJsonList.size(); i++) {

            JSONObject jsonObject = itemJsonList.getJSONObject(i);

            ItemEntity itemEntity = new ItemEntity();

            itemEntity.setItemId(jsonObject.getLong("itemId"));

            String tppLocType = jsonObject.getString("locType");
            String o2oType = StringUtils.isEmpty(tppLocType) ? O2oType.B2C.name() :
                (StringUtils.isEmpty(tppO2oTypeConvertMap.get(tppLocType)) ?
                    O2oType.B2C.name() : tppO2oTypeConvertMap.get(tppLocType));
            itemEntity.setO2oType(o2oType);

            itemEntity.setTrack_point(jsonObject.getString("track_point"));

            list.add(itemEntity);
        }
        return list;

    }
}