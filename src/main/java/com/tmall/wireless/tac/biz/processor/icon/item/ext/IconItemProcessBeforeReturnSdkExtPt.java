package com.tmall.wireless.tac.biz.processor.icon.item.ext;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.filter.ItemProcessBeforeReturnSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.context.CommonUserParams;
import com.tmall.tcls.gs.sdk.framework.model.context.PageInfoDO;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContext;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextItem;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.detail.common.extabstract.AbstractDetailItemBackUpSdkExtPt;
import com.tmall.wireless.tac.biz.processor.icon.model.IconFixedItemDTO;
import com.tmall.wireless.tac.biz.processor.wzt.constant.Constant;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhongwei
 * @date 2021/11/24
 */
@SdkExtension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.ICON_ITEM
)
public class IconItemProcessBeforeReturnSdkExtPt  extends Register implements ItemProcessBeforeReturnSdkExtPt {

    Logger logger = LoggerFactory.getLogger(IconItemProcessBeforeReturnSdkExtPt.class);

    public SgFrameworkContextItem process(SgFrameworkContextItem sgFrameworkContextItem) {
        List<ItemEntityVO> itemEntityVOS = fixedItemRank(sgFrameworkContextItem.getEntityVOSgFrameworkResponse().getItemAndContentList(), sgFrameworkContextItem);
        sgFrameworkContextItem.getEntityVOSgFrameworkResponse().setItemAndContentList(itemEntityVOS);
        return sgFrameworkContextItem;
    }

    // 插坑
    private List<ItemEntityVO> fixedItemRank(List<ItemEntityVO> itemEntityVOS, SgFrameworkContextItem context) {
        try {
            if (!context.getUserParams().containsKey(Constant.FIXED_ITEM)) {
                return itemEntityVOS;
            }
            List<IconFixedItemDTO> fixedItemDTOList = ((List<IconFixedItemDTO>)context.getUserParams().getOrDefault(Constant.FIXED_ITEM, Lists.newArrayList()));
            Map<Long, List<IconFixedItemDTO>> fixItemIdMap = fixedItemDTOList.stream().collect(Collectors.groupingBy(IconFixedItemDTO::getItemId));
            List<Pair<Long, ItemEntityVO>> rankItemVO = Lists.newArrayList();
            List<ItemEntityVO> originItemVOS = Lists.newArrayList();
            for (int i = 0; i < itemEntityVOS.size(); i++) {
                long index = i * 10L;
                Long itemId = itemEntityVOS.get(i).getItemId();
                if (fixItemIdMap.containsKey(itemId)) {
                    IconFixedItemDTO fixedItemDTO = fixItemIdMap.get(itemId).get(0);
                    rankItemVO.add(Pair.of(fixedItemDTO.getIndex() * 10 - 5, itemEntityVOS.get(i)));
                } else {
                    originItemVOS.add(itemEntityVOS.get(i));
                    //rankItemVO.add(Pair.of(index, itemEntityVOS.get(i)));
                }
            }
            for (int i = 0; i < originItemVOS.size(); i++) {
                rankItemVO.add(Pair.of((i + 1) * 10L, originItemVOS.get(i)));
            }
            Integer pageSize = Optional.of(context).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getUserPageInfo).map(PageInfoDO::getPageSize).orElse(20);
            itemEntityVOS = rankItemVO.stream()
                .sorted(Comparator.comparing(Pair::getLeft)).map(Pair::getRight)
                .limit(pageSize)
                .collect(Collectors.toList());
            return itemEntityVOS;
        } catch (Exception e) {
            logger.error("fixedItemRank", e);
            return itemEntityVOS;
        }

    }
}
