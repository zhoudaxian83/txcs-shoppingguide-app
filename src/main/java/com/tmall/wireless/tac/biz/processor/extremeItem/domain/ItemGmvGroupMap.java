package com.tmall.wireless.tac.biz.processor.extremeItem.domain;

import com.alibaba.fastjson.JSON;
import com.tmall.wireless.tac.biz.processor.extremeItem.service.entity.GmvEntity;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

@Data
public class ItemGmvGroupMap {
    private static Logger logger = LoggerFactory.getLogger(ItemGmvGroupMap.class);

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private Map<Integer, ItemGmvGroup> innerItemGmvGroupMap;

    private Map<Integer, Integer> lastNDaysGmvRankMap;
    private Map<Integer, Integer> last1HourGmvRankMap;

    private static Set<String> lastNDaysDateSet = new CopyOnWriteArraySet<>();

    public static ItemGmvGroupMap valueOf(ItemConfigGroups itemConfigGroups, List<GmvEntity> last7DayGmvEntityList, List<GmvEntity> last1HourGmvEntityList, List<GmvEntity> todayGmvEntityList, int days) {
        Set<String> lastNDaysDateSet = loadLastNDaysDateSet(days);
        ItemGmvGroupMap itemGmvGroupMap = new ItemGmvGroupMap();
        itemGmvGroupMap.innerItemGmvGroupMap = new HashMap<>();

        Map<Long, Double[]> lastNDayGmvEntityMap = last7DayGmvEntityList.stream()
                .collect(Collectors.groupingBy(e -> e.getItemId()))
                .entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().stream()
                        .filter(gmvEntity -> lastNDaysDateSet.contains(gmvEntity.getWindowEnd().split(" ")[0]))
                        .map(item -> item.getGmv()).toArray(Double[]::new)));

        logger.info("ItemGmvGroupMap_valueOf_lastNDayGmvEntityMap: " + JSON.toJSONString(lastNDayGmvEntityMap));
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
                itemGmv.setLastNDaysGmv(lastNDayGmvEntityMap.get(itemId));
                itemGmvList.add(itemGmv);
            }
            itemGmvGroup.setItemGmvList(itemGmvList);
            logger.info("ItemGmvGroupMap_valueOf_itemGmvGroup: " + JSON.toJSONString(itemGmvGroup));
            itemGmvGroupMap.innerItemGmvGroupMap.put(itemConfigGroup.getGroupNo(), itemGmvGroup);
        }

        itemGmvGroupMap.lastNDaysGmvRankMap = new HashMap<>();
        itemGmvGroupMap.last1HourGmvRankMap = new HashMap<>();

        List<ItemGmvGroup> lastNDaysGmvRankList = itemGmvGroupMap.innerItemGmvGroupMap.values().stream()
                .sorted(Comparator.comparing(ItemGmvGroup::lastNDaysGmvSum).reversed()).collect(Collectors.toList());

        List<ItemGmvGroup> last1HourGmvRankList = itemGmvGroupMap.innerItemGmvGroupMap.values().stream()
                .sorted(Comparator.comparing(ItemGmvGroup::last1HourGmvSum).reversed()).collect(Collectors.toList());

        int i = 1;
        for (ItemGmvGroup itemGmvGroup : lastNDaysGmvRankList) {
            itemGmvGroupMap.lastNDaysGmvRankMap.put(itemGmvGroup.getGroupNo(), i++);
        }
        i = 1;
        for (ItemGmvGroup itemGmvGroup : last1HourGmvRankList) {
            itemGmvGroupMap.last1HourGmvRankMap.put(itemGmvGroup.getGroupNo(), i++);
        }
        logger.info("ItemGmvGroupMap_valueOf_itemGmvGroupMap: " + JSON.toJSONString(itemGmvGroupMap));
        return itemGmvGroupMap;

    }

    public double raceValueOf(Integer groupNo) {
        return this.lastNDaysGmvRankMap.get(groupNo) * 0.5 + this.last1HourGmvRankMap.get(groupNo) * 0.5;
    }

    public double raceValueOfLast1HourGmvRank(Integer groupNo) {
        return this.last1HourGmvRankMap.get(groupNo);
    }

    /**
     * 获取最近n天前到当天的日期集合，如n为0代表当天，n最大为7，超过7当作7处理，最小为0，小于0当作0处理
     * 比如当天为2021-09-26，则n为3时，返回{"2021-09-26", "2021-09-25", "2021-09-24", "2021-09-23"}
     * @param n 代表n天前
     * @return
     */
    public static Set<String> loadLastNDaysDateSet(int n) {
        Set<String> result = new HashSet<>();
        if(n < 0) {
            n = 0;
        }
        if(n > 7) {
            n = 7;
        }

        LocalDate todayDate =  LocalDate.now();

        if(lastNDaysDateSet.contains(todayDate.format(dateTimeFormatter)) && lastNDaysDateSet.size() == n + 1) {
            return lastNDaysDateSet;
        }
        for(int i=0; i<=n; i++) {
            result.add(todayDate.minusDays(i).format(dateTimeFormatter));
        }
        lastNDaysDateSet.clear();
        lastNDaysDateSet.addAll(result);
        return lastNDaysDateSet;
    }

    public static void main(String[] args) {
        System.out.println("args = " + loadLastNDaysDateSet(-1));
        System.out.println("args = " + loadLastNDaysDateSet(0));
        System.out.println("args = " + loadLastNDaysDateSet(1));
        System.out.println("args = " + loadLastNDaysDateSet(2));
        System.out.println("args = " + loadLastNDaysDateSet(3));
        System.out.println("args = " + loadLastNDaysDateSet(4));
        System.out.println("args = " + loadLastNDaysDateSet(5));
        System.out.println("args = " + loadLastNDaysDateSet(6));
        System.out.println("args = " + loadLastNDaysDateSet(7));
        System.out.println("args = " + loadLastNDaysDateSet(8));
    }

}
