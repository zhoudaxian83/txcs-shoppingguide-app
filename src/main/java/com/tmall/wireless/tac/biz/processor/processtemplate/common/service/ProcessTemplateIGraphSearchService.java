package com.tmall.wireless.tac.biz.processor.processtemplate.common.service;

import java.util.List;

public interface ProcessTemplateIGraphSearchService<T> {
    List<T> search(String tableName, List<String> keyList, String[] fields, int perKeySize, IGraphResponseHandler<T> handler);
}
