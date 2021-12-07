package com.tmall.wireless.tac.biz.processor.guessWhatYouLike.promotion;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.alibaba.fastjson.JSON;

import com.google.common.base.Preconditions;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataFailProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.OriginDataProcessRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContext;
import com.tmall.tmallwireless.tac.spi.context.SPIResult;
import com.tmall.wireless.store.spi.tair.TairSpi;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.icon.item.ItemRecommendService;
import com.tmall.wireless.tac.biz.processor.icon.item.ItemRequest;
import com.tmall.wireless.tac.biz.processor.todaycrazy.utils.MapUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zhongwei
 * @date 2021/12/2
 */
@SdkExtension(
    bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.SCENARIO_SUB_PROMOTION_PAGE
)
public class GulItemOriginDataFailProcessorSdkExtPt  extends Register implements ItemOriginDataFailProcessorSdkExtPt {

    public static final String SHOPPING_GUIDE_TAIR_USER_NAME = "b6241830ca7f4b9d";
    public static final int SHOPPING_GUIDE_NAME_SPACE = 184;

    @Autowired
    TairSpi tairSpi;

    @Override
    public OriginDataDTO<ItemEntity> process(OriginDataProcessRequest originDataProcessRequest) {
        String tairKey = "";
        boolean success = false;
        try {
            Map<String, Object> userParams = originDataProcessRequest.getSgFrameworkContextItem().getUserParams();
            // 圈品集+locType
            String itemSetId = MapUtil.getStringWithDefault(userParams, "itemSetIdList", "0");
            String locType = MapUtil.getStringWithDefault(userParams, "locType", "B2C");
            tairKey = String.format("%s_%s_%s","gulPromotionItemTairKey", itemSetId, locType);
            SPIResult<Result<DataEntry>> resultSPIResult = tairSpi.get(SHOPPING_GUIDE_TAIR_USER_NAME, SHOPPING_GUIDE_NAME_SPACE, tairKey);

            Object o = Optional.ofNullable(resultSPIResult).map(SPIResult::getData).map(Result::getValue).map(DataEntry::getValue).orElse(null);
            if (o == null) {
                return originDataProcessRequest.getItemEntityOriginDataDTO();
            }
            List<ItemEntity> itemEntityList = JSON.parseArray(o.toString(), ItemEntity.class);
            OriginDataDTO<ItemEntity> result = new OriginDataDTO<>();
            result.setResult(itemEntityList);
            success = CollectionUtils.isNotEmpty(itemEntityList);
            result.setHasMore(false);
            result.setSuccess(true);
            result.setScm("tpp.error");
            return result;
        } catch (Exception e) {
            LOGGER.error("GulItemOriginDataFailProcessorSdkExtPt error", e);
            return originDataProcessRequest.getItemEntityOriginDataDTO();
        } finally {
            LOGGER.warn("GulItemOriginDataFailProcessorSdkExtPt:{}, {}",tairKey, success);
        }

    }

}
