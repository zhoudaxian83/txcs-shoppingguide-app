package com.tmall.wireless.tac.biz.processor.processtemplate.common.service;

import com.taobao.igraph.client.model.SingleQueryResult;

import java.util.List;

public interface IGraphResponseHandler<T> {
    List<T> handleResponse(SingleQueryResult singleQueryResult);
}
