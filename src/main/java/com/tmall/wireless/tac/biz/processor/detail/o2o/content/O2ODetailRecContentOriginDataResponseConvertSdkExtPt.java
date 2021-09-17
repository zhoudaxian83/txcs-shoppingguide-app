package com.tmall.wireless.tac.biz.processor.detail.o2o.content;

import java.util.List;
import java.util.Optional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import com.google.common.collect.Lists;
import com.tmall.tcls.gs.sdk.biz.extensions.item.origindata.ConvertUtil;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataResponseConvertSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentResponseConvertRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.ContentEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;
import com.tmall.tcls.gs.sdk.framework.suport.LogUtil;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.DetailConstant;
import org.apache.commons.collections4.CollectionUtils;

/**
 * @author: guichen
 * @Data: 2021/9/15
 * @Description:
 */
@SdkExtension(bizId = DetailConstant.BIZ_ID,
    useCase = DetailConstant.USE_CASE_O2O,
    scenario = DetailConstant.CONTENT_SCENERIO)
public class O2ODetailRecContentOriginDataResponseConvertSdkExtPt extends Register implements
    ContentOriginDataResponseConvertSdkExtPt {

    @Override
    public OriginDataDTO<ContentEntity> process(ContentResponseConvertRequest contentResponseConvertRequest) {
        JSONObject resObj = JSON.parseObject(contentResponseConvertRequest.getResponse());
        OriginDataDTO<ContentEntity> responseEntity = ConvertUtil.processResponse(resObj);
        List<ContentEntity> list = Lists.newArrayList();
        responseEntity.setResult(list);
        JSONArray result = resObj.getJSONArray("result");

        if (CollectionUtils.isEmpty(result)) {
            responseEntity.setErrorCode("TPP_RETURN_CONTENT_LIST_IS_EMPTY");
            return responseEntity;
        }
        for (int i = 0; i < result.size(); i++) {
            JSONObject jsonObject = result.getJSONObject(i);

            ContentEntity contentEntity = new ContentEntity();

            contentEntity.setTrack_point(responseEntity.getScm() + "." + jsonObject.getString("trackPoint"));
            contentEntity.setContentId(jsonObject.getLong("contentId"));
            contentEntity.setContentSetId(jsonObject.getString("sceneSetId"));

            //商品数量要大于6
            boolean present = Optional.ofNullable(jsonObject.getJSONArray("items"))
                .map(v -> jsonObject.getJSONArray("items"))
                .filter(CollectionUtils::isNotEmpty)
                .filter(v -> v.size() >= 6)
                .isPresent();
            if (!present) {
                LogUtil.errorCode("TPP_RECOMMEND_CONTENT_HAS_NO_ITEM", contentEntity.getContentId());
                continue;
            }
            contentEntity.setItems(null);
            list.add(contentEntity);
        }
        return responseEntity;
    }
}
