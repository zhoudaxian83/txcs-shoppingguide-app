package com.tmall.wireless.tac.biz.processor.guessYourLikeShopCart4;


import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import com.tmall.tcls.gs.sdk.biz.extensions.item.vo.DefaultBuildItemVoSdkExtPt;
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
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

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
public class GuessYourLikeShopCart4BuildItemVoSdkExtPt extends DefaultBuildItemVoSdkExtPt implements BuildItemVoSdkExtPt {

    @Autowired
    TacLoggerImpl tacLogger;
    @Override
    public Response<ItemEntityVO> process(BuildItemVoRequest buildItemVoRequest) {

        try {
            tacLogger.info("VO重写开始");
            Response<ItemEntityVO> entityVOResponse = super.process(buildItemVoRequest);

            if(!entityVOResponse.isSuccess()){
                return entityVOResponse;
            }
            ItemEntityVO entityVO = entityVOResponse.getValue();

            //cff
            ItemEntityVO itemEntityVO = new ItemEntityVO();
            itemEntityVO.put("scm", entityVO.get("scm"));
            /**点击埋点**/
            Map<String,Object> clickParam = Maps.newHashMap();
            Map<String,Object> args = Maps.newHashMap();
            args.put("ext","{\"index\":\"1\"}");
            args.put("spm","a1z60.7768435.recommend.1");
            args.put("itemid",entityVO.get("itemId"));
            clickParam.put("args",args);
            clickParam.put("eventId","2101");
            clickParam.put("arg1","Page_ShoppingCart_Button-a1z60.7768435.recommend.1");
            clickParam.put("page","Page_ShoppingCart");
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
            itemEntityVO.put("type", "tmall_common_recommend_item");
            /********/
            itemEntityVO.put("pageParam", 0);
            /**曝光埋点**/
            Map<String,Object> exposureParam = Maps.newHashMap();
            Map<String,Object> args1 = Maps.newHashMap();
            args1.put("ext","{\"index\":\"1\"}");
            args1.put("spm","a1z60.7768435.recommend.1");
            args1.put("itemId",entityVO.get("itemId"));
            exposureParam.put("args",args1);
            exposureParam.put("eventId","2201");
            exposureParam.put("arg1","a1z60.7768435.recommend.1");
            exposureParam.put("page","Page_ShoppingCart");
            itemEntityVO.put("exposureParam", exposureParam);

            itemEntityVO.put("imgUrl", entityVO.get("itemImg"));
            itemEntityVO.put("itemImg", entityVO.get("itemImg"));
            itemEntityVO.put("canBuy", entityVO.get("canBuy"));
            itemEntityVO.put("sellOut", entityVO.get("sellOut"));
            tacLogger.info("VO重写完成,VO结果集为："/*+JSON.toJSONString(itemEntityVO)*/);
            //return Response.success(itemEntityVO);
            return Response.success(itemEntityVO);
        } catch (Exception e) {
            tacLogger.info("ERROR" + JSON.toJSONString(e));
            return Response.fail("ERROR_TEXT="+JSON.toJSONString(e));
        }
    }
}
