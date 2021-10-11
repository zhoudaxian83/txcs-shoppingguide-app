package com.tmall.wireless.tac.biz.processor.extremeItem.domain.service;

import com.alibaba.fastjson.JSON;
import com.taobao.eagleeye.EagleEye;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.SupermarketHallContext;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.util.Logger;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.util.LoggerProxy;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemConfigGroups;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemGmvGroup;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemGmvGroupMap;
import com.tmall.wireless.tac.biz.processor.extremeItem.service.ItemGmvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupSortDomainService {
    private static Logger logger = LoggerProxy.getLogger(GroupSortDomainService.class);

    @Autowired
    private ItemGmvService itemGmvService;

    /**
     * 组间排序，如果是强制排序，则按照运营制定的顺序直接返回，否则进行赛马
     *
     * @param itemConfigGroups
     */
    public void groupSort(ItemConfigGroups itemConfigGroups, List<Long> itemIds, SupermarketHallContext supermarketHallContext) {
        if(itemConfigGroups.forceSort()) {
            itemConfigGroups.sortGroup();
            logger.info("GroupSortDomainService_groupSort_itemConfigGroups_forceSort: " + JSON.toJSONString(itemConfigGroups));
            return;
        }
        //如果为空，默认days是0
        int days = supermarketHallContext.getTacParamsMap().getIntValue("gmvDays");
        raceSort(itemConfigGroups, itemIds, days);
        logger.info("GroupSortDomainService_groupSort_itemConfigGroups_raceSort: " + JSON.toJSONString(itemConfigGroups));
    }

    /**
     * 赛马排序
     *
     * @param itemConfigGroups
     * @param itemIds
     * @param days
     */
    private void raceSort(ItemConfigGroups itemConfigGroups, List<Long> itemIds, int days) {
        Long raceSortStart = System.currentTimeMillis();
        try {
            ItemGmvGroupMap itemGmvGroupMap = itemGmvService.queryGmv(itemConfigGroups, itemIds, days);
            boolean lostLastNDaysGmv = itemGmvGroupMap.getInnerItemGmvGroupMap().values().stream().map(ItemGmvGroup::lastNDaysGmvSum).anyMatch(gmv -> gmv == 0);
            if (lostLastNDaysGmv) {
                HadesLogUtil.stream("ExtremeItemSdkItemHandler|raceSort.downgrade|" + Logger.isEagleEyeTest() + "|downgrade")
                        .error();
                itemConfigGroups.sortGroup();
            } else {
                itemConfigGroups.sortGroup(itemGmvGroupMap);
                Long raceSortEnd = System.currentTimeMillis();
                HadesLogUtil.stream("ExtremeItemSdkItemHandler|raceSort|" + Logger.isEagleEyeTest() + "|success|" + (raceSortEnd - raceSortStart))
                        .error();
            }
        } catch (Exception e) {
            HadesLogUtil.stream("ExtremeItemSdkItemHandler|raceSort|" + Logger.isEagleEyeTest() + "|exception")
                    .error();
            logger.error("GroupSortDomainService error, traceId:" + EagleEye.getTraceId(), e);
            itemConfigGroups.sortGroup();
        }
    }
}
