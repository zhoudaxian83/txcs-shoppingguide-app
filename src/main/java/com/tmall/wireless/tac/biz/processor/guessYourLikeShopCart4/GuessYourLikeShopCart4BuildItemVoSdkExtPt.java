package com.tmall.wireless.tac.biz.processor.guessYourLikeShopCart4;


import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import com.tmall.aselfcaptain.item.model.ItemDTO;
import com.tmall.tcls.gs.sdk.biz.extensions.item.vo.DefaultBuildItemVoSdkExtPt;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.vo.BuildItemVoRequest;
import com.tmall.tcls.gs.sdk.framework.extensions.item.vo.BuildItemVoSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.Response;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemInfoBySourceDTO;
import com.tmall.tcls.gs.sdk.framework.model.context.O2oType;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemInfoDTO;
import com.tmall.tcls.gs.sdk.sm.iteminfo.bysource.captain.ItemInfoBySourceCaptainDTO;
import com.tmall.tcls.gs.sdk.sm.iteminfo.bysource.tpp.ItemInfoBySourceTppDTO;
import com.tmall.txcs.biz.supermarket.iteminfo.source.captain.ItemInfoBySourceDTOMain;
import com.tmall.txcs.biz.supermarket.iteminfo.source.origindate.ItemInfoBySourceDTOOrigin;
import com.tmall.txcs.gs.framework.model.ErrorCode;
import com.tmall.txcs.gs.model.spi.model.ItemDataDTO;

import com.google.common.collect.Maps;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Iterator;
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

    @Autowired
    TacLoggerImpl tacLogger;
    @Override
    public Response<ItemEntityVO> process(BuildItemVoRequest buildItemVoRequest) {

        try {
            ItemEntityVO entityVO = new ItemEntityVO();
            if (buildItemVoRequest != null && buildItemVoRequest.getItemInfoDTO() != null) {
                ItemInfoDTO itemInfoDTO = buildItemVoRequest.getItemInfoDTO();
                entityVO.setItemId((Long)Optional.of(itemInfoDTO).map(ItemInfoDTO::getItemEntity).map(ItemEntity::getItemId).orElse(0L));
                entityVO.setO2oType((String)Optional.of(itemInfoDTO).map(ItemInfoDTO::getItemEntity).map(ItemEntity::getO2oType).orElse(O2oType.B2C.name()));
                String originScm = "";
                String itemUrl = "";
                Map<String, String> trackPoint = Maps.newHashMap();

                com.tmall.tcls.gs.sdk.framework.model.context.ItemInfoBySourceDTO itemInfoBySourceDTO;
                for(Iterator var7 = itemInfoDTO.getItemInfos().keySet().iterator(); var7.hasNext(); entityVO.putAll(this.getItemVoMap(itemInfoBySourceDTO))) {
                    String s = (String)var7.next();
                    itemInfoBySourceDTO = (ItemInfoBySourceDTO)itemInfoDTO.getItemInfos().get(s);
                    if (itemInfoBySourceDTO instanceof ItemInfoBySourceCaptainDTO) {
                        ItemInfoBySourceCaptainDTO itemInfoBySourceCaptainDTO = (ItemInfoBySourceCaptainDTO)itemInfoBySourceDTO;
                        itemUrl = (String)Optional.of(itemInfoBySourceCaptainDTO).map(ItemInfoBySourceCaptainDTO::getItemDTO).map(ItemDTO::getDetailUrl).orElse("");
                    }

                    if (itemInfoBySourceDTO instanceof ItemInfoBySourceTppDTO) {
                        ItemInfoBySourceTppDTO itemInfoBySourceDTOOrigin = (ItemInfoBySourceTppDTO)itemInfoBySourceDTO;
                        originScm = itemInfoBySourceDTOOrigin.getScm();
                    }

                    Map<String, String> scmKeyValue = itemInfoBySourceDTO.getScmKeyValue();
                    if (MapUtils.isNotEmpty(scmKeyValue)) {
                        trackPoint.putAll(scmKeyValue);
                    }
                }

                String scm = this.processScm(originScm, trackPoint);
                itemUrl = itemUrl + "&scm=" + scm;
                entityVO.put("scm", scm);
                entityVO.put("itemUrl", itemUrl);
            } else {
                return Response.fail("PARAMS_ERROR");
            }
            tacLogger.info("VO重写开始");
            /*Response<ItemEntityVO> entityVOResponse = super.process(buildItemVoRequest);

            if(!entityVOResponse.isSuccess()){
                return entityVOResponse;
            }
            ItemEntityVO entityVO = entityVOResponse.getValue();*/

            //cff
            ItemEntityVO itemEntityVO = new ItemEntityVO();
            itemEntityVO.put("scm", entityVO.get("scm"));
            /**点击埋点**/
            Map<String,Object> clickParam = Maps.newHashMap();
            Map<String,Object> args = Maps.newHashMap();
            args.put("ext",0);
            args.put("spm",0);
            args.put("itemid",entityVO.get("itemId"));
            clickParam.put("args",args);
            clickParam.put("eventId",0);
            clickParam.put("arg1",0);
            clickParam.put("page",0);
            itemEntityVO.put("clickParam", clickParam);

            /**价格区域**/
            Map<String,Object> priceArea = Maps.newHashMap();
            priceArea.put("price",entityVO.get("showPrice"));
            priceArea.put("originPrice",0);
            priceArea.put("pricePrefix",0);
            itemEntityVO.put("priceArea", priceArea);

            /****benefitInfo****//*
            Map<String,Object> benefitInfo = Maps.newHashMap();
            benefitInfo.put("benefitGap",0);
            benefitInfo.put("benefitMaxWidth",0);
            *//***文字标***//*
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
            *//***图片标***//*
            Map<String,Object> pictureBenefitInfo = Maps.newHashMap();
            pictureBenefitInfo.put("split",0);
            pictureBenefitInfo.put("benefitType","image");
            pictureBenefitInfo.put("benefitContent",0);
            benefitInfo.put("pictureBenefitInfo",pictureBenefitInfo);

            itemEntityVO.put("benefitInfo", benefitInfo);*/
            /**标题区域**/
            Map<String,Object> titleInfo = Maps.newHashMap();
            titleInfo.put("textSize",0);
            titleInfo.put("textContent",entityVO.get("title"));
            titleInfo.put("textColor",0);
            titleInfo.put("textMaxLines",0);
            titleInfo.put("labelImgUrl",0);
            titleInfo.put("labelImgHeight",0);
            titleInfo.put("labelImgWidth",0);
            itemEntityVO.put("titleInfo", titleInfo);
            /**点击事件，跳转到该商品详情**/
            itemEntityVO.put("action", entityVO.get("itemUrl"));
            /********/
            itemEntityVO.put("type", 0);
            /********/
            itemEntityVO.put("pageParam", 0);
            /**曝光埋点**/
            Map<String,Object> exposureParam = Maps.newHashMap();
            Map<String,Object> args1 = Maps.newHashMap();
            args1.put("ext",0);
            args1.put("spm",0);
            args1.put("itemId",entityVO.get("itemId"));
            exposureParam.put("args",args1);
            exposureParam.put("eventId",0);
            exposureParam.put("arg1",0);
            exposureParam.put("page",0);
            itemEntityVO.put("exposureParam", exposureParam);

            itemEntityVO.put("imgUrl", entityVO.get("itemImg"));

            tacLogger.info("VO重写完成");
            //return Response.success(itemEntityVO);
            return Response.success(null);
        } catch (Exception e) {
            tacLogger.info("ERROR" + JSON.toJSONString(e));
            return Response.fail("ERROR_TEXT="+JSON.toJSONString(e));
        }
    }

    protected Map<String, Object> getItemVoMap(ItemInfoBySourceDTO itemInfoBySourceDTO) {
        return itemInfoBySourceDTO.getItemInfoVO();
    }

    private String processScm(String originScm, Map<String, String> scmKeyValue) {
        if (MapUtils.isEmpty(scmKeyValue)) {
            return originScm;
        } else {
            String addScm = Joiner.on("_").withKeyValueSeparator("-").join(scmKeyValue);
            return this.scmConvert(originScm, addScm);
        }
    }

    public String scmConvert(String scm, String add) {
        try {
            if (StringUtils.isBlank(scm)) {
                return scm;
            } else {
                int index = scm.lastIndexOf("-");
                String prefixScm = scm.substring(0, index);
                String suffixScm = scm.substring(index);
                return prefixScm + "_" + add + suffixScm;
            }
        } catch (Exception var6) {
            LOGGER.error("scmConvertError", var6);
            return scm;
        }
    }
}
