package com.tmall.wireless.tac.biz.processor.firstpage.banner.iteminfo.model;

import lombok.Data;

import java.util.List;

/**
 * Created by yangqing.byq on 2021/4/6.
 */
public class BannerDTO {
    String locType;
    List<Long> itemIdList;

    public String getLocType() {
        return locType;
    }

    public void setLocType(String locType) {
        this.locType = locType;
    }

    public List<Long> getItemIdList() {
        return itemIdList;
    }

    public void setItemIdList(List<Long> itemIdList) {
        this.itemIdList = itemIdList;
    }
}
