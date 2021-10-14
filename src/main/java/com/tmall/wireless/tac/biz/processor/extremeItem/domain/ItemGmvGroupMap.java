package com.tmall.wireless.tac.biz.processor.extremeItem.domain;

import com.alibaba.fastjson.JSON;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.util.Logger;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.util.LoggerProxy;
import com.tmall.wireless.tac.biz.processor.extremeItem.service.entity.GmvEntity;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

@Data
public class ItemGmvGroupMap {
    private static Logger logger = LoggerProxy.getLogger(ItemGmvGroupMap.class);

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter dfWithMilliSecond = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private Map<Integer, ItemGmvGroup> innerItemGmvGroupMap;

    private Map<Integer, Integer> lastNDaysGmvRankMap;
    private Map<Integer, Integer> last1HourGmvRankMap;

    private static Set<String> lastNDaysDateSet = new CopyOnWriteArraySet<>();

    public static ItemGmvGroupMap valueOf(ItemConfigGroups itemConfigGroups, List<GmvEntity> lastNDayGmvEntityList, List<GmvEntity> last1HourGmvEntityList, int days) {
        Set<String> lastNDaysDateSet = loadLastNDaysDateSet(days);
        ItemGmvGroupMap itemGmvGroupMap = new ItemGmvGroupMap();
        itemGmvGroupMap.innerItemGmvGroupMap = new HashMap<>();

        Map<Long, Double[]> lastNDayGmvEntityMap = lastNDayGmvEntityList.stream()
                .collect(Collectors.groupingBy(e -> e.getItemId()))
                .entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().stream()
                        .filter(Objects::nonNull)
                        .filter(gmvEntity -> StringUtils.isNotBlank(gmvEntity.getWindowEnd()))
                        .filter(gmvEntity -> lastNDaysDateSet.contains(gmvEntity.getWindowEnd().split(" ")[0]))
                        .map(item -> item.getGmv()).toArray(Double[]::new)));

        //logger.info("ItemGmvGroupMap_valueOf_lastNDayGmvEntityMap: " + JSON.toJSONString(lastNDayGmvEntityMap));
        //String currentYMDH = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH"));
        Map<Long, Double> last1HourGmvEntityMap = last1HourGmvEntityList.stream()
                .filter(Objects::nonNull)
                .filter(e -> e.getWindowEnd() != null)
                .filter(e -> withIn1Hour(e.getWindowEnd()))
                //.filter(e->e.getWindowEnd().startsWith(currentYMDH))
                .collect(Collectors.toMap(e -> e.getItemId(), e -> e.getGmv()));
        //logger.info("ItemGmvGroupMap_valueOf_last1HourGmvEntityMap: " + JSON.toJSONString(last1HourGmvEntityMap));

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
            itemGmvGroupMap.innerItemGmvGroupMap.put(itemConfigGroup.getGroupNo(), itemGmvGroup);
        }

        itemGmvGroupMap.lastNDaysGmvRankMap = new HashMap<>();
        itemGmvGroupMap.last1HourGmvRankMap = new HashMap<>();

        List<ItemGmvGroup> lastNDaysGmvRankList = itemGmvGroupMap.innerItemGmvGroupMap.values().stream()
                .sorted(Comparator.comparing(ItemGmvGroup::lastNDaysGmvSum).reversed()).collect(Collectors.toList());

        List<ItemGmvGroup> last1HourGmvRankList = itemGmvGroupMap.innerItemGmvGroupMap.values().stream()
                .sorted(Comparator.comparing(ItemGmvGroup::last1HourGmvSum).reversed()).collect(Collectors.toList());

        int i = 1;
        int nDaysSize = lastNDaysGmvRankList.size();
        for(int index = 0; index < nDaysSize; index++) {
            ItemGmvGroup itemGmvGroup = lastNDaysGmvRankList.get(index);
            if(index > 0 && Math.abs(lastNDaysGmvRankList.get(index).lastNDaysGmvSum() - lastNDaysGmvRankList.get(index-1).lastNDaysGmvSum()) < 0.01) {
                i--;
            }
            itemGmvGroupMap.lastNDaysGmvRankMap.put(itemGmvGroup.getGroupNo(), i++);
        }

        boolean lost1HourGmvData = last1HourGmvRankList.stream().anyMatch(itemGmvGroup -> itemGmvGroup.last1HourGmvSum() == 0);
        if(!lost1HourGmvData) {
            i = 1;
            int oneHoursSize = last1HourGmvRankList.size();
            for (int index = 0; index < oneHoursSize; index++) {
                ItemGmvGroup itemGmvGroup = last1HourGmvRankList.get(index);
                if (index > 0 && Math.abs(last1HourGmvRankList.get(index).last1HourGmvSum() - last1HourGmvRankList.get(index - 1).last1HourGmvSum()) < 0.01) {
                    i--;
                }
                itemGmvGroupMap.last1HourGmvRankMap.put(itemGmvGroup.getGroupNo(), i++);
            }
            logger.info("ItemGmvGroupMap_valueOf_itemGmvGroupMap: " + JSON.toJSONString(itemGmvGroupMap));
        } else {
            HadesLogUtil.stream("ExtremeItemSdkItemHandler|raceSort.downgrade.L1|" + Logger.isEagleEyeTest() + "|success")
                    .error();
            int oneHoursSize = last1HourGmvRankList.size();
            for (int index = 0; index < oneHoursSize; index++) {
                ItemGmvGroup itemGmvGroup = last1HourGmvRankList.get(index);
                itemGmvGroupMap.last1HourGmvRankMap.put(itemGmvGroup.getGroupNo(), 1);
            }
            logger.info("ItemGmvGroupMap_valueOf_itemGmvGroupMap(lost1HourGmvData): " + JSON.toJSONString(itemGmvGroupMap));
        }
        return itemGmvGroupMap;

    }

    private static boolean withIn1Hour(String windowEnd) {
        LocalDateTime windowEndTime = LocalDateTime.parse(windowEnd, dfWithMilliSecond);
        LocalDateTime nowMinus1Hour = LocalDateTime.now().minusHours(1);
        return windowEndTime.isAfter(nowMinus1Hour);
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
        boolean b = withIn1Hour("2021-10-09 14:30:00.000");
        System.out.println("b = " + b);
    }
}
