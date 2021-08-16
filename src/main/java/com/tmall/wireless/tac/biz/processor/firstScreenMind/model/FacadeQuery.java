package com.tmall.wireless.tac.biz.processor.firstScreenMind.model;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

public class FacadeQuery implements Serializable  {

    private static final long serialVersionUID = -1L;

    /********************用户****************************/
    /**用户ID*/
    @Getter @Setter
    private Long userId;
    /**用户nick*/
    @Getter @Setter
    private String userNick;
    /**未登陆用户唯一身份ID*/
    @Getter @Setter
    private String cna;
    /**设备ID*/
    @Getter @Setter
    private String ttid;
    /**请求来源渠道*/
    @Setter@Getter
    private String channel;

    /********************时间****************************/
    /**日期，格式：2020-11-12*/
    @Getter @Setter
    private String dateString;
    /**小时，格式：9、12、23*/
    @Getter @Setter
    private String hour;
    /**预览模式，格式：*/
    @Getter @Setter
    private Long previewTime;

    /********************地址****************************/
    /**四级区域地址ID*/
    @Getter @Setter
    private Long smAreaId;
    /**三级城市ID*/
    @Getter @Setter
    private Long cityCode;
    /**当前页头的csa地址信息*/
    @Getter @Setter
    private String csa;
    /**门店类型*/
    @Getter @Setter
    private String storeType;
    /**门店ID*/
    @Getter @Setter
    private Long storeId;

    /********************PMT****************************/
    /**场景ID*/
    @Getter @Setter
    private String sceneId;
    /**模块ID*/
    @Getter @Setter
    private String moduleId;
    /**资源位ID*/
    @Getter @Setter
    private String tagId;
    /**所见即所得商品串，逗号隔开*/
    @Getter @Setter
    private String entryItemIds;
    /**商品集ID，如果是多个，则用逗号隔开*/
    @Getter @Setter
    private String itemSetIds;

    /********************分页****************************/
    /**分页数据开始位置，默认从0开始*/
    @Getter @Setter
    private int pageStartPosition = 0;
    /**每页数据量大小，默认20*/
    @Getter @Setter
    private int pageSize = 20;
    /**数据分页缓存tair版本，默认-1*/
    @Getter @Setter
    private int version = -1;

}
