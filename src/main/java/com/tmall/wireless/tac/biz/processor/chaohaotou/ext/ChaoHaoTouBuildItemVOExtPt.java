package com.tmall.wireless.tac.biz.processor.chaohaotou.ext;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSONObject;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.tmall.txcs.biz.supermarket.iteminfo.source.captain.ItemInfoBySourceDTOMain;
import com.tmall.txcs.biz.supermarket.iteminfo.source.origindate.ItemInfoBySourceDTOOrigin;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.extensions.buildvo.BuildItemVOExtPt;
import com.tmall.txcs.gs.framework.extensions.buildvo.BuildItemVoRequest;
import com.tmall.txcs.gs.framework.model.ErrorCode;
import com.tmall.txcs.gs.framework.model.ItemEntityVO;
import com.tmall.txcs.gs.model.Response;
import com.tmall.txcs.gs.model.spi.model.ItemDataDTO;
import com.tmall.txcs.gs.model.spi.model.ItemInfoBySourceDTO;
import com.tmall.txcs.gs.model.spi.model.ItemInfoDTO;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.common.VoKeyConstantApp;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: luoJunChong
 * @Date: 2021/5/14 10:26
 */
@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.CHAO_HAO_TOU)
@Service
public class ChaoHaoTouBuildItemVOExtPt implements BuildItemVOExtPt {

    Logger LOGGER = LoggerFactory.getLogger(ChaoHaoTouBuildItemVOExtPt.class);

    @Override
    public Response<ItemEntityVO> process(BuildItemVoRequest buildItemVoRequest) {
        Map<String, Object> userParams = buildItemVoRequest.getContext().getUserParams();
        String umpChannel = MapUtil.getStringWithDefault(userParams, VoKeyConstantApp.UMP_CHANNEL,
                VoKeyConstantApp.CHANNEL_KEY);
        ItemInfoDTO itemInfoDTO = buildItemVoRequest.getItemInfoDTO();
        ItemEntityVO itemEntityVO = new ItemEntityVO();
        itemEntityVO.put("contentType", 0);
        boolean hasMainSource = false;
        if (buildItemVoRequest.getItemInfoDTO() == null) {
            return Response.fail(ErrorCode.PARAMS_ERROR);
        }
        String originScm = "";
        String itemUrl = "";
        String itemDesc = null;
        boolean canBuy = false;
        boolean sellout = false;
        String specifications = "";

        Map<String, String> trackPoint = Maps.newHashMap();
        for (String s : itemInfoDTO.getItemInfos().keySet()) {
            ItemInfoBySourceDTO itemInfoBySourceDTO = itemInfoDTO.getItemInfos().get(s);
            if (itemInfoBySourceDTO instanceof ItemInfoBySourceDTOMain) {
                ItemInfoBySourceDTOMain itemInfoBySourceDTOMain = (ItemInfoBySourceDTOMain) itemInfoBySourceDTO;
                itemUrl = Optional.of(itemInfoBySourceDTOMain)
                        .map(ItemInfoBySourceDTOMain::getItemDTO)
                        .map(ItemDataDTO::getDetailUrl)
                        .orElse("");
                ItemDataDTO itemDataDTO = itemInfoBySourceDTOMain.getItemDTO();
                canBuy = itemDataDTO.isCanBuy();
                sellout = itemDataDTO.isSellOut();
                JSONObject itemPromotionResp = (JSONObject) itemDataDTO.getItemPromotionResp();
                itemDesc = buildItemDesc(itemPromotionResp);
                specifications = itemDataDTO.getSpecDetail();
                hasMainSource = true;
            }
            if (itemInfoBySourceDTO instanceof ItemInfoBySourceDTOOrigin) {
                ItemInfoBySourceDTOOrigin itemInfoBySourceDTOOrigin = (ItemInfoBySourceDTOOrigin) itemInfoBySourceDTO;
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
        itemEntityVO.put("itemType", "channelPriceNew");
        itemEntityVO.put("canBuy", canBuy);
        itemEntityVO.put("specifications", specifications);
        itemEntityVO.put("sellout", sellout);
        itemEntityVO.put("itemDesc", itemDesc);
        itemEntityVO.put(VoKeyConstantApp.UMP_CHANNEL, umpChannel);
        if (!hasMainSource) {
            return Response.fail(ErrorCode.ITEM_VO_BUILD_ERROR_HAS_NO_MAIN_SOURCE);
        }
        return Response.success(itemEntityVO);
    }

    private String buildItemDesc(JSONObject itemPromotionResp) {
        String unifyPrice = "unifyPrice";
        String price = "price";
        String showPriceKey = "showPrice";
        String chaoShiPriceKey = "chaoShiPrice";
        if (itemPromotionResp.getJSONObject(unifyPrice) != null && itemPromotionResp.getJSONObject(unifyPrice)
                .getJSONObject(showPriceKey) != null && itemPromotionResp.getJSONObject(unifyPrice).getJSONObject(
                showPriceKey).getBigDecimal(price) != null &&
                itemPromotionResp.getJSONObject(unifyPrice)
                        .getJSONObject(chaoShiPriceKey) != null && itemPromotionResp.getJSONObject(unifyPrice).getJSONObject(
                chaoShiPriceKey).getBigDecimal(price) != null) {
            BigDecimal showPrice = itemPromotionResp.getJSONObject(unifyPrice).getJSONObject(showPriceKey)
                    .getBigDecimal(price);
            BigDecimal chaoShiPrice = itemPromotionResp.getJSONObject(unifyPrice).getJSONObject(chaoShiPriceKey)
                    .getBigDecimal(price);
            String text = "专享补贴";
            return text + chaoShiPrice.subtract(showPrice) + "元";
        } else {
            return null;
        }
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
            //如果异常了就返回原来的
            LOGGER.error("scmConvertError", e);
            return scm;
        }
    }
}
