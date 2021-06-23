package com.tmall.wireless.tac.biz.processor.wzt.utils;

import com.google.common.collect.Lists;
import com.tmall.txcs.gs.framework.model.EntityVO;
import com.tmall.wireless.tac.biz.processor.wzt.model.ItemLimitDTO;

import java.util.List;

/**
 * @author luojunchong
 */
public class LimitItemUtil {

    public static boolean notLimit(ItemLimitDTO itemLimitDTO) {
        //当已售数量大于等于总限制数，个人限制数量大于等于个人限购数沉底处理
        return itemLimitDTO.getUsedCount() < itemLimitDTO.getTotalLimit()
            && itemLimitDTO.getUserUsedCount() < itemLimitDTO.getUserLimit();
    }

    public static List<EntityVO> doLimitItems(List<EntityVO> entityVOList) {
        List<EntityVO> noLimitEntityVOList = Lists.newArrayList();
        entityVOList.forEach(entityVO -> {
            ItemLimitDTO itemLimitDTO = (ItemLimitDTO)entityVO.get("itemLimit");
            if (itemLimitDTO == null || itemLimitDTO.getSkuId() == null) {
                noLimitEntityVOList.add(entityVO);
            }
            //去掉超出限购的，如果都超出限购则正常放回全部数据
            if (LimitItemUtil.notLimit(itemLimitDTO)) {
                noLimitEntityVOList.add(entityVO);
            }
        });
        return noLimitEntityVOList;

    }

}
