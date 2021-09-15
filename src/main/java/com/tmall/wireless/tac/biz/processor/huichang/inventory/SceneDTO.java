package com.tmall.wireless.tac.biz.processor.huichang.inventory;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.tmall.aselfcaptain.item.model.ItemDTO;
import lombok.Data;

/**
 * Created by likunlin.lkl on 2021-07-15.
 */
@Data
public class SceneDTO implements Serializable {
    // 以下是渲染得出
    private String id; // 场景id
    private String title;  //"场景标题",
    private String subtitle;  //"场景副标题",
    private String marketChannel; // 市场渠道, B2C;O2O
    private String setSource; // setSource: 数据集来源, SCM;UPLUS
    private List<String> setIds; //场景主圈品集IDs(榜单场景仅一个圈品集ID)
    private Map<String, Object> property;
    // 以下是填入
    private String urlParams; // 承接页url参数
    private String description; // 多少好评、多少销售、多少回购的文案
    private List<ItemDTO> itemList; // 推荐商品的补全信息
}
