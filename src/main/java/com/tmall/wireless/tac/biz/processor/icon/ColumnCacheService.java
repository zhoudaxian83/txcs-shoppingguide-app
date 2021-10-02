package com.tmall.wireless.tac.biz.processor.icon;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.taobao.tair.ResultCode;
import com.tmall.aselfcommon.model.column.KeyUtil;
import com.tmall.aselfcommon.model.column.MainColumnDTO;
import com.tmall.txcs.gs.spi.recommend.TairFactorySpi;
import com.tmall.txcs.gs.spi.recommend.TairManager;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ColumnCacheService {

    Logger LOGGER = LoggerFactory.getLogger(ColumnCacheService.class);

    @Autowired
    KeyUtil keyUtil;

    @Autowired
    TairFactorySpi tairFactorySpi;

    private static final int labelSceneNamespace = 2338;

    private LoadingCache<String, HashMap<String, ArrayList<Long>>> columnTypeToLevel1IdListCache = CacheBuilder
            .newBuilder()
            .maximumSize(10)
            .expireAfterWrite(60, TimeUnit.SECONDS)
            .build(new CacheLoader<String, HashMap<String, ArrayList<Long>> >() {

                @Override
                public HashMap<String, ArrayList<Long>>  load(String key) throws Exception {

                    TairManager iconTair = tairFactorySpi.getIconTair();
                    Result<DataEntry> result = iconTair.getMultiClusterTairManager().get(labelSceneNamespace, key);

                    try {
                        return (HashMap<String, ArrayList<Long>> )result.getValue().getValue();
                    } catch (Throwable e) {
                        LOGGER.error("columnTypeToLevel1IdListCache error", e);
                        return null;
                    }
                }

            });

    private LoadingCache<String, Object> columnCache = CacheBuilder
            .newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(60, TimeUnit.SECONDS)
            .build(new CacheLoader<String, Object>() {

                @Override
                public Object load(String key) throws Exception {
                    TairManager iconTair = tairFactorySpi.getIconTair();
                    Result<DataEntry> result = iconTair.getMultiClusterTairManager().get(labelSceneNamespace, keyUtil.getKey(Long.valueOf(key)));
                    if (result.getRc() == ResultCode.SUCCESS && result.isSuccess()) {
                        return result.getValue().getValue();
                    }
                    LOGGER.error("Get column from tair error:key={},result={}",key,result);
                    return null;
                }

            });


    public HashMap<String, ArrayList<Long>> queryColumnTypeToLevel1IdList() {
        try {
            return columnTypeToLevel1IdListCache.get(keyUtil.getcolumnTypeToLevel1IdKey());
        } catch (Exception e) {

            LOGGER.error("queryColumnTypeToLevel1IdList error:", e);
        }
        return null;
    }

    public Map<Long, MainColumnDTO> getMainColumnMap(String level1IdString) {
        Map<Long, MainColumnDTO> level1IdToMainColumnDTOMap = Maps.newHashMap();
        try {
            HashMap<String, ArrayList<Long>> stringArrayListHashMap = this.queryColumnTypeToLevel1IdList();
            ArrayList<Long> level1IdList = stringArrayListHashMap.get(level1IdString);

            level1IdToMainColumnDTOMap = Maps.newHashMap();

            for (Long level1Id : level1IdList) {
                MainColumnDTO column = this.getColumn(level1Id);
                if (column != null) {
                    level1IdToMainColumnDTOMap.put(level1Id, column);
                }
            }
        } catch (Exception e) {
            LOGGER.error("getMainColumnMap error", e);
        }


        if (MapUtils.isEmpty(level1IdToMainColumnDTOMap)) {
            LOGGER.error("category getMainColumnMap error:{}", level1IdString);
        }

        return level1IdToMainColumnDTOMap;

    }

    public MainColumnDTO getColumn(Long columnId) {
        if (columnId == null || columnId == 0L) {
            LOGGER.error("columnId is null");
            return null;
        }
        try {
            return (MainColumnDTO) columnCache.get(columnId.toString());
        } catch (Exception e) {
            LOGGER.info("getColumn Error:columnId={}",columnId, e);
        }
        return null;
    }

}
