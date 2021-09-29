package com.tmall.wireless.tac.biz.processor.extremeItem.domain;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class ItemConfigs {
    private static Logger logger = LoggerFactory.getLogger(ItemConfigs.class);

    private List<ItemConfig> itemConfigList = new ArrayList<>();

    /**
     * 从运营手工配置的数据中构造领域对象ItemConfigs
     *
     * @param aldDataList 运营在鸿雁手工配置的数据
     * @return 商品配置列表领域对象
     */
    public static ItemConfigs valueOf(List<Map<String, Object>> aldDataList) {
        ItemConfigs itemConfigs = new ItemConfigs();
        for (Map<String, Object> stringObjectMap : aldDataList) {
            ItemConfig itemConfig = ItemConfig.valueOf(stringObjectMap);
            itemConfigs.itemConfigList.add(itemConfig);
        }
        logger.info("ItemConfigs_valueOf_itemConfigs: " + JSON.toJSONString(itemConfigs));
        itemConfigs.checkItemConfig();
        return itemConfigs;
    }

    /**
     * 校验商品配置数据是否合法
     * 0、itemConfigList不允许为空
     * 1、如果强制排序，则所有商品都必须配置为强制排序
     * 2、如果强制排序，则同一分组下面的商品顺序号必须一致
     * 3、曝光比率必须在【0，100】%，且各个品的曝光比率加起来必须等于100%
     */
    public void checkItemConfig() {
        if(CollectionUtils.isEmpty(this.itemConfigList)) {
            throw new RuntimeException("商品配置数据不允许为空");
        }
    }

    /**
     * 是否强制排序
     * @return
     */
    public boolean isForceSort() {
        return itemConfigList.stream().allMatch(ItemConfig::isForceSort);
    }

    /**
     * 按照运营配置的组号将各个商品行的数据进行拆到各自的分组中
     * @return
     */
    public ItemConfigGroups splitGroup() {
        ItemConfigGroups itemConfigGroups = new ItemConfigGroups();
        List<ItemConfigGroup> itemConfigGroupList = itemConfigList.stream()
                .collect(Collectors.groupingBy(ItemConfig::getGroupNo))
                .values().stream()
                .filter(CollectionUtils::isNotEmpty)
                .map(itemConfigs -> {
                    ItemConfig itemConfig = itemConfigs.get(itemConfigs.size() - 1);
                    ItemConfigGroup itemConfigGroup = new ItemConfigGroup();
                    itemConfigGroup.setGroupNo(itemConfig.getGroupNo());
                    itemConfigGroup.setForceSort(itemConfigs.stream().allMatch(ItemConfig::isForceSort));
                    itemConfigGroup.setSequenceNo(itemConfig.getSequenceNo());
                    itemConfigGroup.getItemConfigList().addAll(itemConfigs);
                    return itemConfigGroup;
                }).collect(Collectors.toList());
        itemConfigGroups.getItemConfigGroupList().addAll(itemConfigGroupList);
        logger.info("ItemConfigs_splitGroup_itemConfigGroups: " + JSON.toJSONString(itemConfigGroups));
        return itemConfigGroups;
    }

    public List<Long> extractItemIds() {
        return this.itemConfigList.stream().map(ItemConfig::getItemId).collect(Collectors.toList());
    }
}
