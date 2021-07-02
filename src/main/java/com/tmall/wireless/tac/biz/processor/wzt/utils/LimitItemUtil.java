package com.tmall.wireless.tac.biz.processor.wzt.utils;

import com.google.common.collect.Lists;
import com.tmall.txcs.gs.framework.model.EntityVO;
import com.tmall.wireless.tac.biz.processor.wzt.model.ItemLimitDTO;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * @author luojunchong
 */
public class LimitItemUtil {
    @Autowired
    TacLogger tacLogger;

    public static boolean notLimit(ItemLimitDTO itemLimitDTO) {
        if (itemLimitDTO == null || itemLimitDTO.getSkuId() == null) {
            return true;
        }

        //兼容只有总限购或个人限购的情况
        boolean totalLimit = itemLimitDTO.getUsedCount() != null && itemLimitDTO.getTotalLimit() != null;
        boolean userLimit = itemLimitDTO.getUserUsedCount() != null && itemLimitDTO.getUserLimit() != null;
        if (!totalLimit && !userLimit) {
            return true;
        }
        if (totalLimit && userLimit) {
            //当已售数量大于等于总限制数，个人限制数量大于等于个人限购数沉底处理
            return itemLimitDTO.getUsedCount() < itemLimitDTO.getTotalLimit()
                && itemLimitDTO.getUserUsedCount() < itemLimitDTO.getUserLimit();
        }
        if (totalLimit) {
            return itemLimitDTO.getUsedCount() < itemLimitDTO.getTotalLimit();
        } else {
            return itemLimitDTO.getUserUsedCount() < itemLimitDTO.getUserLimit();
        }
    }

    public static List<EntityVO> doLimitItems(List<EntityVO> entityVOList) {
        List<EntityVO> noLimitEntityVOList = Lists.newArrayList();
        entityVOList.forEach(entityVO -> {
            ItemLimitDTO itemLimitDTO = (ItemLimitDTO)entityVO.get("itemLimit");
            boolean canBuy = (boolean)entityVO.get("canBuy");
            boolean sellout = (boolean)entityVO.get("sellout");
            boolean limit = LimitItemUtil.notLimit(itemLimitDTO);
            //去掉超出限购的，如果都超出限购则正常放回全部数据
            //且拥有库存，可以购买的
            if (limit && canBuy && sellout) {
                noLimitEntityVOList.add(entityVO);
            }
        });
        return noLimitEntityVOList;

    }

}
