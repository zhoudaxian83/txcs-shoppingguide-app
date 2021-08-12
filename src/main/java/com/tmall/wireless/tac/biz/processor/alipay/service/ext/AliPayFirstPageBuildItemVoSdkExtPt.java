package com.tmall.wireless.tac.biz.processor.alipay.service.ext;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.tcls.mkt.atmosphere.model.response.*;
import com.tmall.aselfcaptain.item.model.ItemDTO;
import com.tmall.tcls.gs.sdk.biz.extensions.item.vo.DefaultBuildItemVoSdkExtPt;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.vo.BuildItemVoRequest;
import com.tmall.tcls.gs.sdk.framework.extensions.item.vo.BuildItemVoSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.Response;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemInfoBySourceDTO;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemInfoDTO;
import com.tmall.tcls.gs.sdk.sm.iteminfo.bysource.captain.ItemInfoBySourceCaptainDTO;
import com.tmall.tcls.gs.sdk.sm.iteminfo.bysource.tpp.ItemInfoBySourceTppDTO;
import com.tmall.txcs.biz.supermarket.extpt.buildvo.DefaultBuildItemVOExtPt;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.Op;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

@SdkExtension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C
        , scenario = ScenarioConstantApp.SCENARIO_ALI_PAY_FIRST_PAGE
)
public class AliPayFirstPageBuildItemVoSdkExtPt extends DefaultBuildItemVoSdkExtPt implements BuildItemVoSdkExtPt {

    public static final String PROMOTION_POINT = "promotionPoint";
    public static final String SELLING_PRICE = "sellingPrice";
    public static final String ORIGIN_PRICE = "originPrice";
    @Override
    protected Map<String, Object> getItemVoMap(ItemInfoBySourceDTO itemInfoBySourceDTO) {
        if (itemInfoBySourceDTO instanceof ItemInfoBySourceCaptainDTO) {
            Map<String, Object> result = Maps.newHashMap();
            result.putAll(itemInfoBySourceDTO.getItemInfoVO());
            result.put(PROMOTION_POINT, getPromotionPoint((ItemInfoBySourceCaptainDTO) itemInfoBySourceDTO));
            result.put(SELLING_PRICE, getSellingPrice((ItemInfoBySourceCaptainDTO) itemInfoBySourceDTO));
            result.put(ORIGIN_PRICE, getOriginPrice((ItemInfoBySourceCaptainDTO) itemInfoBySourceDTO));
            return result;
        }
        return itemInfoBySourceDTO.getItemInfoVO();
    }
    private String getOriginPrice(ItemInfoBySourceCaptainDTO itemInfoBySourceDTO) {
        return Optional.of(itemInfoBySourceDTO).
                map(ItemInfoBySourceCaptainDTO::getItemDTO).
                map(ItemDTO::getItemPromotionResp).
                map(ItemPromotionResp::getUnifyPrice).
                map(UnifyPriceDTO::getChaoShiPrice).
                map(Price::getPrice).orElse("");
    }

    private String getSellingPrice(ItemInfoBySourceCaptainDTO itemInfoBySourceDTO) {
        return Optional.of(itemInfoBySourceDTO).
                map(ItemInfoBySourceCaptainDTO::getItemDTO).
                map(ItemDTO::getItemPromotionResp).
                map(ItemPromotionResp::getUnifyPrice).
                map(UnifyPriceDTO::getShowPrice).
                map(Price::getPrice).orElse("");
    }

    private Object getPromotionPoint(ItemInfoBySourceCaptainDTO itemInfoBySourceDTO) {

        ItemPromotionResp itemPromotionResp = Optional.of(itemInfoBySourceDTO).
                map(ItemInfoBySourceCaptainDTO::getItemDTO).
                map(ItemDTO::getItemPromotionResp).orElse(null);

        if (itemPromotionResp == null) {
            return "";
        }

        return itemPromotionResp.getAtmosphereList()
                .stream().findFirst()
                .map(PromotionAtmosphereDTO::getText)
                .map(PromotionTextDTO::getContent)
                .orElse("");
    }
}
