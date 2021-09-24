package com.tmall.wireless.tac.biz.processor.extremeItem.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Data
public class ItemConfigGroups {
    private List<ItemConfigGroup> itemConfigGroupList = new ArrayList<>();

    public boolean forceSort() {
        return itemConfigGroupList.stream().allMatch(ItemConfigGroup::isForceSort);
    }

    /**
     * 根据运营配置强制排序，不走赛马
     */
    public void sortGroup() {
        Collections.sort(itemConfigGroupList, new ForceSortComparator());
    }

    public void sortGroup(ItemGmvGroupMap itemGmvGroupMap) {
        if(itemGmvGroupMap == null) {
            return;
        }
        Collections.sort(itemConfigGroupList, new RaceSortComparator(itemGmvGroupMap));
    }

    private static class ForceSortComparator implements Comparator<ItemConfigGroup> {

        @Override
        public int compare(ItemConfigGroup o1, ItemConfigGroup o2) {
            if(o1.getSequenceNo() < o2.getSequenceNo()) {
                return -1;
            } else if(o1.getSequenceNo() > o2.getSequenceNo()) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    private static class RaceSortComparator implements Comparator<ItemConfigGroup> {
        private ItemGmvGroupMap itemGmvGroupMap;
        public RaceSortComparator(ItemGmvGroupMap itemGmvGroupMap) {
            this.itemGmvGroupMap = itemGmvGroupMap;
        }

        @Override
        public int compare(ItemConfigGroup o1, ItemConfigGroup o2) {
            double group1RaceValue = itemGmvGroupMap.raceValueOf(o1.getGroupNo());
            double group2RaceValue = itemGmvGroupMap.raceValueOf(o2.getGroupNo());
            if(group1RaceValue < group2RaceValue) {
                return 1;
            } else if(group1RaceValue > group2RaceValue) {
                return -1;
            } else {
                double group1RaceValueForLast1HourRank = itemGmvGroupMap.raceValueOfLast1HourGmvRank(o1.getGroupNo());
                double group2RaceValueForLast1HourRank = itemGmvGroupMap.raceValueOfLast1HourGmvRank(o2.getGroupNo());
                if(group1RaceValueForLast1HourRank < group2RaceValueForLast1HourRank) {
                    return 1;
                } else if(group1RaceValueForLast1HourRank > group2RaceValueForLast1HourRank) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }
    }
}
