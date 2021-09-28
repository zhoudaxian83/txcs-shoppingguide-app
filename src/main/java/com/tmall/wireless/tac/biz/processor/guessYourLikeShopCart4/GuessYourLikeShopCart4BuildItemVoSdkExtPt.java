package com.tmall.wireless.tac.biz.processor.guessYourLikeShopCart4;


import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.vo.BuildItemVoRequest;
import com.tmall.tcls.gs.sdk.framework.extensions.item.vo.BuildItemVoSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.Response;
import com.tmall.txcs.gs.model.spi.model.ItemInfoBySourceDTO;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemInfoDTO;
import com.tmall.txcs.biz.supermarket.iteminfo.source.captain.ItemInfoBySourceDTOMain;
import com.tmall.txcs.biz.supermarket.iteminfo.source.origindate.ItemInfoBySourceDTOOrigin;
import com.tmall.txcs.gs.framework.model.ErrorCode;
import com.tmall.txcs.gs.model.spi.model.ItemDataDTO;

import com.google.common.collect.Maps;
import com.tmall.hades.monitor.print.HadesLogUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import java.util.Map;
import java.util.Optional;

/**
 * Created from template by 程斐斐 on 2021-09-22 18:27:55.
 * 商品VO组装 - 商品VO组装.
 */

@SdkExtension(
        bizId = "supermarket",
        useCase = "b2c",
        scenario = "guessYourLikeShopCart4"
)
public class GuessYourLikeShopCart4BuildItemVoSdkExtPt extends Register implements BuildItemVoSdkExtPt {
    @Override
    public Response<ItemEntityVO> process(BuildItemVoRequest buildItemVoRequest) {
        ItemEntityVO itemEntityVO = new ItemEntityVO();
        boolean hasMainSource = false;
        boolean canBuy = true;
        if (buildItemVoRequest == null || buildItemVoRequest.getItemInfoDTO() == null) {
            return Response.fail(ErrorCode.PARAMS_ERROR);
        }

        ItemInfoDTO itemInfoDTO = buildItemVoRequest.getItemInfoDTO();

        String originScm = "";
        String itemUrl = "";
        Map<String, String> trackPoint = Maps.newHashMap();

        for (String s : itemInfoDTO.getItemInfos().keySet()) {

            com.tmall.tcls.gs.sdk.framework.model.context.ItemInfoBySourceDTO contextItemInfoBySourceDTO = itemInfoDTO.getItemInfos().get(s);
            //类型转换
            ItemInfoBySourceDTO itemInfoBySourceDTO=JSON.parseObject(JSON.toJSONString(contextItemInfoBySourceDTO),ItemInfoBySourceDTO.class);
            if (itemInfoBySourceDTO instanceof ItemInfoBySourceDTOMain) {
                ItemInfoBySourceDTOMain itemInfoBySourceDTOMain = (ItemInfoBySourceDTOMain) itemInfoBySourceDTO;
                itemUrl = Optional.of(itemInfoBySourceDTOMain)
                        .map(ItemInfoBySourceDTOMain::getItemDTO)
                        .map(ItemDataDTO::getDetailUrl)
                        .orElse("");
                canBuy = canBuy(itemInfoBySourceDTOMain);
                hasMainSource = true;
            }
            if (itemInfoBySourceDTO instanceof ItemInfoBySourceDTOOrigin) {
                ItemInfoBySourceDTOOrigin itemInfoBySourceDTOOrigin = (ItemInfoBySourceDTOOrigin) itemInfoBySourceDTO;
                originScm = itemInfoBySourceDTOOrigin.getScm();

            }
            //            if (itemInfoBySourceDTO instanceof ItemInfoBySourceDTOInv) {
            //                canBuy = ((ItemInfoBySourceDTOInv) itemInfoBySourceDTO).isCanBuy();
            //            }
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

        if (!canBuy) {
            if(itemEntityVO.get("itemId") != null){
                HadesLogUtil.stream("guessYourLikeShopCart4")
                        .kv("GuessYourLikeShopCart4BuildItemVoSdkExtPt","process")
                        .kv("canBuy filter ItemId",itemEntityVO.get("itemId").toString())
                        .kv("Response","ITEM_VO_BUILD_ERROR_CAN_BUY_FALSE_F")
                        .info();
            }
            return Response.fail("ITEM_VO_BUILD_ERROR_CAN_BUY_FALSE_F");
        }

        if (!hasMainSource) {
            return Response.fail(ErrorCode.ITEM_VO_BUILD_ERROR_HAS_NO_MAIN_SOURCE);
        }

        if (itemEntityVO.get("smartUi") == null) {
            itemEntityVO.put("contentType", 0);

        }else if(itemEntityVO.get("smartUi") instanceof Map){
            Map<String,Object> smartUiMap = (Map<String, Object>)itemEntityVO.get("smartUi");
            if((smartUiMap.get("whitePict") == null ||  "".equals(smartUiMap.get("whitePict")))
                    && (smartUiMap.get("scenePic") == null || "".equals(smartUiMap.get("whitePict")))
                    && (smartUiMap.get("videoUrl") == null || "".equals(smartUiMap.get("whitePict")))){
                itemEntityVO.put("contentType", 0);
            }
        }

        //cff
        /**点击埋点**/
        Map<String,Object> clickParam = Maps.newHashMap();
        Map<String,Object> args = Maps.newHashMap();
        args.put("ext",0);
        args.put("spm",0);
        args.put("itemid",0);
        clickParam.put("args",args);
        clickParam.put("eventId",0);
        clickParam.put("arg1",0);
        clickParam.put("page",0);
        itemEntityVO.put("clickParam", clickParam);

        /**价格区域**/
        Map<String,Object> priceArea = Maps.newHashMap();
        priceArea.put("price",0);
        priceArea.put("originPrice",0);
        priceArea.put("pricePrefix",0);
        itemEntityVO.put("priceArea", priceArea);

        /****benefitInfo****/
        Map<String,Object> benefitInfo = Maps.newHashMap();
        benefitInfo.put("benefitGap",0);
        benefitInfo.put("benefitMaxWidth",0);
        /***文字标***/
        Map<String,Object> textBenefitInfo = Maps.newHashMap();
        textBenefitInfo.put("benefitTextColor",0);
        textBenefitInfo.put("split",0);
        textBenefitInfo.put("benefitBorderColor",0);
        textBenefitInfo.put("benefitType","text");
        textBenefitInfo.put("benefitContent",0);
        textBenefitInfo.put("benefitTextSize",0);
        textBenefitInfo.put("benefitBorderWidth",0);
        textBenefitInfo.put("benefitBorderRadius",0);
        benefitInfo.put("textBenefitInfo",textBenefitInfo);
        /***图片标***/
        Map<String,Object> pictureBenefitInfo = Maps.newHashMap();
        pictureBenefitInfo.put("split",0);
        pictureBenefitInfo.put("benefitType","image");
        pictureBenefitInfo.put("benefitContent",0);
        benefitInfo.put("pictureBenefitInfo",pictureBenefitInfo);

        itemEntityVO.put("benefitInfo", benefitInfo);
        /**标题区域**/
        Map<String,Object> titleInfo = Maps.newHashMap();
        titleInfo.put("textSize",0);
        titleInfo.put("textContent",0);
        titleInfo.put("textColor",0);
        titleInfo.put("textMaxLines",0);
        titleInfo.put("labelImgUrl",0);
        titleInfo.put("labelImgHeight",0);
        titleInfo.put("labelImgWidth",0);
        itemEntityVO.put("titleInfo", titleInfo);
        /**点击事件，跳转到该商品详情**/
        itemEntityVO.put("action", 0);
        /********/
        itemEntityVO.put("type", 0);
        /********/
        itemEntityVO.put("pageParam", 0);
        /**曝光埋点**/
        Map<String,Object> exposureParam = Maps.newHashMap();
        Map<String,Object> args1 = Maps.newHashMap();
        args1.put("ext",0);
        args1.put("spm",0);
        args1.put("itemId",0);
        exposureParam.put("args",args1);
        exposureParam.put("eventId",0);
        exposureParam.put("arg1",0);
        exposureParam.put("page",0);
        itemEntityVO.put("exposureParam", exposureParam);

        return Response.success(itemEntityVO);
    }

    private boolean canBuy(ItemInfoBySourceDTOMain itemInfoBySourceDTO) {
        Boolean canBuy = Optional.of(itemInfoBySourceDTO).map(ItemInfoBySourceDTOMain::getItemDTO).map(ItemDataDTO::isCanBuy).orElse(true);
        Boolean sellOut = Optional.of(itemInfoBySourceDTO).map(ItemInfoBySourceDTOMain::getItemDTO).map(ItemDataDTO::isSellOut).orElse(false);
        return canBuy && !sellOut;

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
