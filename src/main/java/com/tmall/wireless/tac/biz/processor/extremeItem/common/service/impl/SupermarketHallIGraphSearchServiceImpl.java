package com.tmall.wireless.tac.biz.processor.extremeItem.common.service.impl;

import com.alibaba.fastjson.JSON;
import com.taobao.igraph.client.model.*;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tmallwireless.tac.spi.context.SPIResult;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.service.IGraphResponseHandler;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.service.SupermarketHallIGraphSearchService;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.util.Logger;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.util.LoggerProxy;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupermarketHallIGraphSearchServiceImpl<T> implements SupermarketHallIGraphSearchService<T> {
    private static Logger logger = LoggerProxy.getLogger(SupermarketHallIGraphSearchServiceImpl.class);

    @Autowired
    com.tmall.wireless.store.spi.third.IGraphSpi iGraphSpi;

    @Override
    public List<T> search(String tableName, List<String> keyList, String[] fields, int perKeySize,  IGraphResponseHandler<T> handler) {
        // 查询语句构造
        if(CollectionUtils.isEmpty(keyList) || fields == null || fields.length == 0) {
            return new ArrayList<>();
        }
        List<KeyList> keyLists = keyList.stream().map(searchKey -> new KeyList(searchKey)).collect(Collectors.toList());
        AtomicQuery atomicQuery = new AtomicQuery(tableName, keyLists);
        atomicQuery.setReturnFields(fields);
        atomicQuery.setRange(0, perKeySize * keyList.size());

        // 查询接口调用
        QueryResult queryResult;
        Long totalStart = System.currentTimeMillis();
        try {
            SPIResult<QueryResult> queryResultSPIResult = iGraphSpi.search(atomicQuery);
            if(queryResultSPIResult.isSuccess()) {
                Long totalEnd = System.currentTimeMillis();
                HadesLogUtil.stream("SupermarketHallIGraphSearchServiceImpl.search|success")
                        .kv("totalCost", String.valueOf(totalEnd - totalStart))
                        .error();
                queryResult = queryResultSPIResult.getData();
            } else {
                Long totalEnd = System.currentTimeMillis();
                HadesLogUtil.stream("SupermarketHallIGraphSearchServiceImpl.search|error")
                        .kv("totalCost", String.valueOf(totalEnd - totalStart))
                        .error();
                return new ArrayList<>();
            }
        } catch (Exception e) {
            Long totalEnd = System.currentTimeMillis();
            HadesLogUtil.stream("SupermarketHallIGraphSearchServiceImpl.search|error")
                    .kv("totalCost", String.valueOf(totalEnd - totalStart))
                    .error();
            logger.error("search failed", e);
            return new ArrayList<>();
        }

        SingleQueryResult singleQueryResult = queryResult.getSingleQueryResult();
        logger.info("=========singleQueryResult:" + JSON.toJSONString(singleQueryResult));
        return handler.handleResponse(singleQueryResult);
    }
}
