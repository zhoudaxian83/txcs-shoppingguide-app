package com.tmall.wireless.tac.biz.processor.firstpage.banner.iteminfo.model;

import lombok.Data;

import java.util.List;

/**
 * Created by yangqing.byq on 2021/4/6.
 */
@Data
public class BannerVO {
    String source;

    List<Long> failItemList;

    String entryItems;

    List<Long> itemIdList;

    List<BannerItemVO> items;
}
