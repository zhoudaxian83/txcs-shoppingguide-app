package com.tmall.wireless.tac.biz.processor.firstpage.banner.iteminfo.model;

import lombok.Data;

/**
 * Created by yangqing.byq on 2021/4/6.
 */

public class BannerItemVO {
    Long itemId;
    String locType;
    String itemImg;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getLocType() {
        return locType;
    }

    public void setLocType(String locType) {
        this.locType = locType;
    }

    public String getItemImg() {
        return itemImg;
    }

    public void setItemImg(String itemImg) {
        this.itemImg = itemImg;
    }
}
