package com.tmall.wireless.tac.biz.processor.extremeItem.common.service;

import com.taobao.igraph.client.model.SingleQueryResult;

import java.util.List;

public interface IGraphResponseHandler<T> {
    List<T> handleResponse(SingleQueryResult singleQueryResult);
}
