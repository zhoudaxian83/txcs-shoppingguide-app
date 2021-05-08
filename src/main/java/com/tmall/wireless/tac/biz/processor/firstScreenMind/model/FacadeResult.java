package com.tmall.wireless.tac.biz.processor.firstScreenMind.model;

import com.google.common.collect.Maps;
import com.taobao.eagleeye.EagleEye;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FacadeResult<ItemModel,ContentModel> implements Serializable {

    private static final long serialVersionUID = -1L;

    /***返回的商品总数*/
    @Getter @Setter
    private int count;

    /***是否还有下一页*/
    @Getter @Setter
    private boolean hasMore;

    /***返回商品对象*/
    @Getter @Setter
    protected ItemModel itemModel;

    /***返回内容对象*/
    @Getter @Setter
    protected ContentModel contentModel;

    @Getter @Setter
    protected boolean success;

    @Getter @Setter
    private String errCode;

    @Getter @Setter
    private String errMessage;

    @Getter@Setter
    private String traceId = EagleEye.getTraceId();

    /**有序节点列表，记录请求链关键信息*/
    @Getter @Setter
    private Queue<RenderFlowNode> renderFlowNodeList = new ConcurrentLinkedQueue<RenderFlowNode>();

    /**调试相关信息*/
    @Getter @Setter
    private Map<String, Object> otherInfo = Maps.newConcurrentMap();

    public void addOtherInfo(String key, Object value) {
        otherInfo.put(key, value);
    }

    public void removeOtherInfo(String key) {
        otherInfo.remove(key);
    }

    /*******************************************fail**************************************/

    public static FacadeResult fail(String msgInfo) {
        FacadeResult result = new FacadeResult();
        result.setSuccess(false);
        result.setErrMessage(msgInfo);
        return result;
    }

    public static FacadeResult fail(String msgCode, String msgInfo) {
        FacadeResult result = new FacadeResult();
        result.setSuccess(false);
        result.setErrCode(msgCode);
        result.setErrMessage(msgInfo);
        return result;
    }

    public static <ItemModel,ContentModel> FacadeResult<ItemModel,ContentModel> failType(String msgInfo) {
        FacadeResult<ItemModel,ContentModel> result = new FacadeResult<>();
        result.setSuccess(false);
        result.setErrMessage(msgInfo);
        return result;
    }

    public static <ItemModel,ContentModel> FacadeResult<ItemModel,ContentModel> failType(String msgCode, String msgInfo) {
        FacadeResult<ItemModel,ContentModel> result = new FacadeResult<>();
        result.setSuccess(false);
        result.setErrCode(msgCode);
        result.setErrMessage(msgInfo);
        return result;
    }

    /*******************************************success**************************************/

    public static FacadeResult success() {
        FacadeResult result = new FacadeResult();
        result.setSuccess(true);
        return result;
    }

    public static <ItemModel,ContentModel>
    FacadeResult<ItemModel,ContentModel> successItemModel(ItemModel itemModel) {
        FacadeResult<ItemModel,ContentModel> result = new FacadeResult<>();
        result.setSuccess(true);
        result.setItemModel(itemModel);
        return result;
    }

    public static <ItemModel,ContentModel>
    FacadeResult<ItemModel,ContentModel> successContentModel(ContentModel contentModel) {
        FacadeResult<ItemModel,ContentModel> result = new FacadeResult<>();
        result.setSuccess(true);
        result.setContentModel(contentModel);
        return result;
    }

    public static <ItemModel,ContentModel>
    FacadeResult<ItemModel,ContentModel> successMixModel(ItemModel itemModel,ContentModel contentModel) {
        FacadeResult<ItemModel,ContentModel> result = new FacadeResult<>();
        result.setSuccess(true);
        result.setItemModel(itemModel);
        result.setContentModel(contentModel);
        return result;
    }

}

