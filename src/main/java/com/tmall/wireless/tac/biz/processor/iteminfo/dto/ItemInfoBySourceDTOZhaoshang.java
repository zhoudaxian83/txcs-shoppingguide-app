package com.tmall.wireless.tac.biz.processor.iteminfo.dto;

import com.tmall.txcs.gs.framework.support.itemInfo.bysource.ItemInfoBySourceDTO;
import lombok.Data;

/**
 * Created by yangqing.byq on 2021/2/26.
 */

public class ItemInfoBySourceDTOZhaoshang extends ItemInfoBySourceDTO {
    String zhaoshangInfo;


    public String getZhaoshangInfo() {
        return zhaoshangInfo;
    }

    public void setZhaoshangInfo(String zhaoshangInfo) {
        this.zhaoshangInfo = zhaoshangInfo;
    }
}
