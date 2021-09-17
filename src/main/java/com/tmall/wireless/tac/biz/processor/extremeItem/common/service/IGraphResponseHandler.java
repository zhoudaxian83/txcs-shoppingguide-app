package com.tmall.wireless.tac.biz.processor.extremeItem.common.service;

import com.taobao.igraph.client.model.SingleQueryResult;

public interface IGraphResponseHandler<T> {
    T handleResponse(SingleQueryResult singleQueryResult);
}
