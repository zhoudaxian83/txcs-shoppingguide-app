package com.tmall.wireless.tac.biz.processor.extremeItem.service.impl;

import com.alibaba.fastjson.JSON;
import com.taobao.igraph.client.model.MatchRecord;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.service.IGraphResponseHandler;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.service.SupermarketHallIGraphSearchService;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemConfigGroups;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemGmvGroupMap;
import com.tmall.wireless.tac.biz.processor.extremeItem.service.ItemGmvService;
import com.tmall.wireless.tac.biz.processor.extremeItem.service.entity.GmvEntity;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemGmvServiceImpl implements ItemGmvService {

    private static Logger logger = LoggerFactory.getLogger(ItemGmvServiceImpl.class);
    @Autowired
    SupermarketHallIGraphSearchService<GmvEntity> supermarketHallIGraphSearchService;
    @Autowired
    TacLogger tacLogger;

    @Override
    public ItemGmvGroupMap queryGmv(ItemConfigGroups itemConfigGroups, List<Long> itemIdList, int days) {

        IGraphResponseHandler<GmvEntity> handler = singleQueryResult -> {
            List<GmvEntity> result = new ArrayList<>();
            if (singleQueryResult.hasError()) {
                tacLogger.warn("oops, got errorMsg:[" + singleQueryResult.getErrorMsg() + "]");
            }
            tacLogger.info("got [" + singleQueryResult.size() + "] records");
            for (MatchRecord matchRecord : singleQueryResult.getMatchRecords()) {
                GmvEntity gmvEntity = new GmvEntity();
                gmvEntity.setGmv(matchRecord.getDouble("gmv"));
                gmvEntity.setItemId(matchRecord.getLong("item_id"));
                gmvEntity.setWindowStart(matchRecord.getString("window_start"));
                gmvEntity.setWindowEnd(matchRecord.getString("window_end"));
                result.add(gmvEntity);
            }
            return result;
        };
        List<String> keyList = itemIdList.stream().map(id -> String.valueOf(id)).collect(Collectors.toList());
        String[] fields = new String[]{"gmv", "item_id", "window_start", "window_end"};
        long startTime = System.currentTimeMillis();
        List<GmvEntity> lastNDayGmvEntityList = getNDaysGmv(handler, keyList, fields, "TPP_tmall_sm_tmcs_item_gmv_history", 12);
        long endTime = System.currentTimeMillis();
        logger.info("ItemGmvServiceImpl_queryGmv_lastNDayGmvEntityList_cost(ms):" + (endTime - startTime));
        logger.info("ItemGmvServiceImpl_queryGmv_lastNDayGmvEntityList: " + JSON.toJSONString(lastNDayGmvEntityList));
        startTime = System.currentTimeMillis();
        List<GmvEntity> last1HourGmvEntityList = supermarketHallIGraphSearchService.search("TPP_tmall_smtmcs_item_gmv_current_time_1h", keyList, fields, 1, handler);
        endTime = System.currentTimeMillis();
        logger.info("ItemGmvServiceImpl_queryGmv_last1HourGmvEntityList_cost(ms):" + (endTime - startTime));
        logger.info("ItemGmvServiceImpl_queryGmv_last1HourGmvEntityList: " + JSON.toJSONString(last1HourGmvEntityList));

        return ItemGmvGroupMap.valueOf(itemConfigGroups, lastNDayGmvEntityList, last1HourGmvEntityList, days);
    }

    private List<GmvEntity> getNDaysGmv(IGraphResponseHandler<GmvEntity> handler, List<String> keyList, String[] fields, String iGraphTable, int perKeySize) {
        return supermarketHallIGraphSearchService.search(iGraphTable, keyList, fields, perKeySize, handler);
    }
}
