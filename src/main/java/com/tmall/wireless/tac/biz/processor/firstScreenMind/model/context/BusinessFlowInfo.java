package com.tmall.wireless.tac.biz.processor.firstScreenMind.model.context;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

public class BusinessFlowInfo implements Serializable {

    private static final long serialVersionUID = -1L;

    /**并发有序节点列表，记录请求链关键信息*/
//    @Getter @Setter
//    private Queue<RenderFlowNode> renderFlowNodeList = new ConcurrentLinkedQueue<RenderFlowNode>();

    /**是否需要召回活动节点*/
    @Getter @Setter
    private boolean needRecallActivityNode = false;

    /**是否需要规则活动节点*/
    @Getter @Setter
    private boolean needRuleActivityNode = false;

    /**是否需要排序活动节点*/
    @Getter @Setter
    private boolean needSortActivityNode = false;

    /**是否需要分页活动节点*/
    @Getter@Setter
    private boolean needPageActivityNode = false;

    /**是否需要补全活动节点*/
    @Getter@Setter
    private boolean needCompletionActivityNode = false;

    /**是否需要合并&去重活动节点*/
    @Getter@Setter
    private boolean needMergeRemoveActivityNode = false;

    /**是否需要打底活动节点*/
    @Getter@Setter
    private boolean needBottomActivityNode = false;

    /**是否需要埋点活动节点*/
    @Getter@Setter
    private boolean needBuryPointActivityNode = false;

}
