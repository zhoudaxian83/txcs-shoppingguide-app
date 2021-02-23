package com.tmall.wireless.tac.biz.processor.iteminfo.constant;

/**
 * Created by yangqing.byq on 2021/2/23.
 */
public enum ItemInfoSource {

    SM_ASELf_CAPTAIN("猫超captian获取数据"),
    SM_SMART_UI("猫超智能UI"),
    SM_ZHAOSHANG("招商系统"),
    ;

    private String desc;

    ItemInfoSource(String desc) {
        this.desc = desc;
    }
}
