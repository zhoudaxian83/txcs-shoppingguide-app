package com.tmall.wireless.tac.biz.processor.firstScreenMind.model;

import java.io.Serializable;
import java.util.Map;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;

public class RenderFlowNode implements Serializable {

    private static final long serialVersionUID = -1L;

    /**节点名称*/
    @Getter @Setter
    private String nodeName;

    /**节点是否执行降级操作，默认不降级*/
    @Getter @Setter
    private boolean degrade;

    /**节点的核心请求参数(精简过)*/
    @Getter @Setter
    private Map<String,Object> nodeQuery = Maps.newConcurrentMap();

    /**节点的核心返回结果(精简过)*/
    @Getter @Setter
    private Map<String,Object> nodeResult = Maps.newConcurrentMap();

    /**节点失败原因*/
    @Getter @Setter
    private String nodeFailReason;

    /**节点异常原因*/
    @Getter @Setter
    private String nodeExcReason;

    /**节点过滤、校验不通过以及其他逻辑的解释*/
    @Getter @Setter
    private String nodeFilterExplain;

}
