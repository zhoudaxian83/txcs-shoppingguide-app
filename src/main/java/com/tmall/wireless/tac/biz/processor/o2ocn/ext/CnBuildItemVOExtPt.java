package com.tmall.wireless.tac.biz.processor.o2ocn.ext;

import java.util.Map;
import java.util.Optional;

import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.tmall.txcs.biz.supermarket.iteminfo.source.captain.ItemInfoBySourceDTOMain;
import com.tmall.txcs.biz.supermarket.iteminfo.source.origindate.ItemInfoBySourceDTOOrigin;
import com.tmall.txcs.gs.framework.extensions.buildvo.BuildItemVOExtPt;
import com.tmall.txcs.gs.framework.extensions.buildvo.BuildItemVoRequest;
import com.tmall.txcs.gs.framework.model.ErrorCode;
import com.tmall.txcs.gs.framework.model.ItemEntityVO;
import com.tmall.txcs.gs.model.Response;
import com.tmall.txcs.gs.model.spi.model.ItemDataDTO;
import com.tmall.txcs.gs.model.spi.model.ItemInfoBySourceDTO;
import com.tmall.txcs.gs.model.spi.model.ItemInfoDTO;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @Author: luoJunChong
 * @Date: 2021/5/14 10:26
 */
@Service
public class CnBuildItemVOExtPt implements BuildItemVOExtPt {

    Logger LOGGER = LoggerFactory.getLogger(CnBuildItemVOExtPt.class);

    @Override
    public Response<ItemEntityVO> process(BuildItemVoRequest buildItemVoRequest) {
        ItemInfoDTO itemInfoDTO = buildItemVoRequest.getItemInfoDTO();
        ItemEntityVO itemEntityVO = new ItemEntityVO();
        itemEntityVO.put("contentType", 0);
        boolean hasMainSource = false;
        if (buildItemVoRequest.getItemInfoDTO() == null) {
            return Response.fail(ErrorCode.PARAMS_ERROR);
        }
        String originScm = "";
        String itemUrl = "";

        String itemDesc = "";
        String chaoshiItemTitle = "";
        String reservePrice = "";
        String promotionPrice = "";
        Map<String, String> trackPoint = Maps.newHashMap();
        for (String s : itemInfoDTO.getItemInfos().keySet()) {
            ItemInfoBySourceDTO itemInfoBySourceDTO = itemInfoDTO.getItemInfos().get(s);
            if (itemInfoBySourceDTO instanceof ItemInfoBySourceDTOMain) {
                ItemInfoBySourceDTOMain itemInfoBySourceDTOMain = (ItemInfoBySourceDTOMain)itemInfoBySourceDTO;
                itemUrl = Optional.of(itemInfoBySourceDTOMain)
                    .map(ItemInfoBySourceDTOMain::getItemDTO)
                    .map(ItemDataDTO::getDetailUrl)
                    .orElse("");
                ItemDataDTO itemDataDTO = itemInfoBySourceDTOMain.getItemDTO();
                JSONObject itemPromotionResp = (JSONObject)itemDataDTO.getItemPromotionResp();
                hasMainSource = true;
                JSONObject unifyPrice = itemPromotionResp.getJSONObject("unifyPrice");
                itemDesc = buildItemDesc(itemPromotionResp);
                if (unifyPrice != null) {
                    promotionPrice = getPromotionPrice(unifyPrice);
                    reservePrice = getReservePrice(unifyPrice);
                    //reservePrice , ??????chaoshiPrice??????showPrice
                    reservePrice = "".equals(reservePrice) ? promotionPrice : reservePrice;
                }
            }
            if (itemInfoBySourceDTO instanceof ItemInfoBySourceDTOOrigin) {
                ItemInfoBySourceDTOOrigin itemInfoBySourceDTOOrigin = (ItemInfoBySourceDTOOrigin)itemInfoBySourceDTO;
                originScm = itemInfoBySourceDTOOrigin.getScm();

            }
            Map<String, String> scmKeyValue = itemInfoBySourceDTO.getScmKeyValue();
            if (MapUtils.isNotEmpty(scmKeyValue)) {
                trackPoint.putAll(scmKeyValue);
            }

            itemEntityVO.putAll(itemInfoBySourceDTO.getItemInfoVO());

        }

        String scm = processScm(originScm, trackPoint);
        itemUrl = itemUrl + "&scm=" + scm;
        itemEntityVO.put("scm", scm);
        itemEntityVO.put("itemUrl", itemUrl);
        //???????????????begin

        chaoshiItemTitle = itemEntityVO.getString("title");
        itemEntityVO.put("itemDesc", itemDesc);
        itemEntityVO.put("reservePrice", reservePrice);
        itemEntityVO.put("promotionPrice", promotionPrice);
        itemEntityVO.put("promotionName", itemDesc);
        itemEntityVO.put("chaoshiItemTitle", chaoshiItemTitle);
        //???????????????end
        if (!hasMainSource) {
            return Response.fail(ErrorCode.ITEM_VO_BUILD_ERROR_HAS_NO_MAIN_SOURCE);
        }
        return Response.success(itemEntityVO);
    }

    private String buildItemDesc(JSONObject itemPromotionResp) {
        String itemDesc = "????????????";
        JSONArray atmosphereList = itemPromotionResp.getJSONArray("atmosphereList");
        if (atmosphereList == null || atmosphereList.size() == 0) {
            return itemDesc;
        }
        JSONObject text = ((JSONObject)atmosphereList.get(0)).getJSONObject("text");
        if (text == null) {
            return itemDesc;
        }
        String content = text.getString("content");
        return content == null ? itemDesc : content;
    }

    private String getReservePrice(JSONObject unifyPrice) {
        JSONObject chaoshiPrice = unifyPrice.getJSONObject("chaoshiPrice");
        if (chaoshiPrice == null) {
            return "";
        }
        String price = chaoshiPrice.getString("price");
        price = price == null ? "" : price;
        return price;
    }

    private String getPromotionPrice(JSONObject unifyPrice) {
        JSONObject showPrice = unifyPrice.getJSONObject("showPrice");
        if (showPrice == null) {
            return "";
        }
        String price = showPrice.getString("price");
        price = price == null ? "" : price;
        return price;
    }

    private String processScm(String originScm, Map<String, String> scmKeyValue) {

        if (MapUtils.isEmpty(scmKeyValue)) {
            return originScm;
        }
        String addScm = Joiner.on("_").withKeyValueSeparator("-").join(scmKeyValue);

        return scmConvert(originScm, addScm);

    }

    public String scmConvert(String scm, String add) {
        try {

            if (StringUtils.isBlank(scm)) {
                return scm;
            }

            int index = scm.lastIndexOf("-");
            String prefixScm = scm.substring(0, index);
            String suffixScm = scm.substring(index);

            return prefixScm + "_" + add + suffixScm;
        } catch (Exception e) {
            //?????????????????????????????????
            LOGGER.error("scmConvertError", e);
            return scm;
        }
    }

}
