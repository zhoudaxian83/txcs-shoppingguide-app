package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;

import com.google.common.collect.Lists;
import com.tmall.aselfcommon.util.StackTraceUtil;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.filter.ItemFilterRequest;
import com.tmall.tcls.gs.sdk.framework.extensions.item.filter.ItemFilterSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ErrorCode;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.constant.CommonConstant;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.model.ItemLimitDTO;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.config.SxlSwitch;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created from template by 罗俊冲 on 2021-09-25 13:40:47.
 */

@SdkExtension(
    bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.TODAY_CRAZY_RECOMMEND_TAB
)
@Slf4j
public class TodayCrazyRecommendTabItemFilterSdkExtPt extends Register implements ItemFilterSdkExtPt {
    @Autowired
    TacLoggerImpl tacLogger;

    private static final Logger LOGGER = LoggerFactory.getLogger(TodayCrazyRecommendTabItemFilterSdkExtPt.class);
    public static final List<String> CHECK_FIELD = Lists.newArrayList(
        "itemImg"
    );

    @Override
    public SgFrameworkResponse<ItemEntityVO> process(ItemFilterRequest itemFilterRequest) {

        tacLogger.info("过滤节点开始");
        SgFrameworkResponse<ItemEntityVO> entityVOSgFrameworkResponse = itemFilterRequest.getEntityVOSgFrameworkResponse();

        List<ItemEntityVO> itemAndContentList = entityVOSgFrameworkResponse.getItemAndContentList();

        if (CollectionUtils.isEmpty(itemAndContentList)) {
            return entityVOSgFrameworkResponse;
        }
        List<ItemEntityVO> itemAndContentListAfterFilter = Lists.newArrayList();
        Set<Long> noFilterItemIdSet = new HashSet<>();
        try{
            noFilterItemIdSet = Arrays.stream(SxlSwitch.TODAY_CRAZY_NO_FILTER_ITEMS.split(",")).map(Long::valueOf).collect(Collectors.toSet());
        }catch (Exception e) {
            log.error("重点商品配置错误:{},{}",SxlSwitch.TODAY_CRAZY_NO_FILTER_ITEMS, StackTraceUtil.stackTrace(e));
        }

        for (ItemEntityVO entityVO : itemAndContentList) {
            if (entityVO != null) {
                if (noFilterItemIdSet.contains(entityVO.getItemId())){
                    entityVO.put("canBuy", Boolean.TRUE);
                    itemAndContentListAfterFilter.add(entityVO);
                } else {
                    if (!this.canBuy(entityVO) || !this.noLimitBuyV2(entityVO)) {
                        tacLogger.info("被过滤数据：" + entityVO.getString("itemId"));
                    } else {
                        if (checkField(entityVO)) {
                            itemAndContentListAfterFilter.add(entityVO);
                        }
                    }
                }
            }
        }

        entityVOSgFrameworkResponse.setItemAndContentList(itemAndContentListAfterFilter);

        return entityVOSgFrameworkResponse;
    }

    protected boolean checkField(ItemEntityVO entityVO) {
        List<String> checkField = getFieldList();
        if (CollectionUtils.isEmpty(checkField)) {
            return true;
        }
        for (String field : checkField) {
            if (Objects.isNull(entityVO.get(field))) {
                LOGGER.error("itemFilter,{}, itemId:{},field:{}", ErrorCode.ITEM_FILTER_BY_FIELD_ERROR, entityVO.getString("itemId"), field);
                return false;
            }
        }
        return true;
    }

    protected List<String> getFieldList() {
        return CHECK_FIELD;
    }

    private boolean canBuy(ItemEntityVO item) {
        Boolean canBuy = item.getBoolean("canBuy");
        Boolean sellOut = item.getBoolean("sellOut");
        return (canBuy == null || canBuy) && (sellOut == null || !sellOut);
    }

    public boolean noLimitBuy(ItemEntityVO itemEntityVO) {
        if (!CommonConstant.LIMIT_BUY_SWITCH) {
            return true;
        }
        ItemLimitDTO itemLimitDTO = (ItemLimitDTO)itemEntityVO.get("itemLimit");
        if (itemLimitDTO == null) {
            return true;
        }
        //为true不校验总限购
        boolean totalLimit = itemLimitDTO.getTotalLimit() == itemLimitDTO.getUsedCount() && 0L == itemLimitDTO.getUsedCount();
        //为true不校验用户限购
        boolean userLimit = itemLimitDTO.getUserLimit() == itemLimitDTO.getUserUsedCount() && 0L == itemLimitDTO.getUserUsedCount();
        //如果只有总限购则只校验用户限购
        if (totalLimit && userLimit) {
            return true;
        }
        if (totalLimit) {
            return itemLimitDTO.getUserLimit() > itemLimitDTO.getUserUsedCount();
        }
        if (userLimit) {
            return itemLimitDTO.getTotalLimit() > itemLimitDTO.getUsedCount();
        }
        if (itemLimitDTO.getUsedCount() < itemLimitDTO.getTotalLimit()
            && itemLimitDTO.getUserUsedCount() < itemLimitDTO.getUserLimit()) {
            return true;
        } else {
            tacLogger.info("被限购过滤itemId=" + itemEntityVO.getItemId() + JSON.toJSONString(itemLimitDTO));
            HadesLogUtil.stream(ScenarioConstantApp.TODAY_CRAZY_RECOMMEND_TAB)
                .kv("TodayCrazyRecommendTabItemFilterSdkExtPt", "noLimitBuy")
                .kv("itemId", Long.toString(itemEntityVO.getItemId()))
                .kv("recommendRequest", JSON.toJSONString(itemLimitDTO))
                .info();
            return false;
        }
    }

    public boolean noLimitBuyV2(ItemEntityVO itemEntityVO) {
        ItemLimitDTO itemLimitDTO = (ItemLimitDTO)itemEntityVO.get("itemLimit");
        if (itemLimitDTO == null) {
            return true;
        }
        //兼容只有总限购或个人限购的情况
        boolean totalLimit = itemLimitDTO.getUsedCount() != null && itemLimitDTO.getTotalLimit() != null;
        boolean userLimit = itemLimitDTO.getUserUsedCount() != null && itemLimitDTO.getUserLimit() != null;
        if (!totalLimit && !userLimit) {
            return true;
        }
        if (totalLimit && userLimit) {
            /**
             * 当已售数量大于等于总限制数，个人限制数量大于等于个人限购数不过滤。否则过滤
             */
            return itemLimitDTO.getUsedCount() < itemLimitDTO.getTotalLimit()
                && itemLimitDTO.getUserUsedCount() < itemLimitDTO.getUserLimit();
        }
        if (totalLimit) {
            return itemLimitDTO.getUsedCount() < itemLimitDTO.getTotalLimit();
        } else {
            return itemLimitDTO.getUserUsedCount() < itemLimitDTO.getUserLimit();
        }

    }

}
