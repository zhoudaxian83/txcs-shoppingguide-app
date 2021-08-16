package com.tmall.wireless.tac.biz.processor.firstScreenMind.model.context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.google.common.collect.Maps;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.RenderDataSourceEnum;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.model.content.ContentModel;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.model.item.ItemModel;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.utils.RenderConvertUtil;
import lombok.Getter;
import lombok.Setter;

public class PmtInfo implements Serializable {

    private static final long serialVersionUID = -1L;

    /*************PMT基本信息***********/
    /*场景ID*/
    @Getter @Setter
    private String sceneId;
    /*模块ID*/
    @Getter @Setter
    private String moduleId;
    /*资源位ID*/
    @Getter @Setter
    private String tagId;

    /*是否是第一个Pmt位置*/
    @Getter @Setter
    private boolean firstPmtPosition = false;

    /*多个场景ID映射关系*/
    @Getter @Setter
    private Map<String,String> sceneIdMap = Maps.newConcurrentMap();
    /*多个模块ID映射关系*/
    @Getter @Setter
    private Map<String,String> moduleIdMap = Maps.newConcurrentMap();
    /*多个资源位ID映射关系*/
    @Getter @Setter
    private Map<String,String> tagIdMap = Maps.newConcurrentMap();

    /*************数据基本信息***********/
    /*数据集来源，默认走个性化*/
    @Getter @Setter
    private String dataSource = RenderDataSourceEnum.tppRecallSource.getCode();
    /*商品集ID，如果多个，则逗号隔开*/
    @Getter @Setter
    private String itemSetIds;
    /*数据是否走个性化逻辑*/
    @Getter @Setter
    private boolean individualData = false;
    /*数据是否走打底*/
    @Getter @Setter
    private boolean bottomData = false;

    /*************商品数据信息***********/
    /*召回的商品数据ID列表*/
    @Setter@Getter
    private List<Long> itemIdList = new ArrayList<>();
    /*召回的商品数据*/
    @Setter@Getter
    private List<ItemModel> items = new ArrayList<>();
    /*商品数据渲染结果映射关系，key是itemId*/
    @Setter@Getter
    private Map<Long,ItemModel> itemsMap = Maps.newConcurrentMap();
    /*所见即所得商品串，逗号隔开*/
    @Getter @Setter
    private String entryItemIds;

    /*************内容数据信息***********/
    /*召回的单个内容数据*/
    @Setter@Getter
    private ContentModel contentModel;
    /*召回的内容数据列表*/
    @Setter@Getter
    private List<ContentModel> contentModels = new ArrayList<>();

    /*************商品和内容混排数据信息***********/
    /*召回的商品和内容混排数据*/
    @Setter@Getter
    private List<DataModel> dataModels;

    /*pmt info 扩展字段*/
    @Getter @Setter
    private String pmtInfoExtension;

    /**获取所见即所得商品ID列表*/
    public List<Long> getEntryItemIdList(){
        return RenderConvertUtil.strToLongList(entryItemIds,",");
    }

    /**获取商品集ID列表*/
    public List<Long> getItemSetIdList(){
        return RenderConvertUtil.strToLongList(itemSetIds,",");
    }

}
