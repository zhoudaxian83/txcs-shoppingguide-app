package com.tmall.wireless.tac.biz.processor.firstpage.banner.iteminfo.model;

import lombok.Data;

import java.util.List;

/**
 * Created by yangqing.byq on 2021/4/6.
 */

public class BannerVO {
    String source;

    List<Long> failItemList;

    String entryItems;

    List<Long> itemIdList;

    List<BannerItemVO> items;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<Long> getFailItemList() {
        return failItemList;
    }

    public void setFailItemList(List<Long> failItemList) {
        this.failItemList = failItemList;
    }

    public String getEntryItems() {
        return entryItems;
    }

    public void setEntryItems(String entryItems) {
        this.entryItems = entryItems;
    }

    public List<Long> getItemIdList() {
        return itemIdList;
    }

    public void setItemIdList(List<Long> itemIdList) {
        this.itemIdList = itemIdList;
    }

    public List<BannerItemVO> getItems() {
        return items;
    }

    public void setItems(List<BannerItemVO> items) {
        this.items = items;
    }
}
