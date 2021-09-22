package com.tmall.wireless.tac.biz.processor.detail.o2o.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.txcs.biz.supermarket.extpt.buildvo.DefaultBuildItemVOExtPt;
import com.tmall.txcs.gs.framework.extensions.buildvo.BuildItemVoRequest;
import com.tmall.txcs.gs.framework.model.ItemEntityVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.model.Response;
import com.tmall.wireless.tac.biz.processor.detail.common.config.DetailSwitch;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.DetailConstant;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @author: guichen
 * @Data: 2021/9/22
 * @Description:
 */
@SdkExtension(bizId = DetailConstant.BIZ_ID,
    useCase = DetailConstant.USE_CASE_O2O,
    scenario = DetailConstant.ITEM_SCENERIO)
public class O2ODetailBuildItemVOExtPt extends DefaultBuildItemVOExtPt {
    @Override
    public Response<ItemEntityVO> process(BuildItemVoRequest buildItemVoRequest) {
        Response<ItemEntityVO> process = super.process(buildItemVoRequest);
        if (process.isSuccess() && Objects.nonNull(process.getValue())) {
            String s = processDetailParams(buildItemVoRequest.getContext().getRequestParams());
            if(StringUtils.isNotEmpty(s)){
                String itemUrl = (String)process.getValue().get("itemUrl");
                process.getValue().put("itemUrl", itemUrl + s);
            }
        }

        return process;
    }

    private String processDetailParams(Map<String, Object> params) {
        StringBuilder url = new StringBuilder();
        if (CollectionUtils.isNotEmpty(DetailSwitch.detailThoughParams) &&
            MapUtils.isNotEmpty(params)) {

            DetailSwitch.detailThoughParams.stream().filter(params::containsKey)
                .filter(v->Objects.nonNull(params.get(v)))
                .forEach(v -> url.append("&").append(v).append("=").append((String)params.get(v)));
        }

        return url.toString();
    }
}
