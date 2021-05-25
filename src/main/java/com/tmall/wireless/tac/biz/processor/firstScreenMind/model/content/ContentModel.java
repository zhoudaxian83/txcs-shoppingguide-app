package com.tmall.wireless.tac.biz.processor.firstScreenMind.model.content;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.google.common.collect.Maps;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.model.item.ItemModel;
import lombok.Getter;
import lombok.Setter;

public class ContentModel implements Serializable {

    private static final long serialVersionUID = -1L;

    /**内容基本信息*/
    @Getter @Setter
    private ContentBaseInfo contentBaseInfo = new ContentBaseInfo();

    /**内容下挂载的子内容集合*/
    @Setter@Getter
    private List<SubContentModel> subContentModelList = new ArrayList<>();

    /**内容下挂载的商品数据集合*/
    @Setter@Getter
    private List<ItemModel> items = new ArrayList<>();

    /**内容规则信息*/
    @Getter @Setter
    private ContentRuleInfo contentRuleInfo = new ContentRuleInfo();

    /**内容其他扩展信息，格式 k1=v1|k2=v2|...*/
    @Getter @Setter
    private String extension;

    /**内容调试相关信息*/
    @Getter
    private Map<String, Object> contentModelOtherInfo = Maps.newConcurrentMap();

    public void addOtherInfo(String key, Object value) {
        contentModelOtherInfo.put(key, value);
    }

    public void removeOtherInfo(String key) {
        contentModelOtherInfo.remove(key);
    }

}
