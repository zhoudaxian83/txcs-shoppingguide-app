package com.tmall.wireless.tac.biz.processor.extremeItem.domain;

import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class ItemConfigList {
    private List<ItemConfig> itemConfigList;

    /**
     * 校验商品配置数据是否合法
     * 0、itemConfigList不允许为空
     * 1、如果强制排序，则所有商品都必须配置为强制排序
     * 2、如果强制排序，则同一分组下面的商品顺序号必须一致
     * 3、曝光比率必须在【0，100】%，且各个品的曝光比率加起来必须等于100%
     */
    public void checkItemConfig() {

    }

    /**
     * 是否强制排序
     * @return
     */
    public boolean isForceSort() {
        return itemConfigList.get(0).isForceSort();
    }

    /**
     * 按照运营配置的组号将各个商品行的数据进行拆到各自的分组中
     * @return
     */
    public ItemConfigGroupList splitGroup() {
        ItemConfigGroupList itemConfigGroupList = new ItemConfigGroupList();
        List<ItemConfigGroup> itemConfigGroups = itemConfigList.stream()
                .collect(Collectors.groupingBy(ItemConfig::getGroupNo))
                .values().stream()
                .filter(CollectionUtils::isEmpty)
                .map(itemConfigs -> {
                    ItemConfig itemConfig = itemConfigs.get(0);
                    ItemConfigGroup itemConfigGroup = new ItemConfigGroup();
                    itemConfigGroup.setGroupNo(itemConfig.getGroupNo());
                    itemConfigGroup.setForceSort(itemConfig.isForceSort());
                    itemConfigGroup.setSequenceNo(itemConfig.getSequenceNo());
                    itemConfigGroup.getItemConfigList().addAll(itemConfigs);
                    return itemConfigGroup;
                }).collect(Collectors.toList());
        itemConfigGroupList.getItemConfigGroups().addAll(itemConfigGroups);
        return itemConfigGroupList;
    }
}
