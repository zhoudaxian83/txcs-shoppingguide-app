package com.tmall.wireless.tac.biz.processor.extremeItem.common.service;

import java.util.List;

public interface SupermarketHallIGraphSearchService<T> {
    List<T> search(String tableName, List<String> keyList, String[] fields, int perKeySize, IGraphResponseHandler<T> handler);
}
