package com.tmall.wireless.tac.biz.processor.extremeItem.service.impl;

import com.alibaba.fastjson.JSON;
import com.taobao.igraph.client.model.MatchRecord;
import com.taobao.igraph.client.model.SingleQueryResult;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.service.IGraphResponseHandler;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.service.SupermarketHallIGraphSearchService;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemGmvGroupMap;
import com.tmall.wireless.tac.biz.processor.extremeItem.service.ItemGmvService;
import com.tmall.wireless.tac.biz.processor.extremeItem.service.entity.GmvEntity;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public ItemGmvGroupMap queryGmv(List<Long> itemIdList) {

        IGraphResponseHandler<GmvEntity> handler = singleQueryResult -> {
            if (singleQueryResult.hasError()) {
                tacLogger.warn("oops, got errorMsg:[" + singleQueryResult.getErrorMsg() + "]");
            }
            tacLogger.info("got [" + singleQueryResult.size() + "] records");
            GmvEntity gmvEntity = new GmvEntity();
            for (MatchRecord matchRecord : singleQueryResult.getMatchRecords()) {
                gmvEntity.setGmv(matchRecord.getDouble("gmv"));
                gmvEntity.setItemId(matchRecord.getLong("item_id"));
                gmvEntity.setWindowStart(matchRecord.getString("window_start"));
                gmvEntity.setWindowEnd(matchRecord.getString("window_end"));
            }
            return gmvEntity;
        };
        List<String> keyList = itemIdList.stream().map(id -> String.valueOf(id)).collect(Collectors.toList());
        String[] fields = new String[]{"gmv", "item_id", "window_start", "window_end"};
        List<GmvEntity> gmvEntityList = supermarketHallIGraphSearchService.search("TPP_tmall_sm_tmcs_item_gmv_history", keyList, fields, 10, handler);
        logger.info("ItemGmvServiceImpl_queryGmv_gmvEntityList: " + JSON.toJSONString(gmvEntityList));
        return null;
    }
}
