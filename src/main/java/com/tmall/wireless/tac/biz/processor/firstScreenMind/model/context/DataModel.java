package com.tmall.wireless.tac.biz.processor.firstScreenMind.model.context;

import java.io.Serializable;
import java.util.Map;

import com.google.common.collect.Maps;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.model.content.ContentModel;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.model.item.ItemModel;
import lombok.Getter;
import lombok.Setter;

public class DataModel implements Serializable {

    private static final long serialVersionUID = -1L;

    /**数据类型：商品item、内容content*/
    @Getter @Setter
    private String dataType;

    /**内容数据模型*/
    @Setter@Getter
    private ContentModel contentModel;

    /**商品数据模型*/
    @Setter@Getter
    private ItemModel itemModel;

    /**其他扩展信息，格式 k1=v1|k2=v2|...*/
    @Getter @Setter
    private String extension;

    /**调试相关信息*/
    @Getter
    private Map<String, Object> DataModelOtherInfo = Maps.newConcurrentMap();

    public void addOtherInfo(String key, Object value) {
        DataModelOtherInfo.put(key, value);
    }

    public void removeOtherInfo(String key) {
        DataModelOtherInfo.remove(key);
    }

}
