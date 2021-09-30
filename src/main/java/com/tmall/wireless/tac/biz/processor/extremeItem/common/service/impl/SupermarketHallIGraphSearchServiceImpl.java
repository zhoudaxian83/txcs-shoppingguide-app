package com.tmall.wireless.tac.biz.processor.extremeItem.common.service.impl;

import com.alibaba.fastjson.JSON;
import com.taobao.igraph.client.model.*;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.service.IGraphResponseHandler;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.service.SupermarketHallIGraphSearchService;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupermarketHallIGraphSearchServiceImpl<T> implements SupermarketHallIGraphSearchService<T> {
    private static Logger logger = LoggerFactory.getLogger(SupermarketHallIGraphSearchServiceImpl.class);

    @Autowired
    TacLogger tacLogger;
    /*@Autowired
    com.taobao.igraph.client.core.IGraphClientWrap iGraphClientWrap;*/

    @Override
    public List<T> search(String tableName, List<String> keyList, String[] fields, int perKeySize,  IGraphResponseHandler<T> handler) {
        return new ArrayList<>();
        // 查询语句构造
        /*if(CollectionUtils.isEmpty(keyList) || fields == null || fields.length == 0) {
            return new ArrayList<>();
        }
        List<KeyList> keyLists = keyList.stream().map(searchKey -> new KeyList(searchKey)).collect(Collectors.toList());
        AtomicQuery atomicQuery = new AtomicQuery(tableName, keyLists);
        atomicQuery.setReturnFields(fields);
        atomicQuery.setRange(0, perKeySize * keyList.size());

        // 查询接口调用
        QueryResult queryResult;
        try {
            queryResult = iGraphClientWrap.search(atomicQuery);
        } catch (Exception e) {
            logger.error("search failed", e);
            return new ArrayList<>();
        }
        SingleQueryResult singleQueryResult = queryResult.getSingleQueryResult();
        logger.info("=========singleQueryResult:" + JSON.toJSONString(singleQueryResult));
        return handler.handleResponse(singleQueryResult);*/
    }
}
