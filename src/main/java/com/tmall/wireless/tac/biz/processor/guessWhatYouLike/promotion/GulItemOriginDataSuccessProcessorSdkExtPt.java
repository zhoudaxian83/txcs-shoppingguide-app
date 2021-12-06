package com.tmall.wireless.tac.biz.processor.guessWhatYouLike.promotion;

import java.util.Map;
import java.util.Optional;

import com.alibaba.fastjson.JSON;

import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataSuccessProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.OriginDataProcessRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContext;
import com.tmall.tmallwireless.tac.spi.context.SPIResult;
import com.tmall.wireless.store.spi.tair.TairSpi;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.icon.item.ItemRecommendService;
import com.tmall.wireless.tac.biz.processor.icon.item.ItemRequest;
import com.tmall.wireless.tac.biz.processor.icon.item.ext.IconItemOriginDataFailProcessorSdkExtPt;
import com.tmall.wireless.tac.biz.processor.todaycrazy.utils.MapUtil;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zhongwei
 * @date 2021/12/3
 */
@SdkExtension(
    bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.SCENARIO_SUB_PROMOTION_PAGE
)
public class GulItemOriginDataSuccessProcessorSdkExtPt  extends Register implements ItemOriginDataSuccessProcessorSdkExtPt {

    public static final String SHOPPING_GUIDE_TAIR_USER_NAME = "b6241830ca7f4b9d";
    public static final int SHOPPING_GUIDE_NAME_SPACE = 184;

    @Autowired
    TairSpi tairSpi;

    @Override
    public OriginDataDTO<ItemEntity> process(OriginDataProcessRequest originDataProcessRequest) {

        try {
            Map<String, Object> userParams = originDataProcessRequest.getSgFrameworkContextItem().getUserParams();
            // 圈品集+locType
            String itemSetId = MapUtil.getStringWithDefault(userParams, "itemSetIdList", "0");
            String locType = MapUtil.getStringWithDefault(userParams, "locType", "B2C");
            String tairKey = String.format("%s_%s_%s","gulPromotionItemTairKey_", itemSetId, locType);
            tairSpi.put(
                IconItemOriginDataFailProcessorSdkExtPt.SHOPPING_GUIDE_TAIR_USER_NAME,
                IconItemOriginDataFailProcessorSdkExtPt.SHOPPING_GUIDE_NAME_SPACE,
                tairKey,
                JSON.toJSONString(originDataProcessRequest.getItemEntityOriginDataDTO().getResult())
            , 60 * 60 * 24 * 2);
            // 打底的超时时间


            return originDataProcessRequest.getItemEntityOriginDataDTO();
        } catch (Exception e) {
            LOGGER.error("IconItemOriginDataSuccessProcessorSdkExtPt error", e);
            return originDataProcessRequest.getItemEntityOriginDataDTO();
        }
    }
}
