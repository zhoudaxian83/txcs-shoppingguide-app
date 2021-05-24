package com.tmall.wireless.tac.biz.processor.firstScreenMind.model.item;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.util.Map;

public class ItemModel implements Serializable {

    private static final long serialVersionUID = -1L;

    /**商品ID，之所以提出来，是为了充当主键用*/
    @Getter @Setter
    private Long itemId;

    /**商品基本信息*/
    @Getter @Setter
    private ItemBaseInfo itemBaseInfo = new ItemBaseInfo();

    /**商品个性化信息*/
    @Getter @Setter
    private ItemIndividualInfo itemIndividualInfo = new ItemIndividualInfo();

    /**商品库存信息*/
    @Getter @Setter
    private ItemInventoryInfo itemInventoryInfo = new ItemInventoryInfo();

    /**商品优惠信息*/
    @Getter @Setter
    private ItemPromotionInfo itemPromotionInfo = new ItemPromotionInfo();

    /**商品标签信息*/
    @Getter @Setter
    private ItemLabelInfo itemLabelInfo = new ItemLabelInfo();

    /**商品价格信息*/
    @Getter @Setter
    private ItemPriceInfo itemPriceInfo = new ItemPriceInfo();

    /**商品规则信息*/
    @Getter @Setter
    private ItemRuleInfo itemRuleInfo = new ItemRuleInfo();

    /**商品其他扩展信息，格式 k1=v1|k2=v2|...*/
    @Getter @Setter
    private String extension;

    /**商品调试相关信息*/
    @Getter
    private Map<String, Object> itemModelOtherInfo = Maps.newConcurrentMap();

    public void addOtherInfo(String key, Object value) {
        itemModelOtherInfo.put(key, value);
    }

    public void removeOtherInfo(String key) {
        itemModelOtherInfo.remove(key);
    }

}
