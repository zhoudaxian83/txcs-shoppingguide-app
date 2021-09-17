package com.tmall.wireless.tac.biz.processor.extremeItem.domain;

import com.alibaba.fastjson.JSON;
import com.tmall.wireless.tac.biz.processor.extremeItem.service.entity.GmvEntity;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class ItemGmvGroupMap {
    private static Logger logger = LoggerFactory.getLogger(ItemGmvGroupMap.class);

    private Map<Integer, ItemGmvGroup> innerItemGmvGroupMap;

    public static ItemGmvGroupMap valueOf(ItemConfigGroups itemConfigGroups, List<GmvEntity> last7DayGmvEntityList, List<GmvEntity> last1HourGmvEntityList, List<GmvEntity> todayGmvEntityList) {
        String todayDateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        ItemGmvGroupMap itemGmvGroupMap = new ItemGmvGroupMap();
        itemGmvGroupMap.innerItemGmvGroupMap = new HashMap<>();

        Map<Long, Double[]> last7DayGmvEntityMap = last7DayGmvEntityList.stream()
                .collect(Collectors.groupingBy(e -> e.getItemId()))
                .entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().stream()
                        .filter(gmvEntity -> !gmvEntity.getWindowEnd().startsWith(todayDateStr))
                        .map(item -> item.getGmv()).toArray(Double[]::new)));

        logger.info("ItemGmvGroupMap_valueOf_last7DayGmvEntityMap: " + JSON.toJSONString(last7DayGmvEntityMap));
        Map<Long, Double> last1HourGmvEntityMap = last1HourGmvEntityList.stream().collect(Collectors.toMap(e -> e.getItemId(), e -> e.getGmv()));
        logger.info("ItemGmvGroupMap_valueOf_last1HourGmvEntityMap: " + JSON.toJSONString(last1HourGmvEntityMap));
        Map<Long, Double> todayGmvEntityMap = todayGmvEntityList.stream().collect(Collectors.toMap(e -> e.getItemId(), e -> e.getGmv()));
        logger.info("ItemGmvGroupMap_valueOf_todayGmvEntityMap: " + JSON.toJSONString(todayGmvEntityMap));

        for (ItemConfigGroup itemConfigGroup : itemConfigGroups.getItemConfigGroupList()) {
            ItemGmvGroup itemGmvGroup = new ItemGmvGroup();
            itemGmvGroup.setGroupNo(itemConfigGroup.getGroupNo());
            List<ItemGmv> itemGmvList = new ArrayList<>();
            for (ItemConfig itemConfig : itemConfigGroup.getItemConfigList()) {
                ItemGmv itemGmv = new ItemGmv();
                Long itemId = itemConfig.getItemId();
                itemGmv.setItemId(itemId);
                itemGmv.setLast1HourGmv(last1HourGmvEntityMap.get(itemId));
                itemGmv.setTodayGmv(todayGmvEntityMap.get(itemId));
                itemGmv.setLast7DaysGmv(last7DayGmvEntityMap.get(itemId));
                itemGmvList.add(itemGmv);
            }
            itemGmvGroup.setItemGmvList(itemGmvList);
            logger.info("ItemGmvGroupMap_valueOf_itemGmvGroup: " + JSON.toJSONString(itemGmvGroup));
        }

        return itemGmvGroupMap;

    }

    public double raceValueOf(Integer groupNo) {
        return innerItemGmvGroupMap.get(groupNo).ranceValue();
    }

}
