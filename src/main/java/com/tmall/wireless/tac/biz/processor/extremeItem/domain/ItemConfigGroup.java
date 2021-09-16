package com.tmall.wireless.tac.biz.processor.extremeItem.domain;

import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Data
public class ItemConfigGroup {

    private static Random random = new Random();
    /**
     * 分组编号
     */
    private Integer groupNo;

    /**
     * 是否强制排序，如果强制排序，则不走赛马逻辑
     */
    private boolean forceSort;

    /**
     * 分组顺序，如果强制排序，则各分组间按照该字段从小到达排序，每个分组的所有商品该字段必须保持一致
     */
    private Integer sequenceNo;

    private List<ItemConfig> itemConfigList = new ArrayList<>();

    /**
     * 库存过滤之后剩余的商品
     */
    private List<ItemConfig> invFilteredItemList = new ArrayList<>();

    /**
     * 曝光为0过滤之后剩余的商品
     */
    private List<ItemConfig> exposureRateFilteredItemList = new ArrayList<>();

    /**
     * 库存过滤，从原始的商品配置列表中过滤出来有库存的，放入invFilteredItemList中
     *
     * @param itemSoldOutMap 商品ID和是否售光的Map
     */
    public void inventoryFilter(Map<Long, Boolean> itemSoldOutMap) {
        for (ItemConfig itemConfig : this.itemConfigList) {
            if(!itemSoldOutMap.get(itemConfig.getItemId())) {
                this.invFilteredItemList.add(itemConfig);
            }
        }
    }

    /**
     * 在库存过滤的基础上，过滤掉曝光为0的品
     */
    public void exposureRateFilter() {
        for (ItemConfig itemConfig : this.invFilteredItemList) {
            if(itemConfig.getExposurePercent() > 0) {
                this.exposureRateFilteredItemList.add(itemConfig);
            }
        }
    }

    /**
     * 当库存过滤后，基于剩下的有库存商品，曝光比例非零的，优先按照曝光比例；
     * 如有库存但均为0，则随机。如所有库存均无，则随机出一个打售罄标，
     * 前端此时一排2的时候，有一个售罄，是打售罄标，2个坑都售罄了就缩坑。
     *
     * @return
     */
    public ItemConfig pickItem() {
        //有库存的曝光比例全都是0，在有库存的品中随机出
        if(CollectionUtils.isEmpty(this.exposureRateFilteredItemList) && CollectionUtils.isNotEmpty(this.invFilteredItemList)) {
            return this.invFilteredItemList.get(random.nextInt(this.invFilteredItemList.size()));
        }
        //如所有库存均无，则在全部品中随机出一个
        if(CollectionUtils.isEmpty(this.invFilteredItemList)) {
            return this.itemConfigList.get(random.nextInt(this.itemConfigList.size()));
        }
        //有库存，曝光比例非0，按照曝光比例出
        return new PickItemSolution(this.exposureRateFilteredItemList).pickItem();
    }

    private static class PickItemSolution {
        int[] pre;
        int total;
        List<ItemConfig> itemConfigList;

        public PickItemSolution(List<ItemConfig> itemConfigList) {
            this.itemConfigList = itemConfigList;
            pre = new int[itemConfigList.size()];
            pre[0] = itemConfigList.get(0).getExposurePercent();
            for (int i = 1; i < itemConfigList.size(); ++i) {
                pre[i] = pre[i - 1] + itemConfigList.get(i).getExposurePercent();
            }
            total = itemConfigList.stream().mapToInt(ItemConfig::getExposurePercent).sum();
        }

        private int pickIndex() {
            int x = (int) (Math.random() * total) + 1;
            return binarySearch(x);
        }

        private int binarySearch(int x) {
            int low = 0, high = pre.length - 1;
            while (low < high) {
                int mid = (high - low) / 2 + low;
                if (pre[mid] < x) {
                    low = mid + 1;
                } else {
                    high = mid;
                }
            }
            return low;
        }

        public ItemConfig pickItem() {
            return this.itemConfigList.get(pickIndex());
        }
    }
}
